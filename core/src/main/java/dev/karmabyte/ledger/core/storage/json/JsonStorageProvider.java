package dev.karmabyte.ledger.core.storage.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.karmabyte.ledger.api.economy.Account;
import dev.karmabyte.ledger.core.economy.SimpleAccount;
import dev.karmabyte.ledger.core.economy.SimpleEconomyService;
import dev.karmabyte.ledger.core.LedgerPlugin;
import dev.karmabyte.ledger.core.storage.StorageProvider;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * JSON file-based storage implementation.
 *
 * <p>Each player account is stored in a separate JSON file in the data directory.
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public class JsonStorageProvider implements StorageProvider {

    private final File dataDirectory;
    private final Gson gson;
    private final Map<UUID, AccountData> cache = new ConcurrentHashMap<>();

    public JsonStorageProvider(File dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
    }

    @Override
    public void initialize() throws Exception {
        if (!dataDirectory.exists()) {
            dataDirectory.mkdirs();
        }

        // Load all existing accounts into cache
        File[] files = dataDirectory.listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                try {
                    String uuidStr = file.getName().replace(".json", "");
                    UUID uuid = UUID.fromString(uuidStr);
                    AccountData data = loadFromFile(file);
                    if (data != null) {
                        cache.put(uuid, data);
                    }
                } catch (Exception e) {
                    LedgerPlugin.getInstance().getLogger().atWarning().log(
                        "Failed to load account file: %s - %s", file.getName(), e.getMessage()
                    );
                }
            }
        }
    }

    @Override
    public void close() throws Exception {
        // Save all cached accounts
        for (Map.Entry<UUID, AccountData> entry : cache.entrySet()) {
            saveToFile(entry.getKey(), entry.getValue());
        }
        cache.clear();
    }

    @Override
    @NotNull
    public CompletableFuture<Boolean> hasAccount(@NotNull UUID playerId) {
        return CompletableFuture.supplyAsync(() ->
            cache.containsKey(playerId) || getAccountFile(playerId).exists()
        );
    }

    @Override
    @NotNull
    public CompletableFuture<Optional<Account>> loadAccount(@NotNull UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            // Check cache first
            AccountData data = cache.get(playerId);
            if (data != null) {
                return Optional.of(dataToAccount(playerId, data));
            }

            // Load from file
            File file = getAccountFile(playerId);
            if (!file.exists()) {
                return Optional.empty();
            }

            data = loadFromFile(file);
            if (data != null) {
                cache.put(playerId, data);
                return Optional.of(dataToAccount(playerId, data));
            }

            return Optional.empty();
        });
    }

    @Override
    @NotNull
    public CompletableFuture<Void> saveAccount(@NotNull Account account) {
        return CompletableFuture.runAsync(() -> {
            AccountData data = accountToData(account);
            cache.put(account.getOwner(), data);
            saveToFile(account.getOwner(), data);
        });
    }

    @Override
    @NotNull
    public CompletableFuture<Boolean> deleteAccount(@NotNull UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            cache.remove(playerId);
            File file = getAccountFile(playerId);
            if (file.exists()) {
                return file.delete();
            }
            return false;
        });
    }

    @Override
    @NotNull
    public CompletableFuture<List<UUID>> getTopBalances(@NotNull String currencyId, int limit) {
        return CompletableFuture.supplyAsync(() ->
            cache.entrySet().stream()
                .sorted((a, b) -> {
                    double balA = a.getValue().balances.getOrDefault(currencyId, 0.0);
                    double balB = b.getValue().balances.getOrDefault(currencyId, 0.0);
                    return Double.compare(balB, balA); // Descending
                })
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList())
        );
    }

    @Override
    @NotNull
    public String getType() {
        return "JSON";
    }

    @Override
    @NotNull
    public CompletableFuture<Integer> getAccountCount() {
        return CompletableFuture.completedFuture(cache.size());
    }

    // ==================== Internal ====================

    private File getAccountFile(UUID playerId) {
        return new File(dataDirectory, playerId.toString() + ".json");
    }

    private AccountData loadFromFile(File file) {
        try (Reader reader = new FileReader(file)) {
            return gson.fromJson(reader, AccountData.class);
        } catch (Exception e) {
            LedgerPlugin.getInstance().getLogger().atWarning().log(
                "Failed to read account file: %s - %s", file.getName(), e.getMessage()
            );
            return null;
        }
    }

    private void saveToFile(UUID playerId, AccountData data) {
        File file = getAccountFile(playerId);
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(data, writer);
        } catch (Exception e) {
            LedgerPlugin.getInstance().getLogger().atWarning().log(
                "Failed to save account file: %s - %s", file.getName(), e.getMessage()
            );
        }
    }

    private Account dataToAccount(UUID playerId, AccountData data) {
        SimpleEconomyService service = LedgerPlugin.getInstance().getEconomyService();
        return new SimpleAccount(playerId, data.name, data.balances, service);
    }

    private AccountData accountToData(Account account) {
        AccountData data = new AccountData();
        data.name = account.getName();
        if (account instanceof SimpleAccount simple) {
            data.balances = simple.getBalancesMap();
        } else {
            data.balances = new HashMap<>();
        }
        return data;
    }

    /**
     * Internal data class for JSON serialization.
     */
    private static class AccountData {
        String name;
        Map<String, Double> balances = new HashMap<>();
    }
}
