package dev.karmabyte.ledger.api;

import dev.karmabyte.ledger.api.economy.EconomyService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Main entry point for the Ledger Economy API.
 *
 * <p>This class provides static access to the economy service and should be used
 * by plugins wanting to integrate with the economy system.
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * // Get the economy service
 * Optional<EconomyService> economy = Ledger.getEconomy();
 *
 * if (economy.isPresent()) {
 *     // Check if player has 100 coins
 *     economy.get().has(playerId, 100.0).thenAccept(hasEnough -> {
 *         if (hasEnough) {
 *             // Withdraw money
 *             economy.get().withdraw(playerId, 100.0);
 *         }
 *     });
 * }
 * }</pre>
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public final class Ledger {

    private static LedgerProvider provider;

    private Ledger() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    /**
     * Get the economy service if available.
     *
     * @return optional containing the economy service, or empty if not registered
     */
    @NotNull
    public static Optional<EconomyService> getEconomy() {
        return Optional.ofNullable(provider).map(LedgerProvider::getEconomyService);
    }

    /**
     * Get the economy service, throwing if not available.
     *
     * @return the economy service
     * @throws IllegalStateException if no economy provider is registered
     */
    @NotNull
    public static EconomyService getEconomyOrThrow() {
        return getEconomy().orElseThrow(() ->
            new IllegalStateException("No economy provider registered. Is Ledger installed?"));
    }

    /**
     * Check if an economy provider is registered.
     *
     * @return true if economy is available
     */
    public static boolean hasEconomy() {
        return provider != null && provider.getEconomyService() != null;
    }

    /**
     * Get the current Ledger provider.
     *
     * @return the provider, or null if not registered
     */
    @Nullable
    public static LedgerProvider getProvider() {
        return provider;
    }

    /**
     * Get the API version.
     *
     * @return the API version string
     */
    @NotNull
    public static String getVersion() {
        return "1.0.0";
    }

    /**
     * Register the Ledger provider. This should only be called by the Ledger plugin.
     *
     * <p><b>Note:</b> This method is for internal use only. Plugin developers should
     * not call this method.
     *
     * @param ledgerProvider the provider to register
     * @throws IllegalStateException if a provider is already registered
     */
    public static void registerProvider(@NotNull LedgerProvider ledgerProvider) {
        if (provider != null) {
            throw new IllegalStateException("A Ledger provider is already registered");
        }
        provider = ledgerProvider;
    }

    /**
     * Unregister the current provider. This should only be called by the Ledger plugin.
     *
     * <p><b>Note:</b> This method is for internal use only.
     */
    public static void unregisterProvider() {
        provider = null;
    }
}
