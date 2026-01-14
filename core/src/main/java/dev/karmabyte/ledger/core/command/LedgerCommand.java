package dev.karmabyte.ledger.core.command;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import dev.karmabyte.ledger.core.LedgerPlugin;
import dev.karmabyte.ledger.core.economy.SimpleEconomyService;
import dev.karmabyte.ledger.core.util.Messages;

import javax.annotation.Nonnull;

/**
 * Main Ledger admin command.
 * Usage: /ledger <reload|info|version>
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public class LedgerCommand extends CommandBase {

    public LedgerCommand() {
        super("ledger", "Ledger economy administration");
        requirePermission("ledger.admin");

        addSubCommand(new LedgerReloadCommand());
        addSubCommand(new LedgerInfoCommand());
        addSubCommand(new LedgerVersionCommand());
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        context.sendMessage(Message.raw("Usage: /ledger <reload|info|version>"));
    }

    /**
     * /ledger reload - Reload configuration
     */
    public static class LedgerReloadCommand extends CommandBase {
        public LedgerReloadCommand() {
            super("reload", "Reload Ledger configuration");
            requirePermission("ledger.admin.reload");
        }

        @Override
        protected void executeSync(@Nonnull CommandContext context) {
            LedgerPlugin plugin = LedgerPlugin.getInstance();

            if (plugin.reload()) {
                context.sendMessage(Messages.reloadSuccess());
            } else {
                context.sendMessage(Messages.reloadFailed());
            }
        }
    }

    /**
     * /ledger info - Show economy info
     */
    public static class LedgerInfoCommand extends CommandBase {
        public LedgerInfoCommand() {
            super("info", "Show Ledger economy information");
        }

        @Override
        protected void executeSync(@Nonnull CommandContext context) {
            LedgerPlugin plugin = LedgerPlugin.getInstance();
            SimpleEconomyService economy = plugin.getEconomyService();

            String version = plugin.getVersion();
            String storage = plugin.getStorage().getClass().getSimpleName()
                .replace("StorageProvider", "")
                .replace("Storage", "");
            String currency = economy.getPrimaryCurrency().getDisplayName();

            economy.getStorage().getAccountCount().thenAccept(count -> {
                context.sendMessage(Messages.ledgerInfo(version, storage, currency, count));
            }).exceptionally(e -> {
                context.sendMessage(Messages.ledgerInfo(version, storage, currency, -1));
                return null;
            });
        }
    }

    /**
     * /ledger version - Show version
     */
    public static class LedgerVersionCommand extends CommandBase {
        public LedgerVersionCommand() {
            super("version", "Show Ledger version");
            addAliases("ver", "v");
        }

        @Override
        protected void executeSync(@Nonnull CommandContext context) {
            LedgerPlugin plugin = LedgerPlugin.getInstance();
            context.sendMessage(Messages.success("Ledger v" + plugin.getVersion()));
        }
    }
}
