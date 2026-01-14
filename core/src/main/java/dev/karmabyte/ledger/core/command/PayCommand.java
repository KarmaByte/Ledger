package dev.karmabyte.ledger.core.command;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.karmabyte.ledger.api.economy.transaction.TransactionResult;
import dev.karmabyte.ledger.core.LedgerPlugin;
import dev.karmabyte.ledger.core.config.LedgerConfig;
import dev.karmabyte.ledger.core.economy.SimpleEconomyService;
import dev.karmabyte.ledger.core.util.Messages;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Command to pay another player.
 * Usage: /pay <player> <amount>
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public class PayCommand extends CommandBase {

    private final RequiredArg<PlayerRef> targetArg;
    private final RequiredArg<Double> amountArg;

    // Cooldown tracking: player UUID -> last pay timestamp (millis)
    private static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public PayCommand() {
        super("pay", "Pay another player");
        addAliases("transfer", "send");

        targetArg = withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF);
        amountArg = withRequiredArg("amount", "Amount to pay", ArgTypes.DOUBLE);
    }

    @Override
    protected boolean canGeneratePermission() {
        return false; // Public command - no permission required
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        if (!context.isPlayer()) {
            context.sendMessage(Messages.errorPlayerOnly());
            return;
        }

        LedgerPlugin plugin = LedgerPlugin.getInstance();
        LedgerConfig config = plugin.getConfiguration();
        SimpleEconomyService economy = plugin.getEconomyService();

        var sender = context.sender();
        UUID senderUuid = sender.getUuid();
        PlayerRef target = context.get(targetArg);
        double amount = context.get(amountArg);

        // Validate amount > 0
        if (amount <= 0) {
            context.sendMessage(Messages.payInvalidAmount());
            return;
        }

        // Check minimum amount
        double minAmount = config.getMinPayAmount();
        if (amount < minAmount) {
            String formatted = economy.getPrimaryCurrency().format(minAmount);
            context.sendMessage(Messages.payMinAmount(formatted));
            return;
        }

        // Check maximum amount
        double maxAmount = config.getMaxPayAmount();
        if (amount > maxAmount) {
            String formatted = economy.getPrimaryCurrency().format(maxAmount);
            context.sendMessage(Messages.payMaxAmount(formatted));
            return;
        }

        // Can't pay yourself
        if (senderUuid.equals(target.getUuid())) {
            context.sendMessage(Messages.paySelfNotAllowed());
            return;
        }

        // Check cooldown
        int cooldownSeconds = config.getPayCooldownSeconds();
        if (cooldownSeconds > 0) {
            Long lastPay = cooldowns.get(senderUuid);
            if (lastPay != null) {
                long elapsed = (System.currentTimeMillis() - lastPay) / 1000;
                if (elapsed < cooldownSeconds) {
                    int remaining = (int) (cooldownSeconds - elapsed);
                    context.sendMessage(Messages.payCooldown(remaining));
                    return;
                }
            }
        }

        // Execute transfer
        economy.transfer(senderUuid, target.getUuid(), amount).thenAccept(result -> {
            if (result.isSuccess()) {
                // Update cooldown
                if (cooldownSeconds > 0) {
                    cooldowns.put(senderUuid, System.currentTimeMillis());
                }

                String formatted = economy.getPrimaryCurrency().format(amount);
                context.sendMessage(Messages.paySent(target.getUsername(), formatted));
            } else {
                if (result.getStatus() == TransactionResult.Status.INSUFFICIENT_FUNDS) {
                    context.sendMessage(Messages.payInsufficientFunds());
                } else {
                    context.sendMessage(Messages.errorGeneric());
                }
            }
        }).exceptionally(e -> {
            context.sendMessage(Messages.errorGeneric());
            return null;
        });
    }

    /**
     * Clear cooldown for a player (for admin use).
     */
    public static void clearCooldown(UUID playerId) {
        cooldowns.remove(playerId);
    }
}
