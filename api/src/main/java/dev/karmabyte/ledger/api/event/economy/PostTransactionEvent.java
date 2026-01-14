package dev.karmabyte.ledger.api.event.economy;

import dev.karmabyte.ledger.api.economy.Account;
import dev.karmabyte.ledger.api.economy.Currency;
import dev.karmabyte.ledger.api.economy.transaction.TransactionResult;
import dev.karmabyte.ledger.api.economy.transaction.TransactionType;
import dev.karmabyte.ledger.api.event.LedgerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Event fired after a transaction is completed.
 *
 * <p>This event is read-only and cannot be cancelled.
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * @EventHandler
 * public void onPostTransaction(PostTransactionEvent event) {
 *     if (event.isSuccess() && event.getType() == TransactionType.DEPOSIT) {
 *         // Log large deposits
 *         if (event.getAmount() > 1000) {
 *             logger.info("Large deposit: " + event.getAmount() +
 *                 " to " + event.getAccount().getName());
 *         }
 *     }
 * }
 * }</pre>
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public class PostTransactionEvent extends LedgerEvent {

    private final Account account;
    private final Currency currency;
    private final TransactionType type;
    private final TransactionResult result;

    public PostTransactionEvent(
            @NotNull Account account,
            @NotNull Currency currency,
            @NotNull TransactionType type,
            @NotNull TransactionResult result) {
        this.account = account;
        this.currency = currency;
        this.type = type;
        this.result = result;
    }

    /**
     * Get the account involved in the transaction.
     *
     * @return the account
     */
    @NotNull
    public Account getAccount() {
        return account;
    }

    /**
     * Get the currency used.
     *
     * @return the currency
     */
    @NotNull
    public Currency getCurrency() {
        return currency;
    }

    /**
     * Get the transaction type.
     *
     * @return the type
     */
    @NotNull
    public TransactionType getType() {
        return type;
    }

    /**
     * Get the transaction result.
     *
     * @return the result
     */
    @NotNull
    public TransactionResult getResult() {
        return result;
    }

    /**
     * Get the transaction amount.
     *
     * @return the amount
     */
    public double getAmount() {
        return result.getAmount();
    }

    /**
     * Get the new balance after the transaction.
     *
     * @return the new balance
     */
    public double getNewBalance() {
        return result.getNewBalance();
    }

    /**
     * Get the previous balance before the transaction.
     *
     * @return the previous balance
     */
    public double getPreviousBalance() {
        return result.getPreviousBalance();
    }

    /**
     * Check if the transaction was successful.
     *
     * @return true if successful
     */
    public boolean isSuccess() {
        return result.isSuccess();
    }
}
