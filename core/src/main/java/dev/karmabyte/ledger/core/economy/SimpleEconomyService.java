package dev.karmabyte.ledger.core.economy;

import dev.karmabyte.ledger.api.economy.Account;
import dev.karmabyte.ledger.api.economy.Currency;
import dev.karmabyte.ledger.api.economy.EconomyService;
import dev.karmabyte.ledger.api.economy.transaction.TransactionResult;
import dev.karmabyte.ledger.api.economy.transaction.TransactionType;
import dev.karmabyte.ledger.core.LedgerPlugin;
import dev.karmabyte.ledger.core.cache.AccountCache;
import dev.karmabyte.ledger.core.storage.StorageProvider;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of the EconomyService interface.
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public class SimpleEconomyService implements EconomyService {

    private final LedgerPlugin plugin;
    private final StorageProvider storage;
    private final AccountCache accountCache;
    private final Map<String, Currency> currencies = new ConcurrentHashMap<>();
    private Currency primaryCurrency;

    public SimpleEconomyService(LedgerPlugin plugin, StorageProvider storage) {
        this.plugin = plugin;
        this.storage = storage;
        this.accountCache = new AccountCache(plugin.getConfiguration().getCacheExpireMinutes());
    }

    // ==================== Currency Management ====================

    @Override
    @NotNull
    public Currency getPrimaryCurrency() {
        if (primaryCurrency == null) {
            throw new IllegalStateException("No primary currency registered");
        }
        return primaryCurrency;
    }

    @Override
    @NotNull
    public Set<Currency> getCurrencies() {
        return Collections.unmodifiableSet(new HashSet<>(currencies.values()));
    }

    @Override
    @NotNull
    public Optional<Currency> getCurrency(@NotNull String identifier) {
        return Optional.ofNullable(currencies.get(identifier.toLowerCase()));
    }

    @Override
    @NotNull
    public CompletableFuture<Void> registerCurrency(@NotNull Currency currency) {
        return CompletableFuture.runAsync(() -> registerCurrencySync(currency));
    }

    /**
     * Register a currency synchronously.
     *
     * @param currency the currency to register
     */
    public void registerCurrencySync(@NotNull Currency currency) {
        String id = currency.getIdentifier().toLowerCase();

        if (currencies.containsKey(id)) {
            throw new IllegalArgumentException("Currency '" + id + "' is already registered");
        }

        currencies.put(id, currency);

        if (currency.isPrimary()) {
            if (primaryCurrency != null) {
                plugin.getLogger().atWarning().log("Replacing primary currency '%s' with '%s'",
                    primaryCurrency.getIdentifier(), id);
            }
            primaryCurrency = currency;
        }

        // TODO: Fire CurrencyRegisterEvent when Hytale custom event system is better understood
    }

    @Override
    @NotNull
    public CompletableFuture<Boolean> unregisterCurrency(@NotNull String identifier) {
        return CompletableFuture.supplyAsync(() -> {
            String id = identifier.toLowerCase();
            Currency removed = currencies.remove(id);

            if (removed != null && removed.isPrimary()) {
                primaryCurrency = null;
            }

            return removed != null;
        });
    }

    // ==================== Account Management ====================

    @Override
    @NotNull
    public CompletableFuture<Boolean> hasAccount(@NotNull UUID playerId) {
        // Check cache first
        if (accountCache.contains(playerId)) {
            return CompletableFuture.completedFuture(true);
        }
        return storage.hasAccount(playerId);
    }

    @Override
    @NotNull
    public CompletableFuture<Account> getAccount(@NotNull UUID playerId) {
        // Check cache first
        Account cached = accountCache.get(playerId);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        return storage.loadAccount(playerId).thenCompose(optAccount -> {
            if (optAccount.isPresent()) {
                Account account = optAccount.get();
                accountCache.put(playerId, account);
                return CompletableFuture.completedFuture(account);
            } else {
                return createAccount(playerId);
            }
        });
    }

    @Override
    @NotNull
    public CompletableFuture<Account> createAccount(@NotNull UUID playerId) {
        // Try to load existing account first - this handles the race condition
        // by always attempting to load before creating
        return storage.loadAccount(playerId).thenCompose(optAccount -> {
            if (optAccount.isPresent()) {
                Account account = optAccount.get();
                accountCache.put(playerId, account);
                return CompletableFuture.completedFuture(account);
            }

            // Create new account with default balances
            Map<String, Double> balances = new HashMap<>();
            for (Currency currency : currencies.values()) {
                balances.put(currency.getIdentifier(), currency.getDefaultBalance());
            }

            SimpleAccount account = new SimpleAccount(playerId, playerId.toString(), balances, this);

            return storage.saveAccount(account).thenApply(v -> {
                accountCache.put(playerId, account);
                return account;
            });
        });
    }

    /**
     * Get or create an account with a specific name.
     *
     * @param playerId the player UUID
     * @param name the player name
     * @return future with the account
     */
    @NotNull
    public CompletableFuture<Account> getOrCreateAccount(@NotNull UUID playerId, @NotNull String name) {
        // Check cache first
        Account cached = accountCache.get(playerId);
        if (cached != null) {
            // Update name if different
            if (cached instanceof SimpleAccount simple && !simple.getName().equals(name)) {
                simple.setName(name);
                storage.saveAccount(simple);
            }
            return CompletableFuture.completedFuture(cached);
        }

        return storage.loadAccount(playerId).thenCompose(optAccount -> {
            if (optAccount.isPresent()) {
                Account account = optAccount.get();
                // Update name if different
                if (account instanceof SimpleAccount simple && !simple.getName().equals(name)) {
                    simple.setName(name);
                    storage.saveAccount(simple);
                }
                accountCache.put(playerId, account);
                return CompletableFuture.completedFuture(account);
            }

            // Create new account with default balances
            Map<String, Double> balances = new HashMap<>();
            for (Currency currency : currencies.values()) {
                balances.put(currency.getIdentifier(), currency.getDefaultBalance());
            }

            SimpleAccount account = new SimpleAccount(playerId, name, balances, this);

            return storage.saveAccount(account).thenApply(v -> {
                accountCache.put(playerId, account);
                return account;
            });
        });
    }

    @Override
    @NotNull
    public CompletableFuture<Boolean> deleteAccount(@NotNull UUID playerId) {
        accountCache.remove(playerId);
        return storage.deleteAccount(playerId);
    }

    // ==================== Quick Operations ====================

    @Override
    @NotNull
    public CompletableFuture<Double> getBalance(@NotNull UUID playerId) {
        return getAccount(playerId).thenCompose(Account::getBalance);
    }

    @Override
    @NotNull
    public CompletableFuture<Double> getBalance(@NotNull UUID playerId, @NotNull Currency currency) {
        return getAccount(playerId).thenCompose(acc -> acc.getBalance(currency));
    }

    @Override
    @NotNull
    public CompletableFuture<Boolean> has(@NotNull UUID playerId, double amount) {
        return getAccount(playerId).thenCompose(acc -> acc.has(amount));
    }

    @Override
    @NotNull
    public CompletableFuture<Boolean> has(@NotNull UUID playerId, @NotNull Currency currency, double amount) {
        return getAccount(playerId).thenCompose(acc -> acc.has(currency, amount));
    }

    @Override
    @NotNull
    public CompletableFuture<TransactionResult> deposit(@NotNull UUID playerId, double amount) {
        return getAccount(playerId).thenCompose(acc -> acc.deposit(amount));
    }

    @Override
    @NotNull
    public CompletableFuture<TransactionResult> deposit(@NotNull UUID playerId, @NotNull Currency currency, double amount) {
        return getAccount(playerId).thenCompose(acc -> acc.deposit(currency, amount));
    }

    @Override
    @NotNull
    public CompletableFuture<TransactionResult> withdraw(@NotNull UUID playerId, double amount) {
        return getAccount(playerId).thenCompose(acc -> acc.withdraw(amount));
    }

    @Override
    @NotNull
    public CompletableFuture<TransactionResult> withdraw(@NotNull UUID playerId, @NotNull Currency currency, double amount) {
        return getAccount(playerId).thenCompose(acc -> acc.withdraw(currency, amount));
    }

    @Override
    @NotNull
    public CompletableFuture<TransactionResult> setBalance(@NotNull UUID playerId, double amount) {
        return getAccount(playerId).thenCompose(acc -> acc.setBalance(amount));
    }

    @Override
    @NotNull
    public CompletableFuture<TransactionResult> setBalance(@NotNull UUID playerId, @NotNull Currency currency, double amount) {
        return getAccount(playerId).thenCompose(acc -> acc.setBalance(currency, amount));
    }

    @Override
    @NotNull
    public CompletableFuture<TransactionResult> transfer(@NotNull UUID from, @NotNull UUID to, double amount) {
        return transfer(from, to, getPrimaryCurrency(), amount);
    }

    @Override
    @NotNull
    public CompletableFuture<TransactionResult> transfer(@NotNull UUID from, @NotNull UUID to, @NotNull Currency currency, double amount) {
        return getAccount(from).thenCompose(fromAcc ->
            getAccount(to).thenCompose(toAcc ->
                fromAcc.transfer(toAcc, currency, amount)
            )
        );
    }

    // ==================== Internal ====================

    /**
     * Get the storage provider.
     */
    public StorageProvider getStorage() {
        return storage;
    }

    /**
     * Get the plugin instance.
     */
    public LedgerPlugin getPlugin() {
        return plugin;
    }

    /**
     * Invalidate account cache for a player.
     *
     * @param playerId the player UUID
     */
    public void invalidateCache(@NotNull UUID playerId) {
        accountCache.remove(playerId);
    }
}
