package dev.karmabyte.ledger.core.util;

import com.hypixel.hytale.server.core.Message;

/**
 * Utility class for building styled messages using Hytale's native Message API.
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public final class Messages {

    // Color palette (hex codes)
    public static final String COLOR_GOLD = "#FFD700";
    public static final String COLOR_GREEN = "#55FF55";
    public static final String COLOR_RED = "#FF5555";
    public static final String COLOR_WHITE = "#FFFFFF";
    public static final String COLOR_YELLOW = "#FFFF55";
    public static final String COLOR_GRAY = "#AAAAAA";
    public static final String COLOR_AQUA = "#55FFFF";

    private Messages() {}

    /**
     * Create the Ledger prefix message.
     */
    public static Message prefix() {
        return Message.raw("[Ledger] ").color(COLOR_GOLD).bold(true);
    }

    /**
     * Create a success message with prefix.
     */
    public static Message success(String text) {
        return Message.join(
            prefix(),
            Message.raw(text).color(COLOR_GREEN)
        );
    }

    /**
     * Create an error message with prefix.
     */
    public static Message error(String text) {
        return Message.join(
            prefix(),
            Message.raw(text).color(COLOR_RED)
        );
    }

    /**
     * Create an info message with prefix.
     */
    public static Message info(String text) {
        return Message.join(
            prefix(),
            Message.raw(text).color(COLOR_WHITE)
        );
    }

    // ========== Balance Messages ==========

    public static Message balanceSelf(String balance) {
        return Message.join(
            prefix(),
            Message.raw("Your balance: ").color(COLOR_GREEN),
            Message.raw(balance).color(COLOR_WHITE).bold(true)
        );
    }

    public static Message balanceOther(String player, String balance) {
        return Message.join(
            prefix(),
            Message.raw(player).color(COLOR_AQUA),
            Message.raw("'s balance: ").color(COLOR_GREEN),
            Message.raw(balance).color(COLOR_WHITE).bold(true)
        );
    }

    // ========== Pay Messages ==========

    public static Message paySent(String target, String amount) {
        return Message.join(
            prefix(),
            Message.raw("You sent ").color(COLOR_GREEN),
            Message.raw(amount).color(COLOR_WHITE).bold(true),
            Message.raw(" to ").color(COLOR_GREEN),
            Message.raw(target).color(COLOR_AQUA)
        );
    }

    public static Message payReceived(String player, String amount) {
        return Message.join(
            prefix(),
            Message.raw("You received ").color(COLOR_GREEN),
            Message.raw(amount).color(COLOR_WHITE).bold(true),
            Message.raw(" from ").color(COLOR_GREEN),
            Message.raw(player).color(COLOR_AQUA)
        );
    }

    public static Message payInsufficientFunds() {
        return error("Insufficient funds.");
    }

    public static Message payInvalidAmount() {
        return error("Invalid amount. Must be greater than 0.");
    }

    public static Message paySelfNotAllowed() {
        return error("You cannot pay yourself.");
    }

    public static Message payMinAmount(String amount) {
        return error("Minimum payment amount is " + amount + ".");
    }

    public static Message payMaxAmount(String amount) {
        return error("Maximum payment amount is " + amount + ".");
    }

    public static Message payCooldown(int seconds) {
        return error("Please wait " + seconds + " seconds before paying again.");
    }

    // ========== Eco Admin Messages ==========

    public static Message ecoGive(String player, String amount) {
        return Message.join(
            prefix(),
            Message.raw("Gave ").color(COLOR_GREEN),
            Message.raw(amount).color(COLOR_WHITE).bold(true),
            Message.raw(" to ").color(COLOR_GREEN),
            Message.raw(player).color(COLOR_AQUA)
        );
    }

    public static Message ecoTake(String player, String amount) {
        return Message.join(
            prefix(),
            Message.raw("Took ").color(COLOR_GREEN),
            Message.raw(amount).color(COLOR_WHITE).bold(true),
            Message.raw(" from ").color(COLOR_GREEN),
            Message.raw(player).color(COLOR_AQUA)
        );
    }

    public static Message ecoSet(String player, String balance) {
        return Message.join(
            prefix(),
            Message.raw("Set ").color(COLOR_GREEN),
            Message.raw(player).color(COLOR_AQUA),
            Message.raw("'s balance to ").color(COLOR_GREEN),
            Message.raw(balance).color(COLOR_WHITE).bold(true)
        );
    }

    public static Message ecoReset(String player, String balance) {
        return Message.join(
            prefix(),
            Message.raw("Reset ").color(COLOR_GREEN),
            Message.raw(player).color(COLOR_AQUA),
            Message.raw("'s balance to ").color(COLOR_GREEN),
            Message.raw(balance).color(COLOR_WHITE).bold(true)
        );
    }

    // ========== Baltop Messages ==========

    public static Message baltopHeader(int page) {
        return Message.join(
            Message.raw("=== ").color(COLOR_GOLD),
            Message.raw("Top Balances").color(COLOR_YELLOW).bold(true),
            Message.raw(" (Page " + page + ") ").color(COLOR_GOLD),
            Message.raw("===").color(COLOR_GOLD)
        );
    }

    public static Message baltopEntry(int position, String player, String balance) {
        return Message.join(
            Message.raw(position + ". ").color(COLOR_YELLOW),
            Message.raw(player).color(COLOR_WHITE),
            Message.raw(": ").color(COLOR_GRAY),
            Message.raw(balance).color(COLOR_GREEN)
        );
    }

    public static Message baltopEmpty() {
        return info("No balances to display.");
    }

    // ========== Error Messages ==========

    public static Message errorGeneric() {
        return error("An error occurred. Please try again.");
    }

    public static Message errorPlayerOnly() {
        return error("This command can only be used by players.");
    }

    public static Message errorNoPermission() {
        return error("You don't have permission to do that.");
    }

    public static Message errorPlayerNotFound(String player) {
        return error("Player not found: " + player);
    }

    // ========== Ledger Admin Messages ==========

    public static Message reloadSuccess() {
        return success("Configuration reloaded successfully.");
    }

    public static Message reloadFailed() {
        return error("Failed to reload configuration.");
    }

    public static Message ledgerInfo(String version, String storage, String currency, int accounts) {
        return Message.join(
            Message.raw("\n=== ").color(COLOR_GOLD),
            Message.raw("Ledger Economy").color(COLOR_YELLOW).bold(true),
            Message.raw(" ===\n").color(COLOR_GOLD),
            Message.raw("Version: ").color(COLOR_GRAY),
            Message.raw(version).color(COLOR_WHITE),
            Message.raw("\nStorage: ").color(COLOR_GRAY),
            Message.raw(storage).color(COLOR_WHITE),
            Message.raw("\nCurrency: ").color(COLOR_GRAY),
            Message.raw(currency).color(COLOR_WHITE),
            Message.raw("\nAccounts: ").color(COLOR_GRAY),
            Message.raw(String.valueOf(accounts)).color(COLOR_WHITE),
            Message.raw("\n").color(COLOR_GOLD)
        );
    }
}
