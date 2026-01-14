package dev.karmabyte.ledger.api.exception;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Exception thrown when an account is not found.
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public class AccountNotFoundException extends LedgerException {

    private final UUID accountId;

    /**
     * Create a new exception for a missing account.
     *
     * @param accountId the account UUID that wasn't found
     */
    public AccountNotFoundException(@NotNull UUID accountId) {
        super("Account not found: " + accountId);
        this.accountId = accountId;
    }

    /**
     * Create a new exception with a custom message.
     *
     * @param accountId the account UUID
     * @param message custom error message
     */
    public AccountNotFoundException(@NotNull UUID accountId, @NotNull String message) {
        super(message);
        this.accountId = accountId;
    }

    /**
     * Get the account ID that wasn't found.
     *
     * @return the account UUID
     */
    @NotNull
    public UUID getAccountId() {
        return accountId;
    }
}
