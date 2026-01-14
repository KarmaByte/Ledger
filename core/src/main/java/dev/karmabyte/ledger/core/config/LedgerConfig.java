package dev.karmabyte.ledger.core.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Main configuration for Ledger.
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public class LedgerConfig {

    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create();

    // Storage settings
    private String storageType = "json";

    // Currency settings
    private String currencyId = "coins";
    private String currencyDisplayName = "Coins";
    private String currencySingular = "coin";
    private String currencyPlural = "coins";
    private String currencySymbol = "$";
    private int currencyDecimals = 2;
    private double startingBalance = 100.0;
    private double minBalance = 0.0;
    private double maxBalance = 1_000_000_000.0;

    // Transaction settings
    private double minPayAmount = 0.01;
    private double maxPayAmount = 1_000_000.0;
    private boolean allowPaySelf = false;
    private int payCooldownSeconds = 0;

    // Baltop settings
    private int baltopSize = 10;
    private int baltopCacheMinutes = 5;

    // Cache settings
    private int cacheExpireMinutes = 30;

    // Transaction logging
    private boolean transactionLogging = true;

    // Debug
    private boolean debug = false;

    /**
     * Load or create config from file.
     *
     * @param file the config file
     */
    public LedgerConfig(File file) throws IOException {
        if (file.exists()) {
            try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                LedgerConfig loaded = GSON.fromJson(reader, LedgerConfig.class);
                if (loaded != null) {
                    copyFrom(loaded);
                }
            }
        }
        // Always save to ensure new options are written
        save(file);
    }

    /**
     * Save config to file.
     *
     * @param file the config file
     */
    public void save(File file) throws IOException {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            GSON.toJson(this, writer);
        }
    }

    private void copyFrom(LedgerConfig other) {
        this.storageType = other.storageType;
        this.currencyId = other.currencyId;
        this.currencyDisplayName = other.currencyDisplayName;
        this.currencySingular = other.currencySingular;
        this.currencyPlural = other.currencyPlural;
        this.currencySymbol = other.currencySymbol;
        this.currencyDecimals = other.currencyDecimals;
        this.startingBalance = other.startingBalance;
        this.minBalance = other.minBalance;
        this.maxBalance = other.maxBalance;
        this.minPayAmount = other.minPayAmount;
        this.maxPayAmount = other.maxPayAmount;
        this.allowPaySelf = other.allowPaySelf;
        this.payCooldownSeconds = other.payCooldownSeconds;
        this.baltopSize = other.baltopSize;
        this.baltopCacheMinutes = other.baltopCacheMinutes;
        this.cacheExpireMinutes = other.cacheExpireMinutes;
        this.transactionLogging = other.transactionLogging;
        this.debug = other.debug;
    }

    // Getters

    public String getStorageType() { return storageType; }
    public String getCurrencyId() { return currencyId; }
    public String getCurrencyDisplayName() { return currencyDisplayName; }
    public String getCurrencySingular() { return currencySingular; }
    public String getCurrencyPlural() { return currencyPlural; }
    public String getCurrencySymbol() { return currencySymbol; }
    public int getCurrencyDecimals() { return currencyDecimals; }
    public double getStartingBalance() { return startingBalance; }
    public double getMinBalance() { return minBalance; }
    public double getMaxBalance() { return maxBalance; }
    public double getMinPayAmount() { return minPayAmount; }
    public double getMaxPayAmount() { return maxPayAmount; }
    public boolean isAllowPaySelf() { return allowPaySelf; }
    public int getPayCooldownSeconds() { return payCooldownSeconds; }
    public int getBaltopSize() { return baltopSize; }
    public int getBaltopCacheMinutes() { return baltopCacheMinutes; }
    public int getCacheExpireMinutes() { return cacheExpireMinutes; }
    public boolean isTransactionLoggingEnabled() { return transactionLogging; }
    public boolean isDebug() { return debug; }
}
