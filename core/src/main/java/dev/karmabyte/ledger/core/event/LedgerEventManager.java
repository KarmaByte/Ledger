package dev.karmabyte.ledger.core.event;

import com.hypixel.hytale.event.EventBus;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.server.core.HytaleServer;
import dev.karmabyte.ledger.api.economy.Account;
import dev.karmabyte.ledger.api.economy.Currency;
import dev.karmabyte.ledger.api.economy.transaction.TransactionResult;
import dev.karmabyte.ledger.api.economy.transaction.TransactionType;
import dev.karmabyte.ledger.api.event.LedgerEvent;
import dev.karmabyte.ledger.api.event.economy.BalanceChangeEvent;
import dev.karmabyte.ledger.api.event.economy.PostTransactionEvent;
import dev.karmabyte.ledger.api.event.economy.PreTransactionEvent;
import dev.karmabyte.ledger.core.LedgerPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Manages the firing and handling of Ledger events.
 *
 * <p>This class integrates with Hytale's event system to dispatch
 * Ledger-specific events that other plugins can listen to.
 *
 * <h2>Example - Other plugins listening to events:</h2>
 * <pre>{@code
 * // In another plugin's setup method
 * getEventRegistry().register(PreTransactionEvent.class, event -> {
 *     // Tax all deposits by 5%
 *     if (event.getType() == TransactionType.DEPOSIT) {
 *         event.setAmount(event.getAmount() * 0.95);
 *     }
 * });
 *
 * getEventRegistry().register(PostTransactionEvent.class, event -> {
 *     // Log all successful transfers
 *     if (event.isSuccess() && event.getType() == TransactionType.TRANSFER) {
 *         getLogger().atInfo().log("Transfer: %s sent %s",
 *             event.getAccount().getName(),
 *             event.getResult().getAmountFormatted());
 *     }
 * });
 * }</pre>
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public class LedgerEventManager {

    private final LedgerPlugin plugin;

    public LedgerEventManager(@NotNull LedgerPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Fire a PreTransactionEvent and return whether it was cancelled.
     *
     * @param account the account involved
     * @param currency the currency being used
     * @param type the transaction type
     * @param amount the initial amount
     * @return the event (check isCancelled() and getAmount() for modified values)
     */
    @NotNull
    public PreTransactionEvent firePreTransaction(
            @NotNull Account account,
            @NotNull Currency currency,
            @NotNull TransactionType type,
            double amount) {

        PreTransactionEvent event = new PreTransactionEvent(account, currency, type, amount);
        dispatchEvent(PreTransactionEvent.class, event);

        if (plugin.getConfiguration().isDebug() && event.isCancelled()) {
            plugin.getLogger().atInfo().log("PreTransactionEvent cancelled: %s (reason: %s)",
                type, event.getCancelReason());
        }

        return event;
    }

    /**
     * Fire a PostTransactionEvent after a transaction completes.
     *
     * @param account the account involved
     * @param currency the currency used
     * @param type the transaction type
     * @param result the transaction result
     */
    public void firePostTransaction(
            @NotNull Account account,
            @NotNull Currency currency,
            @NotNull TransactionType type,
            @NotNull TransactionResult result) {

        PostTransactionEvent event = new PostTransactionEvent(account, currency, type, result);
        dispatchEvent(PostTransactionEvent.class, event);

        if (plugin.getConfiguration().isDebug()) {
            plugin.getLogger().atInfo().log("PostTransactionEvent fired: %s, success=%s, amount=%s",
                type, result.isSuccess(), result.getAmountFormatted());
        }
    }

    /**
     * Fire a BalanceChangeEvent after a balance modification.
     *
     * @param account the account
     * @param currency the currency
     * @param cause what caused the change
     * @param previousBalance balance before
     * @param newBalance balance after
     */
    public void fireBalanceChange(
            @NotNull Account account,
            @NotNull Currency currency,
            @NotNull TransactionType cause,
            double previousBalance,
            double newBalance) {

        BalanceChangeEvent event = new BalanceChangeEvent(
            account, currency, cause, previousBalance, newBalance
        );
        dispatchEvent(BalanceChangeEvent.class, event);
    }

    /**
     * Dispatch a Ledger event through Hytale's event system.
     *
     * @param eventClass the event class
     * @param event the event instance
     * @param <T> the event type
     */
    private <T extends LedgerEvent> void dispatchEvent(Class<T> eventClass, T event) {
        try {
            EventBus eventBus = HytaleServer.get().getEventBus();
            IEventDispatcher<T, T> dispatcher = eventBus.dispatchFor(eventClass, null);

            if (dispatcher.hasListener()) {
                dispatcher.dispatch(event);
            }
        } catch (Exception e) {
            plugin.getLogger().atWarning().log("Error dispatching %s: %s",
                event.getEventName(), e.getMessage());
        }
    }
}
