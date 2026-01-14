package dev.karmabyte.ledger.api.economy.transaction;

import dev.karmabyte.ledger.api.economy.Currency;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Immutable record of an economy transaction.
 *
 * <p>Used for transaction history and logging.
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public final class Transaction {

    private final UUID id;
    private final TransactionType type;
    private final UUID accountId;
    private final UUID targetId;
    private final String currencyId;
    private final double amount;
    private final double balanceBefore;
    private final double balanceAfter;
    private final String reason;
    private final Instant timestamp;

    private Transaction(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.type = builder.type;
        this.accountId = builder.accountId;
        this.targetId = builder.targetId;
        this.currencyId = builder.currencyId;
        this.amount = builder.amount;
        this.balanceBefore = builder.balanceBefore;
        this.balanceAfter = builder.balanceAfter;
        this.reason = builder.reason;
        this.timestamp = builder.timestamp != null ? builder.timestamp : Instant.now();
    }

    /**
     * Get the unique transaction ID.
     *
     * @return transaction UUID
     */
    @NotNull
    public UUID getId() {
        return id;
    }

    /**
     * Get the transaction type.
     *
     * @return the type
     */
    @NotNull
    public TransactionType getType() {
        return type;
    }

    /**
     * Get the primary account involved.
     *
     * @return account UUID
     */
    @NotNull
    public UUID getAccountId() {
        return accountId;
    }

    /**
     * Get the target account (for transfers).
     *
     * @return optional target UUID
     */
    @NotNull
    public Optional<UUID> getTargetId() {
        return Optional.ofNullable(targetId);
    }

    /**
     * Get the currency identifier.
     *
     * @return currency ID
     */
    @NotNull
    public String getCurrencyId() {
        return currencyId;
    }

    /**
     * Get the transaction amount.
     *
     * @return the amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Get the balance before the transaction.
     *
     * @return balance before
     */
    public double getBalanceBefore() {
        return balanceBefore;
    }

    /**
     * Get the balance after the transaction.
     *
     * @return balance after
     */
    public double getBalanceAfter() {
        return balanceAfter;
    }

    /**
     * Get the transaction reason/description.
     *
     * @return optional reason
     */
    @NotNull
    public Optional<String> getReason() {
        return Optional.ofNullable(reason);
    }

    /**
     * Get when the transaction occurred.
     *
     * @return the timestamp
     */
    @NotNull
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Create a new builder.
     *
     * @return new builder
     */
    @NotNull
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a transaction from a result.
     *
     * @param result the transaction result
     * @return new transaction record
     */
    @NotNull
    public static Transaction fromResult(@NotNull TransactionResult result) {
        return builder()
                .type(result.getType())
                .accountId(result.getAccountId())
                .targetId(result.getTargetId().orElse(null))
                .currencyId(result.getCurrency() != null ? result.getCurrency().getIdentifier() : "unknown")
                .amount(result.getAmount())
                .balanceBefore(result.getPreviousBalance())
                .balanceAfter(result.getNewBalance())
                .build();
    }

    /**
     * Builder for Transaction.
     */
    public static final class Builder {
        private UUID id;
        private TransactionType type = TransactionType.DEPOSIT;
        private UUID accountId;
        private UUID targetId;
        private String currencyId = "default";
        private double amount;
        private double balanceBefore;
        private double balanceAfter;
        private String reason;
        private Instant timestamp;

        private Builder() {}

        public Builder id(@NotNull UUID id) {
            this.id = id;
            return this;
        }

        public Builder type(@NotNull TransactionType type) {
            this.type = type;
            return this;
        }

        public Builder accountId(@NotNull UUID accountId) {
            this.accountId = accountId;
            return this;
        }

        public Builder targetId(@Nullable UUID targetId) {
            this.targetId = targetId;
            return this;
        }

        public Builder currencyId(@NotNull String currencyId) {
            this.currencyId = currencyId;
            return this;
        }

        public Builder currency(@NotNull Currency currency) {
            this.currencyId = currency.getIdentifier();
            return this;
        }

        public Builder amount(double amount) {
            this.amount = amount;
            return this;
        }

        public Builder balanceBefore(double balance) {
            this.balanceBefore = balance;
            return this;
        }

        public Builder balanceAfter(double balance) {
            this.balanceAfter = balance;
            return this;
        }

        public Builder reason(@Nullable String reason) {
            this.reason = reason;
            return this;
        }

        public Builder timestamp(@NotNull Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        @NotNull
        public Transaction build() {
            if (accountId == null) {
                throw new IllegalStateException("accountId is required");
            }
            return new Transaction(this);
        }
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", type=" + type +
                ", accountId=" + accountId +
                ", amount=" + amount +
                ", timestamp=" + timestamp +
                '}';
    }
}
