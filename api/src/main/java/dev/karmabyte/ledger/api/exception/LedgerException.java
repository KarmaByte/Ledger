package dev.karmabyte.ledger.api.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base exception for all Ledger-related errors.
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public class LedgerException extends RuntimeException {

    /**
     * Create a new exception with a message.
     *
     * @param message the error message
     */
    public LedgerException(@NotNull String message) {
        super(message);
    }

    /**
     * Create a new exception with a message and cause.
     *
     * @param message the error message
     * @param cause the underlying cause
     */
    public LedgerException(@NotNull String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}
