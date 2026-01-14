package dev.karmabyte.ledger.core.logging;

import dev.karmabyte.ledger.api.economy.transaction.TransactionType;
import dev.karmabyte.ledger.core.LedgerPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Logs all economy transactions to a SQLite database.
 *
 * <p>This provides an audit trail of all economy activity,
 * useful for debugging, analytics, and admin review.
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public class TransactionLogger {

    private final LedgerPlugin plugin;
    private Connection connection;
    private final boolean enabled;

    public TransactionLogger(@NotNull LedgerPlugin plugin, boolean enabled) {
        this.plugin = plugin;
        this.enabled = enabled;
    }

    /**
     * Initialize the transaction log database.
     */
    public void initialize() throws Exception {
        if (!enabled) {
            plugin.getLogger().atInfo().log("Transaction logging disabled.");
            return;
        }

        // Load SQLite driver
        Class.forName("org.sqlite.JDBC");

        // Connect to transactions database
        var dbFile = plugin.getDataFolder().toPath().resolve("transactions.db").toFile();
        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        connection = DriverManager.getConnection(url);

        // Create tables
        createTables();

        plugin.getLogger().atInfo().log("Transaction logging initialized.");
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    timestamp INTEGER NOT NULL,
                    player_uuid TEXT NOT NULL,
                    player_name TEXT,
                    target_uuid TEXT,
                    target_name TEXT,
                    type TEXT NOT NULL,
                    currency TEXT NOT NULL,
                    amount REAL NOT NULL,
                    previous_balance REAL NOT NULL,
                    new_balance REAL NOT NULL
                )
            """);

            // Index for querying by player
            stmt.execute("""
                CREATE INDEX IF NOT EXISTS idx_transactions_player
                ON transactions(player_uuid, timestamp DESC)
            """);

            // Index for querying by type
            stmt.execute("""
                CREATE INDEX IF NOT EXISTS idx_transactions_type
                ON transactions(type, timestamp DESC)
            """);
        }
    }

    /**
     * Log a transaction asynchronously.
     *
     * @param playerId the player UUID
     * @param targetId the target UUID (for transfers), or null
     * @param type the transaction type
     * @param currencyId the currency identifier
     * @param amount the transaction amount
     * @param previousBalance the balance before
     * @param newBalance the balance after
     */
    public void log(@NotNull UUID playerId,
                    @Nullable UUID targetId,
                    @NotNull TransactionType type,
                    @NotNull String currencyId,
                    double amount,
                    double previousBalance,
                    double newBalance) {
        if (!enabled || connection == null) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try (PreparedStatement stmt = connection.prepareStatement("""
                INSERT INTO transactions
                (timestamp, player_uuid, target_uuid, type, currency, amount, previous_balance, new_balance)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """)) {
                stmt.setLong(1, Instant.now().toEpochMilli());
                stmt.setString(2, playerId.toString());
                stmt.setString(3, targetId != null ? targetId.toString() : null);
                stmt.setString(4, type.name());
                stmt.setString(5, currencyId);
                stmt.setDouble(6, amount);
                stmt.setDouble(7, previousBalance);
                stmt.setDouble(8, newBalance);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().atWarning().log("Failed to log transaction: %s", e.getMessage());
            }
        });
    }

    /**
     * Get recent transactions for a player.
     *
     * @param playerId the player UUID
     * @param limit maximum number of transactions
     * @return list of transaction records
     */
    @NotNull
    public CompletableFuture<List<TransactionRecord>> getHistory(@NotNull UUID playerId, int limit) {
        if (!enabled || connection == null) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        return CompletableFuture.supplyAsync(() -> {
            List<TransactionRecord> records = new ArrayList<>();
            try (PreparedStatement stmt = connection.prepareStatement("""
                SELECT timestamp, player_uuid, target_uuid, type, currency, amount, previous_balance, new_balance
                FROM transactions
                WHERE player_uuid = ? OR target_uuid = ?
                ORDER BY timestamp DESC
                LIMIT ?
            """)) {
                stmt.setString(1, playerId.toString());
                stmt.setString(2, playerId.toString());
                stmt.setInt(3, limit);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        records.add(new TransactionRecord(
                            Instant.ofEpochMilli(rs.getLong("timestamp")),
                            UUID.fromString(rs.getString("player_uuid")),
                            rs.getString("target_uuid") != null ? UUID.fromString(rs.getString("target_uuid")) : null,
                            TransactionType.valueOf(rs.getString("type")),
                            rs.getString("currency"),
                            rs.getDouble("amount"),
                            rs.getDouble("previous_balance"),
                            rs.getDouble("new_balance")
                        ));
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().atWarning().log("Failed to get transaction history: %s", e.getMessage());
            }
            return records;
        });
    }

    /**
     * Get total transaction count.
     *
     * @return total number of logged transactions
     */
    @NotNull
    public CompletableFuture<Integer> getTotalCount() {
        if (!enabled || connection == null) {
            return CompletableFuture.completedFuture(0);
        }

        return CompletableFuture.supplyAsync(() -> {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM transactions")) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException e) {
                plugin.getLogger().atWarning().log("Failed to get transaction count: %s", e.getMessage());
            }
            return 0;
        });
    }

    /**
     * Close the database connection.
     */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                plugin.getLogger().atWarning().log("Error closing transaction log: %s", e.getMessage());
            }
        }
    }

    /**
     * Check if transaction logging is enabled.
     *
     * @return true if enabled
     */
    public boolean isEnabled() {
        return enabled && connection != null;
    }

    /**
     * Record of a logged transaction.
     */
    public record TransactionRecord(
        Instant timestamp,
        UUID playerId,
        UUID targetId,
        TransactionType type,
        String currency,
        double amount,
        double previousBalance,
        double newBalance
    ) {}
}
