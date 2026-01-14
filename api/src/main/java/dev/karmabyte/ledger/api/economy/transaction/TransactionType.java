package dev.karmabyte.ledger.api.economy.transaction;

/**
 * Types of economy transactions.
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public enum TransactionType {

    /**
     * Money added to an account.
     */
    DEPOSIT,

    /**
     * Money removed from an account.
     */
    WITHDRAW,

    /**
     * Money moved between accounts.
     */
    TRANSFER,

    /**
     * Balance set directly (admin operation).
     */
    SET,

    /**
     * Account reset to default values.
     */
    RESET
}
