package dev.karmabyte.ledger.api.event.economy;

import dev.karmabyte.ledger.api.economy.Account;
import dev.karmabyte.ledger.api.economy.Currency;
import dev.karmabyte.ledger.api.economy.transaction.TransactionType;
import dev.karmabyte.ledger.api.event.Cancellable;
import dev.karmabyte.ledger.api.event.LedgerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Event fired before a transaction is processed.
 *
 * <p>This event can be cancelled to prevent the transaction.
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * @EventHandler
 * public void onPreTransaction(PreTransactionEvent event) {
 *     // Block transactions over 10000
 *     if (event.getAmount() > 10000) {
 *         event.cancel("Amount exceeds limit");
 *     }
 *
 *     // Apply tax on deposits
 *     if (event.getType() == TransactionType.DEPOSIT) {
 *         double taxed = event.getAmount() * 0.9; // 10% tax
 *         event.setAmount(taxed);
 *     }
 * }
 * }</pre>
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public class PreTransactionEvent extends LedgerEvent implements Cancellable {

    private final Account account;
    private final Currency currency;
    private final TransactionType type;
    private double amount;
    private boolean cancelled;
    private String cancelReason;

    public PreTransactionEvent(
            @NotNull Account account,
            @NotNull Currency currency,
            @NotNull TransactionType type,
            double amount) {
        this.account = account;
        this.currency = currency;
        this.type = type;
        this.amount = amount;
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
     * Get the currency being used.
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
     * Get the transaction amount.
     *
     * @return the amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Modify the transaction amount.
     *
     * <p>Can be used to apply taxes, bonuses, etc.
     *
     * @param amount the new amount (must be positive and finite)
     * @throws IllegalArgumentException if amount is not positive or is NaN/Infinity
     */
    public void setAmount(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            throw new IllegalArgumentException("Amount must be a finite number");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.amount = amount;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    @Nullable
    public String getCancelReason() {
        return cancelReason;
    }

    @Override
    public void cancel(@NotNull String reason) {
        this.cancelled = true;
        this.cancelReason = reason;
    }
}
