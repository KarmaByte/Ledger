package dev.karmabyte.ledger.core.storage;

import dev.karmabyte.ledger.api.economy.Account;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for data storage backends.
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public interface StorageProvider {

    /**
     * Initialize the storage backend.
     * Called once on plugin startup.
     *
     * @throws Exception if initialization fails
     */
    void initialize() throws Exception;

    /**
     * Close the storage backend.
     * Called on plugin shutdown.
     *
     * @throws Exception if closing fails
     */
    void close() throws Exception;

    /**
     * Check if an account exists.
     *
     * @param playerId the player UUID
     * @return future with true if account exists
     */
    @NotNull
    CompletableFuture<Boolean> hasAccount(@NotNull UUID playerId);

    /**
     * Load an account from storage.
     *
     * @param playerId the player UUID
     * @return future with optional account
     */
    @NotNull
    CompletableFuture<Optional<Account>> loadAccount(@NotNull UUID playerId);

    /**
     * Save an account to storage.
     *
     * @param account the account to save
     * @return future completing when saved
     */
    @NotNull
    CompletableFuture<Void> saveAccount(@NotNull Account account);

    /**
     * Delete an account from storage.
     *
     * @param playerId the player UUID
     * @return future with true if deleted
     */
    @NotNull
    CompletableFuture<Boolean> deleteAccount(@NotNull UUID playerId);

    /**
     * Get top balances for a currency.
     *
     * @param currencyId the currency identifier
     * @param limit maximum number of results
     * @return future with list of player UUIDs sorted by balance (descending)
     */
    @NotNull
    CompletableFuture<List<UUID>> getTopBalances(@NotNull String currencyId, int limit);

    /**
     * Get the storage type name.
     *
     * @return the storage type
     */
    @NotNull
    String getType();

    /**
     * Get the total number of accounts.
     *
     * @return future with account count
     */
    @NotNull
    CompletableFuture<Integer> getAccountCount();
}
