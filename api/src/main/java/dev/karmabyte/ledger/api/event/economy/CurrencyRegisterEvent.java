package dev.karmabyte.ledger.api.event.economy;

import dev.karmabyte.ledger.api.economy.Currency;
import dev.karmabyte.ledger.api.event.LedgerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Event fired when a new currency is registered.
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * @EventHandler
 * public void onCurrencyRegister(CurrencyRegisterEvent event) {
 *     Currency currency = event.getCurrency();
 *     logger.info("New currency registered: " + currency.getDisplayName());
 *
 *     if (currency.isPrimary()) {
 *         logger.info("This is the primary currency!");
 *     }
 * }
 * }</pre>
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public class CurrencyRegisterEvent extends LedgerEvent {

    private final Currency currency;

    public CurrencyRegisterEvent(@NotNull Currency currency) {
        this.currency = currency;
    }

    /**
     * Get the currency that was registered.
     *
     * @return the currency
     */
    @NotNull
    public Currency getCurrency() {
        return currency;
    }

    /**
     * Check if the registered currency is the primary currency.
     *
     * @return true if primary
     */
    public boolean isPrimary() {
        return currency.isPrimary();
    }
}
