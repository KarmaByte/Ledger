package dev.karmabyte.ledger.core.command;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.karmabyte.ledger.core.LedgerPlugin;
import dev.karmabyte.ledger.core.economy.SimpleEconomyService;
import dev.karmabyte.ledger.core.util.Messages;

import javax.annotation.Nonnull;

/**
 * Command to check balance.
 * Usage: /balance [player]
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public class BalanceCommand extends CommandBase {

    private final OptionalArg<PlayerRef> targetArg;

    public BalanceCommand() {
        super("balance", "Check your balance or another player's balance");
        addAliases("bal", "money");

        targetArg = withOptionalArg("player", "Target player", ArgTypes.PLAYER_REF);
    }

    @Override
    protected boolean canGeneratePermission() {
        return false; // Public command - no permission required
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        LedgerPlugin plugin = LedgerPlugin.getInstance();
        SimpleEconomyService economy = plugin.getEconomyService();

        if (context.provided(targetArg)) {
            // Check another player's balance - requires additional permission
            if (!context.sender().hasPermission("ledger.balance.others")) {
                context.sendMessage(Messages.errorNoPermission());
                return;
            }
            PlayerRef target = context.get(targetArg);
            economy.getBalance(target.getUuid()).thenAccept(balance -> {
                String formatted = economy.getPrimaryCurrency().format(balance);
                context.sendMessage(Messages.balanceOther(target.getUsername(), formatted));
            }).exceptionally(e -> {
                context.sendMessage(Messages.errorGeneric());
                return null;
            });
        } else {
            // Check own balance
            if (!context.isPlayer()) {
                context.sendMessage(Messages.errorPlayerOnly());
                return;
            }

            var sender = context.sender();
            economy.getBalance(sender.getUuid()).thenAccept(balance -> {
                String formatted = economy.getPrimaryCurrency().format(balance);
                context.sendMessage(Messages.balanceSelf(formatted));
            }).exceptionally(e -> {
                context.sendMessage(Messages.errorGeneric());
                return null;
            });
        }
    }
}
