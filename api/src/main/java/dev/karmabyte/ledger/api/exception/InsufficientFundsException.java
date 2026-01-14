package dev.karmabyte.ledger.api.exception;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Exception thrown when an account has insufficient funds.
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public class InsufficientFundsException extends LedgerException {

    private final UUID accountId;
    private final double required;
    private final double available;

    /**
     * Create a new insufficient funds exception.
     *
     * @param accountId the account UUID
     * @param required the amount required
     * @param available the amount available
     */
    public InsufficientFundsException(@NotNull UUID accountId, double required, double available) {
        super(String.format("Insufficient funds: required %.2f, available %.2f", required, available));
        this.accountId = accountId;
        this.required = required;
        this.available = available;
    }

    /**
     * Get the account that has insufficient funds.
     *
     * @return the account UUID
     */
    @NotNull
    public UUID getAccountId() {
        return accountId;
    }

    /**
     * Get the amount that was required.
     *
     * @return required amount
     */
    public double getRequired() {
        return required;
    }

    /**
     * Get the amount that was available.
     *
     * @return available amount
     */
    public double getAvailable() {
        return available;
    }

    /**
     * Get how much more is needed.
     *
     * @return the shortfall (required - available)
     */
    public double getShortfall() {
        return required - available;
    }
}
