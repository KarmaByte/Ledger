package dev.karmabyte.ledger.api;

import dev.karmabyte.ledger.api.economy.EconomyService;
import org.jetbrains.annotations.NotNull;

/**
 * Service provider interface for Ledger.
 *
 * <p>Implementations of this interface provide the actual economy functionality.
 * The default implementation is provided by the Ledger Core module, but other
 * plugins can provide their own implementations.
 *
 * <h2>For Economy Providers:</h2>
 * <p>If you want to create your own economy implementation (like EssentialsX Eco),
 * implement this interface and register it with {@link Ledger#registerProvider(LedgerProvider)}.
 *
 * @author KarmaByte
 * @since 1.0.0
 * @see Ledger
 * @see EconomyService
 */
public interface LedgerProvider {

    /**
     * Get the name of this economy provider.
     *
     * @return the provider name (e.g., "Ledger", "EssentialsEco")
     */
    @NotNull
    String getName();

    /**
     * Get the version of this provider.
     *
     * @return the provider version
     */
    @NotNull
    String getVersion();

    /**
     * Get the economy service provided by this implementation.
     *
     * @return the economy service
     */
    @NotNull
    EconomyService getEconomyService();

    /**
     * Check if the provider is enabled and ready.
     *
     * @return true if the provider is operational
     */
    boolean isEnabled();

    /**
     * Called when the provider is being disabled.
     * Use this to clean up resources.
     */
    default void onDisable() {
        // Default no-op
    }
}
