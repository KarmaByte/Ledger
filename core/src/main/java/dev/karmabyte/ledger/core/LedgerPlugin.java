package dev.karmabyte.ledger.core;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import dev.karmabyte.ledger.api.Ledger;
import dev.karmabyte.ledger.api.economy.Currency;
import dev.karmabyte.ledger.api.economy.CurrencyBuilder;
import dev.karmabyte.ledger.core.config.LedgerConfig;
import dev.karmabyte.ledger.core.config.MessagesConfig;
import dev.karmabyte.ledger.core.economy.SimpleEconomyService;
import dev.karmabyte.ledger.core.event.LedgerEventManager;
import dev.karmabyte.ledger.core.logging.TransactionLogger;
import dev.karmabyte.ledger.core.command.BalanceCommand;
import dev.karmabyte.ledger.core.command.BalTopCommand;
import dev.karmabyte.ledger.core.command.EcoCommand;
import dev.karmabyte.ledger.core.command.LedgerCommand;
import dev.karmabyte.ledger.core.command.PayCommand;
import dev.karmabyte.ledger.core.storage.StorageProvider;
import dev.karmabyte.ledger.core.storage.json.JsonStorageProvider;
import dev.karmabyte.ledger.core.storage.sqlite.SqliteStorageProvider;
import dev.karmabyte.ledger.core.util.Messages;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Main plugin class for Ledger economy system.
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public class LedgerPlugin extends JavaPlugin {

    private static final String VERSION = "1.0.0-SNAPSHOT";
    private static LedgerPlugin instance;

    private LedgerConfig config;
    private MessagesConfig messages;
    private StorageProvider storage;
    private SimpleEconomyService economyService;
    private LedgerProviderImpl provider;
    private LedgerEventManager eventManager;
    private TransactionLogger transactionLogger;
    private final List<EventRegistration<?, ?>> eventRegistrations = new ArrayList<>();

    public LedgerPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup() {
        getLogger().atInfo().log("===========================================");
        getLogger().atInfo().log("  Ledger v%s", VERSION);
        getLogger().atInfo().log("  Economy API for Hytale");
        getLogger().atInfo().log("  Author: KarmaByte");
        getLogger().atInfo().log("===========================================");

        // Load configuration
        if (!loadConfiguration()) {
            getLogger().atSevere().log("Failed to load configuration! Disabling plugin.");
            return;
        }

        // Initialize storage
        if (!initializeStorage()) {
            getLogger().atSevere().log("Failed to initialize storage! Disabling plugin.");
            return;
        }

        // Initialize transaction logger
        initializeTransactionLogger();

        // Initialize event manager
        eventManager = new LedgerEventManager(this);

        // Initialize economy service
        economyService = new SimpleEconomyService(this, storage);

        // Register default currency
        registerDefaultCurrency();

        // Create and register provider
        provider = new LedgerProviderImpl(this, economyService);

        // Register event listeners
        registerListeners();

        // Register commands
        registerCommands();

        getLogger().atInfo().log("Ledger setup complete.");
    }

    @Override
    protected void start() {
        // Register the provider globally when dependencies are ready
        Ledger.registerProvider(provider);

        getLogger().atInfo().log("Ledger enabled successfully!");
        getLogger().atInfo().log("Economy provider: %s", provider.getName());
        getLogger().atInfo().log("Storage: %s", storage.getType());
        getLogger().atInfo().log("Transaction logging: %s", transactionLogger != null && transactionLogger.isEnabled() ? "enabled" : "disabled");
    }

    @Override
    protected void shutdown() {
        // Unregister events
        eventRegistrations.forEach(EventRegistration::unregister);
        eventRegistrations.clear();

        // Unregister provider
        if (provider != null) {
            provider.onDisable();
            Ledger.unregisterProvider();
        }

        // Close transaction logger
        if (transactionLogger != null) {
            transactionLogger.close();
        }

        // Close storage
        if (storage != null) {
            try {
                storage.close();
            } catch (Exception e) {
                getLogger().atSevere().log("Error closing storage: %s", e.getMessage());
            }
        }

        instance = null;
        getLogger().atInfo().log("Ledger disabled.");
    }

    private boolean loadConfiguration() {
        try {
            // Create data folder if needed
            File dataFolder = getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            // Load configs
            config = new LedgerConfig(new File(dataFolder, "config.json"));
            messages = new MessagesConfig(new File(dataFolder, "messages.json"));

            return true;
        } catch (Exception e) {
            getLogger().atSevere().log("Failed to load configuration: %s", e.getMessage());
            return false;
        }
    }

    private boolean initializeStorage() {
        try {
            String storageType = config.getStorageType().toLowerCase();
            File dataFolder = getDataFolder();

            storage = switch (storageType) {
                case "json", "flatfile" -> new JsonStorageProvider(
                    new File(dataFolder, "data")
                );
                case "sqlite" -> new SqliteStorageProvider(
                    new File(dataFolder, "ledger.db")
                );
                default -> {
                    getLogger().atWarning().log("Unknown storage type '%s', using JSON", storageType);
                    yield new JsonStorageProvider(new File(dataFolder, "data"));
                }
            };

            storage.initialize();
            getLogger().atInfo().log("Storage initialized: %s", storage.getClass().getSimpleName());
            return true;
        } catch (Exception e) {
            getLogger().atSevere().log("Failed to initialize storage: %s", e.getMessage());
            return false;
        }
    }

    private void initializeTransactionLogger() {
        try {
            boolean logEnabled = config.isTransactionLoggingEnabled();
            transactionLogger = new TransactionLogger(this, logEnabled);
            transactionLogger.initialize();
        } catch (Exception e) {
            getLogger().atWarning().log("Failed to initialize transaction logger: %s", e.getMessage());
            transactionLogger = null;
        }
    }

    private void registerDefaultCurrency() {
        Currency primary = CurrencyBuilder.create(config.getCurrencyId())
            .displayName(config.getCurrencyDisplayName())
            .singular(config.getCurrencySingular())
            .plural(config.getCurrencyPlural())
            .symbol(config.getCurrencySymbol())
            .decimals(config.getCurrencyDecimals())
            .defaultBalance(config.getStartingBalance())
            .minBalance(config.getMinBalance())
            .maxBalance(config.getMaxBalance())
            .primary(true)
            .build();

        economyService.registerCurrencySync(primary);
        getLogger().atInfo().log("Primary currency registered: %s", primary.getDisplayName());
    }

    private void registerListeners() {
        var eventRegistry = getEventRegistry();

        // Player connect - create/load account
        eventRegistrations.add(
            eventRegistry.register(PlayerConnectEvent.class, event -> {
                var playerRef = event.getPlayerRef();
                economyService.getOrCreateAccount(playerRef.getUuid(), playerRef.getUsername())
                    .thenAccept(account -> {
                        if (config.isDebug()) {
                            getLogger().atInfo().log("Account loaded for %s", playerRef.getUsername());
                        }
                    })
                    .exceptionally(e -> {
                        getLogger().atSevere().log("Failed to load account for %s: %s", playerRef.getUsername(), e.getMessage());
                        return null;
                    });
            })
        );

        // Player disconnect - invalidate cache
        eventRegistrations.add(
            eventRegistry.register(PlayerDisconnectEvent.class, event -> {
                var playerRef = event.getPlayerRef();
                economyService.invalidateCache(playerRef.getUuid());
                if (config.isDebug()) {
                    getLogger().atInfo().log("Cache invalidated for %s", playerRef.getUsername());
                }
            })
        );

        getLogger().atInfo().log("Event listeners registered.");
    }

    private void registerCommands() {
        var commandRegistry = getCommandRegistry();

        commandRegistry.registerCommand(new BalanceCommand());
        commandRegistry.registerCommand(new PayCommand());
        commandRegistry.registerCommand(new BalTopCommand());
        commandRegistry.registerCommand(new EcoCommand());
        commandRegistry.registerCommand(new LedgerCommand());

        getLogger().atInfo().log("Commands registered.");
    }

    /**
     * Notify a player that they received a payment.
     *
     * @param receiverUuid the receiver's UUID
     * @param senderName the sender's name
     * @param formattedAmount the formatted amount string
     */
    public void notifyPaymentReceived(UUID receiverUuid, String senderName, String formattedAmount) {
        try {
            // Get the player from Universe
            PlayerRef receiver = Universe.get().getPlayer(receiverUuid);
            if (receiver != null) {
                receiver.sendMessage(Messages.payReceived(senderName, formattedAmount));
            }
        } catch (Exception e) {
            if (config.isDebug()) {
                getLogger().atWarning().log("Could not notify payment recipient: %s", e.getMessage());
            }
        }
    }

    /**
     * Get the data folder for this plugin.
     *
     * @return data folder
     */
    public File getDataFolder() {
        return getDataDirectory().toFile();
    }

    /**
     * Get the plugin version.
     *
     * @return version string
     */
    public String getVersion() {
        return VERSION;
    }

    /**
     * Get the plugin instance.
     *
     * @return the plugin instance
     */
    public static LedgerPlugin getInstance() {
        return instance;
    }

    /**
     * Get the plugin configuration.
     *
     * @return the config
     */
    public LedgerConfig getConfiguration() {
        return config;
    }

    /**
     * Get the messages configuration.
     *
     * @return the messages config
     */
    public MessagesConfig getMessages() {
        return messages;
    }

    /**
     * Get the storage provider.
     *
     * @return the storage provider
     */
    public StorageProvider getStorage() {
        return storage;
    }

    /**
     * Get the economy service.
     *
     * @return the economy service
     */
    public SimpleEconomyService getEconomyService() {
        return economyService;
    }

    /**
     * Get the event manager.
     *
     * @return the event manager
     */
    public LedgerEventManager getEventManager() {
        return eventManager;
    }

    /**
     * Get the transaction logger.
     *
     * @return the transaction logger, or null if disabled
     */
    public TransactionLogger getTransactionLogger() {
        return transactionLogger;
    }

    /**
     * Reload the plugin configuration.
     *
     * @return true if reload was successful
     */
    public boolean reload() {
        try {
            config = new LedgerConfig(new File(getDataFolder(), "config.json"));
            messages = new MessagesConfig(new File(getDataFolder(), "messages.json"));
            getLogger().atInfo().log("Configuration reloaded.");
            return true;
        } catch (Exception e) {
            getLogger().atSevere().log("Failed to reload configuration: %s", e.getMessage());
            return false;
        }
    }
}
