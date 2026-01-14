package dev.karmabyte.ledger.api.economy;

import dev.karmabyte.ledger.api.economy.transaction.TransactionResult;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Main interface for economy operations.
 *
 * <p>This is the primary interface that economy provider plugins implement
 * and that consumer plugins use for all economy operations.
 *
 * <h2>For Plugin Developers (Consumers):</h2>
 * <pre>{@code
 * // Get economy service
 * EconomyService economy = Ledger.getEconomyOrThrow();
 *
 * // Simple operations use primary currency
 * economy.getBalance(playerId).thenAccept(balance -> ...);
 * economy.withdraw(playerId, 100.0).thenAccept(result -> ...);
 *
 * // Multi-currency operations
 * Currency gems = economy.getCurrency("gems").orElseThrow();
 * economy.getAccount(playerId).thenAccept(account -> {
 *     account.deposit(gems, 50.0);
 * });
 * }</pre>
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public interface EconomyService {

    // ==================== Currency Management ====================

    /**
     * Get the primary/default currency.
     *
     * <p>Every server must have exactly one primary currency.
     *
     * @return the primary currency
     */
    @NotNull
    Currency getPrimaryCurrency();

    /**
     * Get all registered currencies.
     *
     * @return unmodifiable set of all currencies
     */
    @NotNull
    Set<Currency> getCurrencies();

    /**
     * Get a currency by its identifier.
     *
     * @param identifier the currency ID (case-insensitive)
     * @return optional containing the currency if found
     */
    @NotNull
    Optional<Currency> getCurrency(@NotNull String identifier);

    /**
     * Register a new currency.
     *
     * <p>Fires {@link dev.karmabyte.ledger.api.event.economy.CurrencyRegisterEvent}.
     *
     * @param currency the currency to register
     * @return future completing when registered
     * @throws IllegalArgumentException if currency with same ID already exists
     */
    @NotNull
    CompletableFuture<Void> registerCurrency(@NotNull Currency currency);

    /**
     * Unregister a currency.
     *
     * <p><b>Warning:</b> This will NOT delete existing balances.
     *
     * @param identifier the currency ID to unregister
     * @return future completing with true if removed
     */
    @NotNull
    CompletableFuture<Boolean> unregisterCurrency(@NotNull String identifier);

    // ==================== Account Management ====================

    /**
     * Check if a player has an account.
     *
     * @param playerId the player UUID
     * @return future with true if account exists
     */
    @NotNull
    CompletableFuture<Boolean> hasAccount(@NotNull UUID playerId);

    /**
     * Get a player's account.
     *
     * <p>If the account doesn't exist, it will be created with default balances.
     *
     * @param playerId the player UUID
     * @return future with the account
     */
    @NotNull
    CompletableFuture<Account> getAccount(@NotNull UUID playerId);

    /**
     * Create an account for a player.
     *
     * <p>If an account already exists, returns the existing account.
     * Fires {@link dev.karmabyte.ledger.api.event.economy.AccountCreateEvent}.
     *
     * @param playerId the player UUID
     * @return future with the account
     */
    @NotNull
    CompletableFuture<Account> createAccount(@NotNull UUID playerId);

    /**
     * Delete a player's account.
     *
     * <p><b>Warning:</b> This permanently deletes all balances.
     *
     * @param playerId the player UUID
     * @return future with true if deleted
     */
    @NotNull
    CompletableFuture<Boolean> deleteAccount(@NotNull UUID playerId);

    // ==================== Quick Operations (Primary Currency) ====================

    /**
     * Get a player's balance in the primary currency.
     *
     * @param playerId the player UUID
     * @return future with the balance
     */
    @NotNull
    CompletableFuture<Double> getBalance(@NotNull UUID playerId);

    /**
     * Get a player's balance in a specific currency.
     *
     * @param playerId the player UUID
     * @param currency the currency
     * @return future with the balance
     */
    @NotNull
    CompletableFuture<Double> getBalance(@NotNull UUID playerId, @NotNull Currency currency);

    /**
     * Check if a player has at least the specified amount in primary currency.
     *
     * @param playerId the player UUID
     * @param amount the amount to check
     * @return future with true if balance >= amount
     */
    @NotNull
    CompletableFuture<Boolean> has(@NotNull UUID playerId, double amount);

    /**
     * Check if a player has at least the specified amount in a currency.
     *
     * @param playerId the player UUID
     * @param currency the currency
     * @param amount the amount to check
     * @return future with true if balance >= amount
     */
    @NotNull
    CompletableFuture<Boolean> has(@NotNull UUID playerId, @NotNull Currency currency, double amount);

    /**
     * Deposit money to a player in primary currency.
     *
     * @param playerId the player UUID
     * @param amount the amount to deposit (must be positive)
     * @return future with the transaction result
     */
    @NotNull
    CompletableFuture<TransactionResult> deposit(@NotNull UUID playerId, double amount);

    /**
     * Deposit money to a player in a specific currency.
     *
     * @param playerId the player UUID
     * @param currency the currency
     * @param amount the amount to deposit (must be positive)
     * @return future with the transaction result
     */
    @NotNull
    CompletableFuture<TransactionResult> deposit(@NotNull UUID playerId, @NotNull Currency currency, double amount);

    /**
     * Withdraw money from a player in primary currency.
     *
     * @param playerId the player UUID
     * @param amount the amount to withdraw (must be positive)
     * @return future with the transaction result
     */
    @NotNull
    CompletableFuture<TransactionResult> withdraw(@NotNull UUID playerId, double amount);

    /**
     * Withdraw money from a player in a specific currency.
     *
     * @param playerId the player UUID
     * @param currency the currency
     * @param amount the amount to withdraw (must be positive)
     * @return future with the transaction result
     */
    @NotNull
    CompletableFuture<TransactionResult> withdraw(@NotNull UUID playerId, @NotNull Currency currency, double amount);

    /**
     * Set a player's balance in primary currency (admin operation).
     *
     * @param playerId the player UUID
     * @param amount the new balance
     * @return future with the transaction result
     */
    @NotNull
    CompletableFuture<TransactionResult> setBalance(@NotNull UUID playerId, double amount);

    /**
     * Set a player's balance in a specific currency (admin operation).
     *
     * @param playerId the player UUID
     * @param currency the currency
     * @param amount the new balance
     * @return future with the transaction result
     */
    @NotNull
    CompletableFuture<TransactionResult> setBalance(@NotNull UUID playerId, @NotNull Currency currency, double amount);

    /**
     * Transfer money between players in primary currency.
     *
     * @param from the sender UUID
     * @param to the receiver UUID
     * @param amount the amount to transfer
     * @return future with the transaction result
     */
    @NotNull
    CompletableFuture<TransactionResult> transfer(@NotNull UUID from, @NotNull UUID to, double amount);

    /**
     * Transfer money between players in a specific currency.
     *
     * @param from the sender UUID
     * @param to the receiver UUID
     * @param currency the currency
     * @param amount the amount to transfer
     * @return future with the transaction result
     */
    @NotNull
    CompletableFuture<TransactionResult> transfer(@NotNull UUID from, @NotNull UUID to, @NotNull Currency currency, double amount);

    // ==================== Formatting ====================

    /**
     * Format an amount using the primary currency's format.
     *
     * @param amount the amount to format
     * @return formatted string
     */
    @NotNull
    default String format(double amount) {
        return getPrimaryCurrency().format(amount);
    }

    /**
     * Format an amount using a specific currency's format.
     *
     * @param currency the currency
     * @param amount the amount to format
     * @return formatted string
     */
    @NotNull
    default String format(@NotNull Currency currency, double amount) {
        return currency.format(amount);
    }
}
