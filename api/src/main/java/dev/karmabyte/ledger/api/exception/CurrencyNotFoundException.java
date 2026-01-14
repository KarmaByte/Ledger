package dev.karmabyte.ledger.api.exception;

import org.jetbrains.annotations.NotNull;

/**
 * Exception thrown when a currency is not found.
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public class CurrencyNotFoundException extends LedgerException {

    private final String currencyId;

    /**
     * Create a new exception for a missing currency.
     *
     * @param currencyId the currency identifier that wasn't found
     */
    public CurrencyNotFoundException(@NotNull String currencyId) {
        super("Currency not found: " + currencyId);
        this.currencyId = currencyId;
    }

    /**
     * Create a new exception with a custom message.
     *
     * @param currencyId the currency identifier
     * @param message custom error message
     */
    public CurrencyNotFoundException(@NotNull String currencyId, @NotNull String message) {
        super(message);
        this.currencyId = currencyId;
    }

    /**
     * Get the currency ID that wasn't found.
     *
     * @return the currency identifier
     */
    @NotNull
    public String getCurrencyId() {
        return currencyId;
    }
}
