package dev.karmabyte.ledger.core.storage.sqlite;

import dev.karmabyte.ledger.api.economy.Account;
import dev.karmabyte.ledger.core.LedgerPlugin;
import dev.karmabyte.ledger.core.economy.SimpleAccount;
import dev.karmabyte.ledger.core.economy.SimpleEconomyService;
import dev.karmabyte.ledger.core.storage.StorageProvider;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * SQLite database storage implementation.
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public class SqliteStorageProvider implements StorageProvider {

    private final File databaseFile;
    private Connection connection;

    public SqliteStorageProvider(File databaseFile) {
        this.databaseFile = databaseFile;
    }

    @Override
    public void initialize() throws Exception {
        // Ensure parent directory exists
        if (!databaseFile.getParentFile().exists()) {
            databaseFile.getParentFile().mkdirs();
        }

        // Explicitly load the SQLite JDBC driver
        Class.forName("org.sqlite.JDBC");

        // Connect to database
        String url = "jdbc:sqlite:" + databaseFile.getAbsolutePath();
        connection = DriverManager.getConnection(url);

        // Create tables
        createTables();
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Accounts table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS accounts (
                    uuid TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    created_at INTEGER DEFAULT (strftime('%s', 'now'))
                )
            """);

            // Balances table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS balances (
                    uuid TEXT NOT NULL,
                    currency TEXT NOT NULL,
                    balance REAL NOT NULL DEFAULT 0,
                    PRIMARY KEY (uuid, currency),
                    FOREIGN KEY (uuid) REFERENCES accounts(uuid) ON DELETE CASCADE
                )
            """);

            // Index for top balances queries
            stmt.execute("""
                CREATE INDEX IF NOT EXISTS idx_balances_currency_balance
                ON balances(currency, balance DESC)
            """);
        }
    }

    @Override
    public void close() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Override
    @NotNull
    public CompletableFuture<Boolean> hasAccount(@NotNull UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT 1 FROM accounts WHERE uuid = ?")) {
                stmt.setString(1, playerId.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            } catch (SQLException e) {
                LedgerPlugin.getInstance().getLogger().atWarning().log(
                    "Failed to check account existence: %s", e.getMessage()
                );
                return false;
            }
        });
    }

    @Override
    @NotNull
    public CompletableFuture<Optional<Account>> loadAccount(@NotNull UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Load account info
                String name = null;
                try (PreparedStatement stmt = connection.prepareStatement(
                        "SELECT name FROM accounts WHERE uuid = ?")) {
                    stmt.setString(1, playerId.toString());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            name = rs.getString("name");
                        } else {
                            return Optional.empty();
                        }
                    }
                }

                // Load balances
                Map<String, Double> balances = new HashMap<>();
                try (PreparedStatement stmt = connection.prepareStatement(
                        "SELECT currency, balance FROM balances WHERE uuid = ?")) {
                    stmt.setString(1, playerId.toString());
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            balances.put(rs.getString("currency"), rs.getDouble("balance"));
                        }
                    }
                }

                SimpleEconomyService service = LedgerPlugin.getInstance().getEconomyService();
                return Optional.of(new SimpleAccount(playerId, name, balances, service));
            } catch (SQLException e) {
                LedgerPlugin.getInstance().getLogger().atWarning().log(
                    "Failed to load account: %s", e.getMessage()
                );
                return Optional.empty();
            }
        });
    }

    @Override
    @NotNull
    public CompletableFuture<Void> saveAccount(@NotNull Account account) {
        return CompletableFuture.runAsync(() -> {
            try {
                connection.setAutoCommit(false);

                // Insert or update account
                try (PreparedStatement stmt = connection.prepareStatement(
                        "INSERT OR REPLACE INTO accounts (uuid, name) VALUES (?, ?)")) {
                    stmt.setString(1, account.getOwner().toString());
                    stmt.setString(2, account.getName());
                    stmt.executeUpdate();
                }

                // Update balances
                if (account instanceof SimpleAccount simple) {
                    for (Map.Entry<String, Double> entry : simple.getBalancesMap().entrySet()) {
                        try (PreparedStatement stmt = connection.prepareStatement(
                                "INSERT OR REPLACE INTO balances (uuid, currency, balance) VALUES (?, ?, ?)")) {
                            stmt.setString(1, account.getOwner().toString());
                            stmt.setString(2, entry.getKey());
                            stmt.setDouble(3, entry.getValue());
                            stmt.executeUpdate();
                        }
                    }
                }

                connection.commit();
            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    // Ignore rollback error
                }
                LedgerPlugin.getInstance().getLogger().atWarning().log(
                    "Failed to save account: %s", e.getMessage()
                );
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    // Ignore
                }
            }
        });
    }

    @Override
    @NotNull
    public CompletableFuture<Boolean> deleteAccount(@NotNull UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                connection.setAutoCommit(false);

                // Delete balances first
                try (PreparedStatement stmt = connection.prepareStatement(
                        "DELETE FROM balances WHERE uuid = ?")) {
                    stmt.setString(1, playerId.toString());
                    stmt.executeUpdate();
                }

                // Delete account
                try (PreparedStatement stmt = connection.prepareStatement(
                        "DELETE FROM accounts WHERE uuid = ?")) {
                    stmt.setString(1, playerId.toString());
                    int rows = stmt.executeUpdate();
                    connection.commit();
                    return rows > 0;
                }
            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    // Ignore
                }
                LedgerPlugin.getInstance().getLogger().atWarning().log(
                    "Failed to delete account: %s", e.getMessage()
                );
                return false;
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    // Ignore
                }
            }
        });
    }

    @Override
    @NotNull
    public CompletableFuture<List<UUID>> getTopBalances(@NotNull String currencyId, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            List<UUID> result = new ArrayList<>();
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT uuid FROM balances WHERE currency = ? ORDER BY balance DESC LIMIT ?")) {
                stmt.setString(1, currencyId);
                stmt.setInt(2, limit);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        result.add(UUID.fromString(rs.getString("uuid")));
                    }
                }
            } catch (SQLException e) {
                LedgerPlugin.getInstance().getLogger().atWarning().log(
                    "Failed to get top balances: %s", e.getMessage()
                );
            }
            return result;
        });
    }

    @Override
    @NotNull
    public String getType() {
        return "SQLite";
    }

    @Override
    @NotNull
    public CompletableFuture<Integer> getAccountCount() {
        return CompletableFuture.supplyAsync(() -> {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM accounts")) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException e) {
                LedgerPlugin.getInstance().getLogger().atWarning().log(
                    "Failed to get account count: %s", e.getMessage()
                );
            }
            return 0;
        });
    }
}
