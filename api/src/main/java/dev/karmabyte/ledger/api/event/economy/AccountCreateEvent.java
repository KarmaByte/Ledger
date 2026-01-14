package dev.karmabyte.ledger.api.event.economy;

import dev.karmabyte.ledger.api.economy.Account;
import dev.karmabyte.ledger.api.event.LedgerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Event fired when a new account is created.
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * @EventHandler
 * public void onAccountCreate(AccountCreateEvent event) {
 *     Player player = getPlayer(event.getAccount().getOwner());
 *     if (player != null) {
 *         player.sendMessage("Welcome! You have received " +
 *             economy.getPrimaryCurrency().getDefaultBalance() + " starting coins!");
 *     }
 * }
 * }</pre>
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public class AccountCreateEvent extends LedgerEvent {

    private final Account account;

    public AccountCreateEvent(@NotNull Account account) {
        this.account = account;
    }

    /**
     * Get the newly created account.
     *
     * @return the account
     */
    @NotNull
    public Account getAccount() {
        return account;
    }
}
