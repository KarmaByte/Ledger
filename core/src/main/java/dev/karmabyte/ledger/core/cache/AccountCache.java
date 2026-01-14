package dev.karmabyte.ledger.core.cache;

import dev.karmabyte.ledger.api.economy.Account;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory cache for player accounts.
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public class AccountCache {

    private final Map<UUID, CacheEntry> cache = new ConcurrentHashMap<>();
    private final long expireMinutes;

    public AccountCache(long expireMinutes) {
        this.expireMinutes = expireMinutes;
    }

    /**
     * Check if an account is in the cache.
     *
     * @param playerId the player UUID
     * @return true if cached and not expired
     */
    public boolean contains(@NotNull UUID playerId) {
        CacheEntry entry = cache.get(playerId);
        if (entry == null) {
            return false;
        }
        if (isExpired(entry)) {
            cache.remove(playerId);
            return false;
        }
        return true;
    }

    /**
     * Get an account from the cache.
     *
     * @param playerId the player UUID
     * @return the account, or null if not cached or expired
     */
    @Nullable
    public Account get(@NotNull UUID playerId) {
        CacheEntry entry = cache.get(playerId);
        if (entry == null) {
            return null;
        }
        if (isExpired(entry)) {
            cache.remove(playerId);
            return null;
        }
        entry.lastAccess = Instant.now();
        return entry.account;
    }

    /**
     * Put an account in the cache.
     *
     * @param playerId the player UUID
     * @param account the account
     */
    public void put(@NotNull UUID playerId, @NotNull Account account) {
        cache.put(playerId, new CacheEntry(account));
    }

    /**
     * Remove an account from the cache.
     *
     * @param playerId the player UUID
     */
    public void remove(@NotNull UUID playerId) {
        cache.remove(playerId);
    }

    /**
     * Clear the entire cache.
     */
    public void clear() {
        cache.clear();
    }

    /**
     * Get the number of cached entries.
     *
     * @return cache size
     */
    public int size() {
        return cache.size();
    }

    /**
     * Clean up expired entries.
     */
    public void cleanup() {
        cache.entrySet().removeIf(entry -> isExpired(entry.getValue()));
    }

    private boolean isExpired(CacheEntry entry) {
        if (expireMinutes <= 0) {
            return false; // Never expire
        }
        return entry.lastAccess.plusSeconds(expireMinutes * 60).isBefore(Instant.now());
    }

    private static class CacheEntry {
        final Account account;
        Instant lastAccess;

        CacheEntry(Account account) {
            this.account = account;
            this.lastAccess = Instant.now();
        }
    }
}
