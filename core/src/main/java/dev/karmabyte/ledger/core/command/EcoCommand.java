package dev.karmabyte.ledger.core.command;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.karmabyte.ledger.core.LedgerPlugin;
import dev.karmabyte.ledger.core.economy.SimpleEconomyService;
import dev.karmabyte.ledger.core.util.Messages;

import javax.annotation.Nonnull;

/**
 * Economy admin command.
 * Usage: /eco <give|take|set|reset> <player> [amount]
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public class EcoCommand extends CommandBase {

    public EcoCommand() {
        super("eco", "Economy admin commands");
        addAliases("economy");
        requirePermission("ledger.admin.eco");

        // Add subcommands
        addSubCommand(new EcoGiveCommand());
        addSubCommand(new EcoTakeCommand());
        addSubCommand(new EcoSetCommand());
        addSubCommand(new EcoResetCommand());
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        context.sendMessage(Message.raw("Usage: /eco <give|take|set|reset> <player> <amount>"));
    }

    /**
     * /eco give <player> <amount>
     */
    public static class EcoGiveCommand extends CommandBase {
        private final RequiredArg<PlayerRef> targetArg;
        private final RequiredArg<Double> amountArg;

        public EcoGiveCommand() {
            super("give", "Give money to a player");
            targetArg = withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF);
            amountArg = withRequiredArg("amount", "Amount to give", ArgTypes.DOUBLE);
        }

        @Override
        protected void executeSync(@Nonnull CommandContext context) {
            LedgerPlugin plugin = LedgerPlugin.getInstance();
            SimpleEconomyService economy = plugin.getEconomyService();

            PlayerRef target = context.get(targetArg);
            double amount = context.get(amountArg);

            if (amount <= 0) {
                context.sendMessage(Messages.payInvalidAmount());
                return;
            }

            economy.deposit(target.getUuid(), amount).thenAccept(result -> {
                if (result.isSuccess()) {
                    String formatted = economy.getPrimaryCurrency().format(amount);
                    context.sendMessage(Messages.ecoGive(target.getUsername(), formatted));
                } else {
                    context.sendMessage(Messages.errorGeneric());
                }
            });
        }
    }

    /**
     * /eco take <player> <amount>
     */
    public static class EcoTakeCommand extends CommandBase {
        private final RequiredArg<PlayerRef> targetArg;
        private final RequiredArg<Double> amountArg;

        public EcoTakeCommand() {
            super("take", "Take money from a player");
            targetArg = withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF);
            amountArg = withRequiredArg("amount", "Amount to take", ArgTypes.DOUBLE);
        }

        @Override
        protected void executeSync(@Nonnull CommandContext context) {
            LedgerPlugin plugin = LedgerPlugin.getInstance();
            SimpleEconomyService economy = plugin.getEconomyService();

            PlayerRef target = context.get(targetArg);
            double amount = context.get(amountArg);

            if (amount <= 0) {
                context.sendMessage(Messages.payInvalidAmount());
                return;
            }

            economy.withdraw(target.getUuid(), amount).thenAccept(result -> {
                if (result.isSuccess()) {
                    String formatted = economy.getPrimaryCurrency().format(amount);
                    context.sendMessage(Messages.ecoTake(target.getUsername(), formatted));
                } else {
                    context.sendMessage(Messages.errorGeneric());
                }
            });
        }
    }

    /**
     * /eco set <player> <amount>
     */
    public static class EcoSetCommand extends CommandBase {
        private final RequiredArg<PlayerRef> targetArg;
        private final RequiredArg<Double> amountArg;

        public EcoSetCommand() {
            super("set", "Set a player's balance");
            targetArg = withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF);
            amountArg = withRequiredArg("amount", "New balance", ArgTypes.DOUBLE);
        }

        @Override
        protected void executeSync(@Nonnull CommandContext context) {
            LedgerPlugin plugin = LedgerPlugin.getInstance();
            SimpleEconomyService economy = plugin.getEconomyService();

            PlayerRef target = context.get(targetArg);
            double amount = context.get(amountArg);

            economy.setBalance(target.getUuid(), amount).thenAccept(result -> {
                if (result.isSuccess()) {
                    String formatted = economy.getPrimaryCurrency().format(amount);
                    context.sendMessage(Messages.ecoSet(target.getUsername(), formatted));
                } else {
                    context.sendMessage(Messages.errorGeneric());
                }
            });
        }
    }

    /**
     * /eco reset <player>
     */
    public static class EcoResetCommand extends CommandBase {
        private final RequiredArg<PlayerRef> targetArg;

        public EcoResetCommand() {
            super("reset", "Reset a player's balance to default");
            targetArg = withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF);
        }

        @Override
        protected void executeSync(@Nonnull CommandContext context) {
            LedgerPlugin plugin = LedgerPlugin.getInstance();
            SimpleEconomyService economy = plugin.getEconomyService();

            PlayerRef target = context.get(targetArg);
            double defaultBalance = economy.getPrimaryCurrency().getDefaultBalance();

            economy.setBalance(target.getUuid(), defaultBalance).thenAccept(result -> {
                if (result.isSuccess()) {
                    String formatted = economy.getPrimaryCurrency().format(defaultBalance);
                    context.sendMessage(Messages.ecoReset(target.getUsername(), formatted));
                } else {
                    context.sendMessage(Messages.errorGeneric());
                }
            });
        }
    }
}
