package dev.karmabyte.ledger.api.economy;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a currency in the economy system.
 *
 * <p>A currency defines how money is displayed and formatted. Each server must have
 * at least one primary currency, but can have multiple currencies (coins, gems, tokens, etc.).
 *
 * <h2>Example:</h2>
 * <pre>{@code
 * Currency coins = economy.getPrimaryCurrency();
 * System.out.println(coins.format(1234.56)); // "$1,234.56" or "1,234.56 Coins"
 * }</pre>
 *
 * @author KarmaByte
 * @since 1.0.0
 * @see CurrencyBuilder
 */
public interface Currency {

    /**
     * Get the unique identifier of this currency.
     *
     * <p>The identifier is used internally and in configuration files.
     * It should be lowercase and contain only alphanumeric characters and underscores.
     *
     * @return the currency identifier (e.g., "coins", "gems", "essence_of_life")
     */
    @NotNull
    String getIdentifier();

    /**
     * Get the display name of this currency.
     *
     * @return the display name (e.g., "Coins", "Gems")
     */
    @NotNull
    String getDisplayName();

    /**
     * Get the singular form of the currency name.
     *
     * <p>Used when the amount is exactly 1.
     *
     * @return singular form (e.g., "coin", "gem")
     */
    @NotNull
    String getNameSingular();

    /**
     * Get the plural form of the currency name.
     *
     * <p>Used when the amount is not 1.
     *
     * @return plural form (e.g., "coins", "gems")
     */
    @NotNull
    String getNamePlural();

    /**
     * Get the currency symbol.
     *
     * <p>This is the prefix/suffix used when formatting amounts.
     * Can be empty if the currency uses name-based formatting.
     *
     * @return the symbol (e.g., "$", "€", "G", "")
     */
    @NotNull
    String getSymbol();

    /**
     * Get the number of decimal places for this currency.
     *
     * <p>Use 0 for whole numbers only (tokens), 2 for cents/pence, etc.
     *
     * @return decimal places (0-8)
     */
    int getDecimalPlaces();

    /**
     * Get the default/starting balance for new accounts in this currency.
     *
     * @return the starting balance
     */
    double getDefaultBalance();

    /**
     * Get the minimum balance allowed for this currency.
     *
     * <p>A negative value allows overdraft/loans.
     * Zero (default) means no negative balances.
     *
     * @return the minimum balance (can be negative)
     */
    double getMinBalance();

    /**
     * Get the maximum balance allowed for this currency.
     *
     * <p>Use {@link Double#MAX_VALUE} for no limit.
     *
     * @return the maximum balance
     */
    double getMaxBalance();

    /**
     * Format an amount of this currency for display.
     *
     * <p>Examples:
     * <ul>
     *   <li>Symbol-prefix: "$1,234.56"</li>
     *   <li>Symbol-suffix: "1,234.56€"</li>
     *   <li>Name-based: "1,234 Coins"</li>
     * </ul>
     *
     * @param amount the amount to format
     * @return the formatted string
     */
    @NotNull
    String format(double amount);

    /**
     * Check if this is the server's primary currency.
     *
     * <p>The primary currency is used when no currency is specified in operations.
     *
     * @return true if this is the primary currency
     */
    boolean isPrimary();

    /**
     * Check if this currency is backed by a physical item.
     *
     * <p>Item-backed currencies (like Essence of Life) can be converted to/from
     * physical items in the player's inventory.
     *
     * @return true if item-backed
     */
    default boolean isItemBacked() {
        return false;
    }

    /**
     * Get the item type for item-backed currencies.
     *
     * @return the item type identifier (e.g., "hytale:essence_of_life"), or empty string
     */
    @NotNull
    default String getItemType() {
        return "";
    }
}
