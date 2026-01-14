package dev.karmabyte.ledger.api.economy;

import org.jetbrains.annotations.NotNull;

/**
 * Builder for creating {@link Currency} instances.
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * Currency coins = CurrencyBuilder.create("coins")
 *     .displayName("Coins")
 *     .singular("coin")
 *     .plural("coins")
 *     .symbol("$")
 *     .decimals(2)
 *     .defaultBalance(100.0)
 *     .primary(true)
 *     .build();
 * }</pre>
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public interface CurrencyBuilder {

    /**
     * Create a new currency builder with the given identifier.
     *
     * @param identifier the unique currency identifier
     * @return new builder instance
     */
    @NotNull
    static CurrencyBuilder create(@NotNull String identifier) {
        return new CurrencyBuilder() {
            private String displayName = identifier;
            private String singular = identifier;
            private String plural = identifier + "s";
            private String symbol = "";
            private int decimals = 2;
            private double defaultBalance = 0.0;
            private double minBalance = 0.0;
            private double maxBalance = Double.MAX_VALUE;
            private boolean primary = false;
            private boolean itemBacked = false;
            private String itemType = "";

            @Override
            public @NotNull CurrencyBuilder displayName(@NotNull String name) {
                this.displayName = name;
                return this;
            }

            @Override
            public @NotNull CurrencyBuilder singular(@NotNull String name) {
                this.singular = name;
                return this;
            }

            @Override
            public @NotNull CurrencyBuilder plural(@NotNull String name) {
                this.plural = name;
                return this;
            }

            @Override
            public @NotNull CurrencyBuilder symbol(@NotNull String symbol) {
                this.symbol = symbol;
                return this;
            }

            @Override
            public @NotNull CurrencyBuilder decimals(int places) {
                if (places < 0 || places > 8) {
                    throw new IllegalArgumentException("Decimal places must be between 0 and 8");
                }
                this.decimals = places;
                return this;
            }

            @Override
            public @NotNull CurrencyBuilder defaultBalance(double balance) {
                this.defaultBalance = balance;
                return this;
            }

            @Override
            public @NotNull CurrencyBuilder minBalance(double min) {
                this.minBalance = min;
                return this;
            }

            @Override
            public @NotNull CurrencyBuilder maxBalance(double max) {
                this.maxBalance = max;
                return this;
            }

            @Override
            public @NotNull CurrencyBuilder primary(boolean primary) {
                this.primary = primary;
                return this;
            }

            @Override
            public @NotNull CurrencyBuilder itemBacked(boolean itemBacked) {
                this.itemBacked = itemBacked;
                return this;
            }

            @Override
            public @NotNull CurrencyBuilder itemType(@NotNull String type) {
                this.itemType = type;
                this.itemBacked = !type.isEmpty();
                return this;
            }

            @Override
            public @NotNull Currency build() {
                final String id = identifier;
                final String dn = displayName;
                final String sing = singular;
                final String plur = plural;
                final String sym = symbol;
                final int dec = decimals;
                final double def = defaultBalance;
                final double min = minBalance;
                final double max = maxBalance;
                final boolean prim = primary;
                final boolean ib = itemBacked;
                final String it = itemType;

                return new Currency() {
                    @Override
                    public @NotNull String getIdentifier() { return id; }

                    @Override
                    public @NotNull String getDisplayName() { return dn; }

                    @Override
                    public @NotNull String getNameSingular() { return sing; }

                    @Override
                    public @NotNull String getNamePlural() { return plur; }

                    @Override
                    public @NotNull String getSymbol() { return sym; }

                    @Override
                    public int getDecimalPlaces() { return dec; }

                    @Override
                    public double getDefaultBalance() { return def; }

                    @Override
                    public double getMinBalance() { return min; }

                    @Override
                    public double getMaxBalance() { return max; }

                    @Override
                    public @NotNull String format(double amount) {
                        String formatted;
                        if (dec == 0) {
                            formatted = String.format("%,.0f", amount);
                        } else {
                            formatted = String.format("%,." + dec + "f", amount);
                        }

                        if (!sym.isEmpty()) {
                            return sym + formatted;
                        } else {
                            String name = Math.abs(amount) == 1 ? sing : plur;
                            return formatted + " " + name;
                        }
                    }

                    @Override
                    public boolean isPrimary() { return prim; }

                    @Override
                    public boolean isItemBacked() { return ib; }

                    @Override
                    public @NotNull String getItemType() { return it; }
                };
            }
        };
    }

    /**
     * Set the display name of the currency.
     *
     * @param name the display name
     * @return this builder
     */
    @NotNull
    CurrencyBuilder displayName(@NotNull String name);

    /**
     * Set the singular form of the currency name.
     *
     * @param name singular name (e.g., "coin")
     * @return this builder
     */
    @NotNull
    CurrencyBuilder singular(@NotNull String name);

    /**
     * Set the plural form of the currency name.
     *
     * @param name plural name (e.g., "coins")
     * @return this builder
     */
    @NotNull
    CurrencyBuilder plural(@NotNull String name);

    /**
     * Set the currency symbol.
     *
     * @param symbol the symbol (e.g., "$", "€")
     * @return this builder
     */
    @NotNull
    CurrencyBuilder symbol(@NotNull String symbol);

    /**
     * Set the number of decimal places.
     *
     * @param places decimal places (0-8)
     * @return this builder
     */
    @NotNull
    CurrencyBuilder decimals(int places);

    /**
     * Set the default/starting balance.
     *
     * @param balance the starting balance
     * @return this builder
     */
    @NotNull
    CurrencyBuilder defaultBalance(double balance);

    /**
     * Set the minimum balance allowed.
     *
     * @param min the minimum balance (can be negative for overdraft)
     * @return this builder
     */
    @NotNull
    CurrencyBuilder minBalance(double min);

    /**
     * Set the maximum balance allowed.
     *
     * @param max the maximum balance
     * @return this builder
     */
    @NotNull
    CurrencyBuilder maxBalance(double max);

    /**
     * Set whether this is the primary currency.
     *
     * @param primary true if primary
     * @return this builder
     */
    @NotNull
    CurrencyBuilder primary(boolean primary);

    /**
     * Set whether this currency is backed by items.
     *
     * @param itemBacked true if item-backed
     * @return this builder
     */
    @NotNull
    CurrencyBuilder itemBacked(boolean itemBacked);

    /**
     * Set the item type for item-backed currencies.
     *
     * @param type the item type identifier
     * @return this builder
     */
    @NotNull
    CurrencyBuilder itemType(@NotNull String type);

    /**
     * Build the currency instance.
     *
     * @return the built currency
     */
    @NotNull
    Currency build();
}
