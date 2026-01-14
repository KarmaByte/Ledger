package dev.karmabyte.ledger.core.economy;

import dev.karmabyte.ledger.api.economy.Account;
import dev.karmabyte.ledger.api.economy.Currency;
import dev.karmabyte.ledger.api.economy.transaction.TransactionResult;
import dev.karmabyte.ledger.api.economy.transaction.TransactionType;
import dev.karmabyte.ledger.api.event.economy.PreTransactionEvent;
import dev.karmabyte.ledger.core.LedgerPlugin;
import dev.karmabyte.ledger.core.event.LedgerEventManager;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of the Account interface.
 *
 * <p>This implementation fires events for all transactions, allowing
 * other plugins to listen and react to economy changes.
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public class SimpleAccount implements Account {

    private final UUID owner;
    private String name;
    private final Map<String, Double> balances;
    private final SimpleEconomyService economyService;

    public SimpleAccount(UUID owner, String name, Map<String, Double> balances, SimpleEconomyService economyService) {
        this.owner = owner;
        this.name = name;
        this.balances = new ConcurrentHashMap<>(balances);
        this.economyService = economyService;
    }

    @Override
    @NotNull
    public UUID getOwner() {
        return owner;
    }

    @Override
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Update the account name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    @NotNull
    public CompletableFuture<Double> getBalance(@NotNull Currency currency) {
        return CompletableFuture.completedFuture(
            balances.getOrDefault(currency.getIdentifier(), currency.getDefaultBalance())
        );
    }

    @Override
    @NotNull
    public CompletableFuture<Double> getBalance() {
        return getBalance(economyService.getPrimaryCurrency());
    }

    @Override
    @NotNull
    public CompletableFuture<Map<Currency, Double>> getAllBalances() {
        Map<Currency, Double> result = new HashMap<>();
        for (Currency currency : economyService.getCurrencies()) {
            double balance = balances.getOrDefault(currency.getIdentifier(), currency.getDefaultBalance());
            result.put(currency, balance);
        }
        return CompletableFuture.completedFuture(result);
    }

    @Override
    @NotNull
    public CompletableFuture<Boolean> has(@NotNull Currency currency, double amount) {
        return getBalance(currency).thenApply(balance -> balance >= amount);
    }

    @Override
    @NotNull
    public CompletableFuture<Boolean> has(double amount) {
        return has(economyService.getPrimaryCurrency(), amount);
    }

    @Override
    @NotNull
    public CompletableFuture<TransactionResult> deposit(@NotNull Currency currency, double amount) {
        return processTransaction(currency, amount, TransactionType.DEPOSIT);
    }

    @Override
    @NotNull
    public CompletableFuture<TransactionResult> deposit(double amount) {
        return deposit(economyService.getPrimaryCurrency(), amount);
    }

    @Override
    @NotNull
    public CompletableFuture<TransactionResult> withdraw(@NotNull Currency currency, double amount) {
        return processTransaction(currency, amount, TransactionType.WITHDRAW);
    }

    @Override
    @NotNull
    public CompletableFuture<TransactionResult> withdraw(double amount) {
        return withdraw(economyService.getPrimaryCurrency(), amount);
    }

    @Override
    @NotNull
    public CompletableFuture<TransactionResult> setBalance(@NotNull Currency currency, double amount) {
        return CompletableFuture.supplyAsync(() -> {
            LedgerEventManager eventManager = getEventManager();
            double currentBalance = balances.getOrDefault(currency.getIdentifier(), 0.0);

            // Fire PreTransactionEvent
            if (eventManager != null) {
                PreTransactionEvent preEvent = eventManager.firePreTransaction(
                    this, currency, TransactionType.SET, amount
                );
                if (preEvent.isCancelled()) {
                    return TransactionResult.failure(
                        TransactionResult.Status.CANCELLED,
                        preEvent.getCancelReason() != null ? preEvent.getCancelReason() : "Transaction cancelled"
                    );
                }
            }

            // Check bounds
            if (amount > currency.getMaxBalance()) {
                return TransactionResult.failure(
                    TransactionResult.Status.BALANCE_OVERFLOW,
                    "Amount exceeds maximum balance of " + currency.format(currency.getMaxBalance())
                );
            }
            if (amount < currency.getMinBalance()) {
                return TransactionResult.failure(
                    TransactionResult.Status.BALANCE_UNDERFLOW,
                    "Amount is below minimum balance of " + currency.format(currency.getMinBalance())
                );
            }

            // Update balance
            double newBalance = round(amount, currency);
            balances.put(currency.getIdentifier(), newBalance);

            // Save to storage
            economyService.getStorage().saveAccount(this);

            TransactionResult result = TransactionResult.success(
                TransactionType.SET,
                amount,
                currentBalance,
                newBalance,
                currency
            );

            // Fire PostTransactionEvent and BalanceChangeEvent
            if (eventManager != null) {
                eventManager.firePostTransaction(this, currency, TransactionType.SET, result);
                eventManager.fireBalanceChange(this, currency, TransactionType.SET, currentBalance, newBalance);
            }

            // Log transaction
            logTransaction(TransactionType.SET, currency, amount, currentBalance, newBalance, null);

            return result;
        });
    }

    @Override
    @NotNull
    public CompletableFuture<TransactionResult> setBalance(double amount) {
        return setBalance(economyService.getPrimaryCurrency(), amount);
    }

    @Override
    @NotNull
    public CompletableFuture<TransactionResult> transfer(@NotNull Account target, @NotNull Currency currency, double amount) {
        return CompletableFuture.supplyAsync(() -> {
            LedgerEventManager eventManager = getEventManager();

            // Validate amount
            if (amount <= 0) {
                return TransactionResult.failure(
                    TransactionResult.Status.INVALID_AMOUNT,
                    "Transfer amount must be positive"
                );
            }

            double currentBalance = balances.getOrDefault(currency.getIdentifier(), 0.0);

            // Fire PreTransactionEvent for sender
            double finalAmount = amount;
            if (eventManager != null) {
                PreTransactionEvent preEvent = eventManager.firePreTransaction(
                    this, currency, TransactionType.TRANSFER, amount
                );
                if (preEvent.isCancelled()) {
                    return TransactionResult.failure(
                        TransactionResult.Status.CANCELLED,
                        preEvent.getCancelReason() != null ? preEvent.getCancelReason() : "Transaction cancelled"
                    );
                }
                // Allow event listeners to modify the amount
                finalAmount = preEvent.getAmount();
            }

            // Check sufficient funds (considering minBalance)
            double effectiveMin = Math.max(0, currency.getMinBalance());
            if (currentBalance - finalAmount < effectiveMin) {
                return TransactionResult.failure(
                    TransactionResult.Status.INSUFFICIENT_FUNDS,
                    "Insufficient funds. You have " + currency.format(currentBalance)
                );
            }

            // Withdraw from sender
            double newBalance = round(currentBalance - finalAmount, currency);
            balances.put(currency.getIdentifier(), newBalance);

            // Deposit to receiver
            double targetPreviousBalance = 0;
            double newTargetBalance = 0;
            if (target instanceof SimpleAccount simpleTarget) {
                targetPreviousBalance = simpleTarget.balances.getOrDefault(currency.getIdentifier(), 0.0);
                newTargetBalance = round(targetPreviousBalance + finalAmount, currency);

                // Check max balance for target
                if (newTargetBalance > currency.getMaxBalance()) {
                    // Rollback sender
                    balances.put(currency.getIdentifier(), currentBalance);
                    return TransactionResult.failure(
                        TransactionResult.Status.BALANCE_OVERFLOW,
                        "Recipient would exceed maximum balance"
                    );
                }

                simpleTarget.balances.put(currency.getIdentifier(), newTargetBalance);
                economyService.getStorage().saveAccount(simpleTarget);

                // Fire balance change event for receiver
                if (eventManager != null) {
                    eventManager.fireBalanceChange(simpleTarget, currency, TransactionType.TRANSFER,
                        targetPreviousBalance, newTargetBalance);
                }
            }

            // Save sender
            economyService.getStorage().saveAccount(this);

            TransactionResult result = TransactionResult.builder()
                .status(TransactionResult.Status.SUCCESS)
                .type(TransactionType.TRANSFER)
                .amount(finalAmount)
                .previousBalance(currentBalance)
                .newBalance(newBalance)
                .currency(currency)
                .accountId(owner)
                .targetId(target.getOwner())
                .message("Transfer successful")
                .build();

            // Fire PostTransactionEvent and BalanceChangeEvent for sender
            if (eventManager != null) {
                eventManager.firePostTransaction(this, currency, TransactionType.TRANSFER, result);
                eventManager.fireBalanceChange(this, currency, TransactionType.TRANSFER, currentBalance, newBalance);
            }

            // Log transaction for both parties
            logTransaction(TransactionType.TRANSFER, currency, finalAmount, currentBalance, newBalance, target.getOwner());

            // Notify receiver about the payment
            notifyPaymentReceived(target, currency, finalAmount);

            return result;
        });
    }

    @Override
    @NotNull
    public CompletableFuture<TransactionResult> transfer(@NotNull Account target, double amount) {
        return transfer(target, economyService.getPrimaryCurrency(), amount);
    }

    @Override
    @NotNull
    public CompletableFuture<TransactionResult> reset() {
        return CompletableFuture.supplyAsync(() -> {
            LedgerEventManager eventManager = getEventManager();
            Currency primaryCurrency = economyService.getPrimaryCurrency();

            // Fire PreTransactionEvent
            if (eventManager != null) {
                PreTransactionEvent preEvent = eventManager.firePreTransaction(
                    this, primaryCurrency, TransactionType.RESET, 0
                );
                if (preEvent.isCancelled()) {
                    return TransactionResult.failure(
                        TransactionResult.Status.CANCELLED,
                        preEvent.getCancelReason() != null ? preEvent.getCancelReason() : "Reset cancelled"
                    );
                }
            }

            // Reset all balances to defaults
            for (Currency currency : economyService.getCurrencies()) {
                double previousBalance = balances.getOrDefault(currency.getIdentifier(), 0.0);
                double newBalance = currency.getDefaultBalance();
                balances.put(currency.getIdentifier(), newBalance);

                // Fire balance change for each currency
                if (eventManager != null && previousBalance != newBalance) {
                    eventManager.fireBalanceChange(this, currency, TransactionType.RESET,
                        previousBalance, newBalance);
                }
            }

            // Save to storage
            economyService.getStorage().saveAccount(this);

            TransactionResult result = TransactionResult.builder()
                .status(TransactionResult.Status.SUCCESS)
                .type(TransactionType.RESET)
                .amount(0)
                .message("Account reset to default balances")
                .build();

            // Fire PostTransactionEvent
            if (eventManager != null) {
                eventManager.firePostTransaction(this, primaryCurrency, TransactionType.RESET, result);
            }

            return result;
        });
    }

    // ==================== Internal ====================

    private CompletableFuture<TransactionResult> processTransaction(Currency currency, double amount, TransactionType type) {
        return CompletableFuture.supplyAsync(() -> {
            LedgerEventManager eventManager = getEventManager();

            // Validate amount
            if (amount <= 0) {
                return TransactionResult.failure(
                    TransactionResult.Status.INVALID_AMOUNT,
                    "Amount must be positive"
                );
            }

            double currentBalance = balances.getOrDefault(currency.getIdentifier(), 0.0);

            // Fire PreTransactionEvent
            double finalAmount = amount;
            if (eventManager != null) {
                PreTransactionEvent preEvent = eventManager.firePreTransaction(this, currency, type, amount);
                if (preEvent.isCancelled()) {
                    return TransactionResult.failure(
                        TransactionResult.Status.CANCELLED,
                        preEvent.getCancelReason() != null ? preEvent.getCancelReason() : "Transaction cancelled"
                    );
                }
                // Allow event listeners to modify the amount (for taxes, bonuses, etc.)
                finalAmount = preEvent.getAmount();
            }

            double newBalance;

            if (type == TransactionType.DEPOSIT) {
                newBalance = currentBalance + finalAmount;

                // Check max balance
                if (newBalance > currency.getMaxBalance()) {
                    return TransactionResult.failure(
                        TransactionResult.Status.BALANCE_OVERFLOW,
                        "Would exceed maximum balance of " + currency.format(currency.getMaxBalance())
                    );
                }
            } else { // WITHDRAW
                newBalance = currentBalance - finalAmount;

                // Check min balance
                if (newBalance < currency.getMinBalance()) {
                    return TransactionResult.failure(
                        TransactionResult.Status.INSUFFICIENT_FUNDS,
                        "Insufficient funds. You have " + currency.format(currentBalance)
                    );
                }
            }

            // Round to decimal places
            newBalance = round(newBalance, currency);

            // Update balance
            balances.put(currency.getIdentifier(), newBalance);

            // Save to storage
            economyService.getStorage().saveAccount(this);

            TransactionResult result = TransactionResult.success(type, finalAmount, currentBalance, newBalance, currency);

            // Fire PostTransactionEvent and BalanceChangeEvent
            if (eventManager != null) {
                eventManager.firePostTransaction(this, currency, type, result);
                eventManager.fireBalanceChange(this, currency, type, currentBalance, newBalance);
            }

            // Log transaction
            logTransaction(type, currency, finalAmount, currentBalance, newBalance, null);

            return result;
        });
    }

    private double round(double value, Currency currency) {
        double multiplier = Math.pow(10, currency.getDecimalPlaces());
        return Math.round(value * multiplier) / multiplier;
    }

    /**
     * Get the event manager from the plugin, if available.
     */
    private LedgerEventManager getEventManager() {
        LedgerPlugin plugin = LedgerPlugin.getInstance();
        return plugin != null ? plugin.getEventManager() : null;
    }

    /**
     * Log a transaction to storage.
     */
    private void logTransaction(TransactionType type, Currency currency, double amount,
                                double previousBalance, double newBalance, UUID targetId) {
        LedgerPlugin plugin = LedgerPlugin.getInstance();
        if (plugin != null && plugin.getTransactionLogger() != null) {
            plugin.getTransactionLogger().log(
                owner, targetId, type, currency.getIdentifier(),
                amount, previousBalance, newBalance
            );
        }
    }

    /**
     * Notify a player that they received a payment.
     */
    private void notifyPaymentReceived(Account receiver, Currency currency, double amount) {
        LedgerPlugin plugin = LedgerPlugin.getInstance();
        if (plugin != null) {
            plugin.notifyPaymentReceived(receiver.getOwner(), this.name, currency.format(amount));
        }
    }

    /**
     * Get the raw balances map (for serialization).
     *
     * @return the balances map
     */
    public Map<String, Double> getBalancesMap() {
        return new HashMap<>(balances);
    }
}
