package dev.karmabyte.ledger.api.economy.transaction;

import dev.karmabyte.ledger.api.economy.Currency;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Result of an economy transaction.
 *
 * <p>Every economy operation returns a TransactionResult that indicates success
 * or failure, along with details about the transaction.
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * economy.withdraw(playerId, 100.0).thenAccept(result -> {
 *     if (result.isSuccess()) {
 *         player.sendMessage("Withdrew " + result.getAmountFormatted());
 *         player.sendMessage("New balance: " + result.getNewBalance());
 *     } else {
 *         player.sendMessage("Failed: " + result.getMessage());
 *     }
 * });
 * }</pre>
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public final class TransactionResult {

    /**
     * Status codes for transaction results.
     */
    public enum Status {
        /** Transaction completed successfully */
        SUCCESS,
        /** Account doesn't have enough funds */
        INSUFFICIENT_FUNDS,
        /** Account does not exist */
        ACCOUNT_NOT_FOUND,
        /** Currency does not exist */
        CURRENCY_NOT_FOUND,
        /** Amount was negative or zero */
        INVALID_AMOUNT,
        /** Transaction would exceed maximum balance */
        BALANCE_OVERFLOW,
        /** Transaction would go below minimum balance */
        BALANCE_UNDERFLOW,
        /** Transaction was cancelled by an event listener */
        CANCELLED,
        /** Generic provider error */
        PROVIDER_ERROR
    }

    private final Status status;
    private final TransactionType type;
    private final double amount;
    private final double previousBalance;
    private final double newBalance;
    private final Currency currency;
    private final UUID accountId;
    private final UUID targetId;
    private final String message;
    private final Instant timestamp;

    private TransactionResult(Builder builder) {
        this.status = builder.status;
        this.type = builder.type;
        this.amount = builder.amount;
        this.previousBalance = builder.previousBalance;
        this.newBalance = builder.newBalance;
        this.currency = builder.currency;
        this.accountId = builder.accountId;
        this.targetId = builder.targetId;
        this.message = builder.message;
        this.timestamp = Instant.now();
    }

    /**
     * Check if the transaction was successful.
     *
     * @return true if status is SUCCESS
     */
    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    /**
     * Check if the transaction failed.
     *
     * @return true if status is not SUCCESS
     */
    public boolean isFailure() {
        return status != Status.SUCCESS;
    }

    /**
     * Get the result status.
     *
     * @return the status
     */
    @NotNull
    public Status getStatus() {
        return status;
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
     * Get the transaction amount.
     *
     * @return the amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Get the formatted amount using the currency.
     *
     * @return formatted amount string
     */
    @NotNull
    public String getAmountFormatted() {
        return currency != null ? currency.format(amount) : String.valueOf(amount);
    }

    /**
     * Get the balance before the transaction.
     *
     * @return previous balance
     */
    public double getPreviousBalance() {
        return previousBalance;
    }

    /**
     * Get the balance after the transaction.
     *
     * @return new balance (same as previous if failed)
     */
    public double getNewBalance() {
        return newBalance;
    }

    /**
     * Get the currency used in the transaction.
     *
     * @return the currency, or null if not applicable
     */
    @Nullable
    public Currency getCurrency() {
        return currency;
    }

    /**
     * Get the account ID.
     *
     * @return the account UUID
     */
    @Nullable
    public UUID getAccountId() {
        return accountId;
    }

    /**
     * Get the target account ID (for transfers).
     *
     * @return optional target UUID
     */
    @NotNull
    public Optional<UUID> getTargetId() {
        return Optional.ofNullable(targetId);
    }

    /**
     * Get the human-readable message.
     *
     * @return the message
     */
    @NotNull
    public String getMessage() {
        return message != null ? message : status.name();
    }

    /**
     * Get the transaction timestamp.
     *
     * @return when the transaction occurred
     */
    @NotNull
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Create a success result.
     *
     * @param type the transaction type
     * @param amount the amount
     * @param previousBalance balance before
     * @param newBalance balance after
     * @param currency the currency
     * @return success result
     */
    @NotNull
    public static TransactionResult success(
            @NotNull TransactionType type,
            double amount,
            double previousBalance,
            double newBalance,
            @NotNull Currency currency) {
        return builder()
                .status(Status.SUCCESS)
                .type(type)
                .amount(amount)
                .previousBalance(previousBalance)
                .newBalance(newBalance)
                .currency(currency)
                .message("Transaction successful")
                .build();
    }

    /**
     * Create a failure result.
     *
     * @param status the failure status
     * @param message the error message
     * @return failure result
     */
    @NotNull
    public static TransactionResult failure(@NotNull Status status, @NotNull String message) {
        return builder()
                .status(status)
                .message(message)
                .build();
    }

    /**
     * Create a new builder.
     *
     * @return new builder instance
     */
    @NotNull
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for TransactionResult.
     */
    public static final class Builder {
        private Status status = Status.PROVIDER_ERROR;
        private TransactionType type = TransactionType.DEPOSIT;
        private double amount = 0;
        private double previousBalance = 0;
        private double newBalance = 0;
        private Currency currency;
        private UUID accountId;
        private UUID targetId;
        private String message;

        private Builder() {}

        public Builder status(@NotNull Status status) {
            this.status = status;
            return this;
        }

        public Builder type(@NotNull TransactionType type) {
            this.type = type;
            return this;
        }

        public Builder amount(double amount) {
            this.amount = amount;
            return this;
        }

        public Builder previousBalance(double balance) {
            this.previousBalance = balance;
            return this;
        }

        public Builder newBalance(double balance) {
            this.newBalance = balance;
            return this;
        }

        public Builder currency(@Nullable Currency currency) {
            this.currency = currency;
            return this;
        }

        public Builder accountId(@Nullable UUID accountId) {
            this.accountId = accountId;
            return this;
        }

        public Builder targetId(@Nullable UUID targetId) {
            this.targetId = targetId;
            return this;
        }

        public Builder message(@Nullable String message) {
            this.message = message;
            return this;
        }

        @NotNull
        public TransactionResult build() {
            return new TransactionResult(this);
        }
    }

    @Override
    public String toString() {
        return "TransactionResult{" +
                "status=" + status +
                ", type=" + type +
                ", amount=" + amount +
                ", newBalance=" + newBalance +
                ", message='" + message + '\'' +
                '}';
    }
}
