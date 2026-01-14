package dev.karmabyte.ledger.api.event.economy;

import dev.karmabyte.ledger.api.event.Cancellable;
import dev.karmabyte.ledger.api.event.LedgerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Event fired when an account is about to be deleted.
 *
 * <p>This event can be cancelled to prevent deletion.
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * @EventHandler
 * public void onAccountDelete(AccountDeleteEvent event) {
 *     // Prevent deletion of accounts with balance
 *     economy.getBalance(event.getAccountId()).thenAccept(balance -> {
 *         if (balance > 0) {
 *             event.cancel("Cannot delete account with balance");
 *         }
 *     });
 * }
 * }</pre>
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public class AccountDeleteEvent extends LedgerEvent implements Cancellable {

    private final UUID accountId;
    private final String accountName;
    private boolean cancelled;
    private String cancelReason;

    public AccountDeleteEvent(@NotNull UUID accountId, @NotNull String accountName) {
        this.accountId = accountId;
        this.accountName = accountName;
    }

    /**
     * Get the account ID being deleted.
     *
     * @return the account UUID
     */
    @NotNull
    public UUID getAccountId() {
        return accountId;
    }

    /**
     * Get the name of the account being deleted.
     *
     * @return the account name
     */
    @NotNull
    public String getAccountName() {
        return accountName;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    @Nullable
    public String getCancelReason() {
        return cancelReason;
    }

    @Override
    public void cancel(@NotNull String reason) {
        this.cancelled = true;
        this.cancelReason = reason;
    }
}
