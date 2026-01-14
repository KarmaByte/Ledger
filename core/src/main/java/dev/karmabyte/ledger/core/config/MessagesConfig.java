package dev.karmabyte.ledger.core.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Messages configuration for Ledger.
 *
 * <p>Supports placeholders:
 * <ul>
 *   <li>{player} - Player name</li>
 *   <li>{balance} - Formatted balance</li>
 *   <li>{amount} - Formatted amount</li>
 *   <li>{currency} - Currency name</li>
 *   <li>{target} - Target player name</li>
 * </ul>
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public class MessagesConfig {

    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create();

    // General
    private String prefix = "&6[Ledger]&r ";
    private String noPermission = "&cYou don't have permission to do that.";
    private String playerNotFound = "&cPlayer not found: {player}";
    private String invalidAmount = "&cInvalid amount: {amount}";
    private String reloadSuccess = "&aConfiguration reloaded successfully.";
    private String reloadFailed = "&cFailed to reload configuration.";

    // Balance
    private String balanceSelf = "&aYour balance: &f{balance}";
    private String balanceOther = "&a{player}'s balance: &f{balance}";

    // Pay
    private String paySent = "&aYou sent {amount} to {target}.";
    private String payReceived = "&aYou received {amount} from {player}.";
    private String payInsufficientFunds = "&cInsufficient funds. You have {balance}.";
    private String payMinAmount = "&cMinimum payment amount is {amount}.";
    private String payMaxAmount = "&cMaximum payment amount is {amount}.";
    private String paySelfNotAllowed = "&cYou cannot pay yourself.";
    private String payCooldown = "&cPlease wait {seconds} seconds before paying again.";

    // Eco Admin
    private String ecoGive = "&aGave {amount} to {player}. New balance: {balance}";
    private String ecoTake = "&aTook {amount} from {player}. New balance: {balance}";
    private String ecoSet = "&aSet {player}'s balance to {balance}.";
    private String ecoReset = "&aReset {player}'s balance to default.";

    // Baltop
    private String baltopHeader = "&6=== Top Balances ===";
    private String baltopEntry = "&e{position}. &f{player}: &a{balance}";
    private String baltopFooter = "&6==================";
    private String baltopEmpty = "&7No balances to display.";

    // Errors
    private String errorGeneral = "&cAn error occurred. Please try again.";
    private String errorNoEconomy = "&cEconomy service is not available.";

    /**
     * Load or create messages from file.
     *
     * @param file the messages file
     */
    public MessagesConfig(File file) throws IOException {
        if (file.exists()) {
            try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                MessagesConfig loaded = GSON.fromJson(reader, MessagesConfig.class);
                if (loaded != null) {
                    copyFrom(loaded);
                }
            }
        }
        save(file);
    }

    /**
     * Save messages to file.
     */
    public void save(File file) throws IOException {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            GSON.toJson(this, writer);
        }
    }

    private void copyFrom(MessagesConfig other) {
        this.prefix = other.prefix;
        this.noPermission = other.noPermission;
        this.playerNotFound = other.playerNotFound;
        this.invalidAmount = other.invalidAmount;
        this.reloadSuccess = other.reloadSuccess;
        this.reloadFailed = other.reloadFailed;
        this.balanceSelf = other.balanceSelf;
        this.balanceOther = other.balanceOther;
        this.paySent = other.paySent;
        this.payReceived = other.payReceived;
        this.payInsufficientFunds = other.payInsufficientFunds;
        this.payMinAmount = other.payMinAmount;
        this.payMaxAmount = other.payMaxAmount;
        this.paySelfNotAllowed = other.paySelfNotAllowed;
        this.payCooldown = other.payCooldown;
        this.ecoGive = other.ecoGive;
        this.ecoTake = other.ecoTake;
        this.ecoSet = other.ecoSet;
        this.ecoReset = other.ecoReset;
        this.baltopHeader = other.baltopHeader;
        this.baltopEntry = other.baltopEntry;
        this.baltopFooter = other.baltopFooter;
        this.baltopEmpty = other.baltopEmpty;
        this.errorGeneral = other.errorGeneral;
        this.errorNoEconomy = other.errorNoEconomy;
    }

    /**
     * Format a message with color codes.
     *
     * @param message the message
     * @return formatted message
     */
    public static String format(String message) {
        return message.replace("&", "§");
    }

    /**
     * Get a formatted message with prefix.
     *
     * @param message the message
     * @return formatted message with prefix
     */
    public String withPrefix(String message) {
        return format(prefix + message);
    }

    // Getters with formatting

    public String getPrefix() { return format(prefix); }
    public String getNoPermission() { return withPrefix(noPermission); }
    public String getPlayerNotFound(String player) {
        return withPrefix(playerNotFound.replace("{player}", player));
    }
    public String getInvalidAmount() { return withPrefix(invalidAmount); }
    public String getReloadSuccess() { return withPrefix(reloadSuccess); }
    public String getReloadFailed() { return withPrefix(reloadFailed); }

    public String getBalanceSelf(String balance) {
        return withPrefix(balanceSelf.replace("{balance}", balance));
    }
    public String getBalanceOther(String player, String balance) {
        return withPrefix(balanceOther
            .replace("{player}", player)
            .replace("{balance}", balance));
    }

    public String getPaySent(String target, String amount) {
        return withPrefix(paySent
            .replace("{target}", target)
            .replace("{amount}", amount));
    }
    public String getPayReceived(String player, String amount) {
        return withPrefix(payReceived
            .replace("{player}", player)
            .replace("{amount}", amount));
    }
    public String getPayInsufficientFunds() { return withPrefix(payInsufficientFunds); }
    public String getPayInvalidAmount() { return withPrefix(invalidAmount); }
    public String getPaySelf() { return withPrefix(paySelfNotAllowed); }

    public String getEcoGive(String player, String amount) {
        return withPrefix(ecoGive
            .replace("{player}", player)
            .replace("{amount}", amount));
    }
    public String getEcoTake(String player, String amount) {
        return withPrefix(ecoTake
            .replace("{player}", player)
            .replace("{amount}", amount));
    }
    public String getEcoSet(String player, String balance) {
        return withPrefix(ecoSet
            .replace("{player}", player)
            .replace("{balance}", balance));
    }
    public String getEcoReset(String player, String balance) {
        return withPrefix(ecoReset
            .replace("{player}", player)
            .replace("{balance}", balance));
    }

    public String getBalTopHeader(int page) {
        return format(baltopHeader + " (Page " + page + ")");
    }
    public String getBalTopEntry(int position, String player, String balance) {
        return format(baltopEntry
            .replace("{position}", String.valueOf(position))
            .replace("{player}", player)
            .replace("{balance}", balance));
    }
    public String getBalTopEmpty() { return format(baltopEmpty); }

    public String getErrorGeneric() { return withPrefix(errorGeneral); }
}
