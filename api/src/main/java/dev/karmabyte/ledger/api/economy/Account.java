package dev.karmabyte.ledger.api.economy;

import dev.karmabyte.ledger.api.economy.transaction.TransactionResult;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Represents an economy account.
 *
 * <p>Each player has one account that can hold balances in multiple currencies.
 * All operations are asynchronous and return {@link CompletableFuture}.
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * economy.getAccount(playerId).thenAccept(account -> {
 *     // Get balance in primary currency
 *     account.getBalance().thenAccept(balance -> {
 *         player.sendMessage("Your balance: " + balance);
 *     });
 *
 *     // Withdraw money
 *     account.withdraw(100.0).thenAccept(result -> {
 *         if (result.isSuccess()) {
 *             player.sendMessage("Withdrew 100 coins!");
 *         }
 *     });
 * });
 * }</pre>
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public interface Account {

    /**
     * Get the unique identifier of the account owner.
     *
     * @return the owner's UUID
     */
    @NotNull
    UUID getOwner();

    /**
     * Get the display name of the account.
     *
     * <p>Usually the player's name, but can be customized.
     *
     * @return the account name
     */
    @NotNull
    String getName();

    // ==================== Balance Operations ====================

    /**
     * Get the balance in the specified currency.
     *
     * @param currency the currency
     * @return future with the balance
     */
    @NotNull
    CompletableFuture<Double> getBalance(@NotNull Currency currency);

    /**
     * Get the balance in the primary currency.
     *
     * @return future with the balance
     */
    @NotNull
    CompletableFuture<Double> getBalance();

    /**
     * Get all balances for this account.
     *
     * @return future with map of currency to balance
     */
    @NotNull
    CompletableFuture<Map<Currency, Double>> getAllBalances();

    /**
     * Check if the account has at least the specified amount.
     *
     * @param currency the currency
     * @param amount the amount to check
     * @return future with true if balance >= amount
     */
    @NotNull
    CompletableFuture<Boolean> has(@NotNull Currency currency, double amount);

    /**
     * Check if the account has at least the specified amount in primary currency.
     *
     * @param amount the amount to check
     * @return future with true if balance >= amount
     */
    @NotNull
    CompletableFuture<Boolean> has(double amount);

    // ==================== Transactions ====================

    /**
     * Deposit money into the account.
     *
     * @param currency the currency
     * @param amount the amount to deposit (must be positive)
     * @return future with the transaction result
     */
    @NotNull
    CompletableFuture<TransactionResult> deposit(@NotNull Currency currency, double amount);

    /**
     * Deposit money in primary currency.
     *
     * @param amount the amount to deposit (must be positive)
     * @return future with the transaction result
     */
    @NotNull
    CompletableFuture<TransactionResult> deposit(double amount);

    /**
     * Withdraw money from the account.
     *
     * @param currency the currency
     * @param amount the amount to withdraw (must be positive)
     * @return future with the transaction result
     */
    @NotNull
    CompletableFuture<TransactionResult> withdraw(@NotNull Currency currency, double amount);

    /**
     * Withdraw money in primary currency.
     *
     * @param amount the amount to withdraw (must be positive)
     * @return future with the transaction result
     */
    @NotNull
    CompletableFuture<TransactionResult> withdraw(double amount);

    /**
     * Set the balance to a specific amount (admin operation).
     *
     * @param currency the currency
     * @param amount the new balance
     * @return future with the transaction result
     */
    @NotNull
    CompletableFuture<TransactionResult> setBalance(@NotNull Currency currency, double amount);

    /**
     * Set the balance in primary currency (admin operation).
     *
     * @param amount the new balance
     * @return future with the transaction result
     */
    @NotNull
    CompletableFuture<TransactionResult> setBalance(double amount);

    /**
     * Transfer money to another account.
     *
     * @param target the target account
     * @param currency the currency
     * @param amount the amount to transfer
     * @return future with the transaction result
     */
    @NotNull
    CompletableFuture<TransactionResult> transfer(@NotNull Account target, @NotNull Currency currency, double amount);

    /**
     * Transfer money in primary currency to another account.
     *
     * @param target the target account
     * @param amount the amount to transfer
     * @return future with the transaction result
     */
    @NotNull
    CompletableFuture<TransactionResult> transfer(@NotNull Account target, double amount);

    /**
     * Reset this account to default balances for all currencies.
     *
     * @return future with the transaction result
     */
    @NotNull
    CompletableFuture<TransactionResult> reset();
}
