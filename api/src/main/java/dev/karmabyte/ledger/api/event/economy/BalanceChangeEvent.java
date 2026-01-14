package dev.karmabyte.ledger.api.event.economy;

import dev.karmabyte.ledger.api.economy.Account;
import dev.karmabyte.ledger.api.economy.Currency;
import dev.karmabyte.ledger.api.economy.transaction.TransactionType;
import dev.karmabyte.ledger.api.event.LedgerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Event fired whenever an account's balance changes.
 *
 * <p>This is a generic event that fires for any balance modification.
 * For more specific handling, use {@link PreTransactionEvent} or {@link PostTransactionEvent}.
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * @EventHandler
 * public void onBalanceChange(BalanceChangeEvent event) {
 *     // Update scoreboard when balance changes
 *     Player player = getPlayer(event.getAccount().getOwner());
 *     if (player != null) {
 *         updateScoreboard(player, event.getNewBalance());
 *     }
 * }
 * }</pre>
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public class BalanceChangeEvent extends LedgerEvent {

    private final Account account;
    private final Currency currency;
    private final TransactionType cause;
    private final double previousBalance;
    private final double newBalance;

    public BalanceChangeEvent(
            @NotNull Account account,
            @NotNull Currency currency,
            @NotNull TransactionType cause,
            double previousBalance,
            double newBalance) {
        this.account = account;
        this.currency = currency;
        this.cause = cause;
        this.previousBalance = previousBalance;
        this.newBalance = newBalance;
    }

    /**
     * Get the account whose balance changed.
     *
     * @return the account
     */
    @NotNull
    public Account getAccount() {
        return account;
    }

    /**
     * Get the currency that changed.
     *
     * @return the currency
     */
    @NotNull
    public Currency getCurrency() {
        return currency;
    }

    /**
     * Get what caused the balance change.
     *
     * @return the transaction type that caused this change
     */
    @NotNull
    public TransactionType getCause() {
        return cause;
    }

    /**
     * Get the balance before the change.
     *
     * @return previous balance
     */
    public double getPreviousBalance() {
        return previousBalance;
    }

    /**
     * Get the balance after the change.
     *
     * @return new balance
     */
    public double getNewBalance() {
        return newBalance;
    }

    /**
     * Get the difference between old and new balance.
     *
     * <p>Positive means balance increased, negative means decreased.
     *
     * @return the balance change
     */
    public double getDifference() {
        return newBalance - previousBalance;
    }

    /**
     * Check if the balance increased.
     *
     * @return true if new balance > previous balance
     */
    public boolean isIncrease() {
        return newBalance > previousBalance;
    }

    /**
     * Check if the balance decreased.
     *
     * @return true if new balance < previous balance
     */
    public boolean isDecrease() {
        return newBalance < previousBalance;
    }
}
