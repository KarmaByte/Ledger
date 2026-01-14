package dev.karmabyte.ledger.core.command;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import dev.karmabyte.ledger.core.LedgerPlugin;
import dev.karmabyte.ledger.core.economy.SimpleEconomyService;
import dev.karmabyte.ledger.core.util.Messages;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Command to show top balances.
 * Usage: /baltop [page]
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public class BalTopCommand extends CommandBase {

    private static final int ENTRIES_PER_PAGE = 10;

    private final DefaultArg<Integer> pageArg;

    public BalTopCommand() {
        super("baltop", "Show top balances");
        addAliases("balancetop", "top");

        pageArg = withDefaultArg("page", "Page number", ArgTypes.INTEGER, 1, "1");
    }

    @Override
    protected boolean canGeneratePermission() {
        return false; // Public command - no permission required
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        LedgerPlugin plugin = LedgerPlugin.getInstance();
        SimpleEconomyService economy = plugin.getEconomyService();

        int page = context.get(pageArg);
        if (page < 1) page = 1;

        String currencyId = economy.getPrimaryCurrency().getIdentifier();

        int finalPage = page;
        economy.getStorage().getTopBalances(currencyId, ENTRIES_PER_PAGE * page).thenAccept(topList -> {
            int startIndex = (finalPage - 1) * ENTRIES_PER_PAGE;

            if (startIndex >= topList.size()) {
                context.sendMessage(Messages.baltopEmpty());
                return;
            }

            context.sendMessage(Messages.baltopHeader(finalPage));

            AtomicInteger rank = new AtomicInteger(startIndex + 1);

            for (int i = startIndex; i < Math.min(startIndex + ENTRIES_PER_PAGE, topList.size()); i++) {
                UUID uuid = topList.get(i);
                int currentRank = rank.getAndIncrement();

                economy.getAccount(uuid).thenAccept(account -> {
                    economy.getBalance(uuid).thenAccept(balance -> {
                        String formatted = economy.getPrimaryCurrency().format(balance);
                        context.sendMessage(Messages.baltopEntry(currentRank, account.getName(), formatted));
                    });
                });
            }
        }).exceptionally(e -> {
            context.sendMessage(Messages.errorGeneric());
            return null;
        });
    }
}
