package dev.karmabyte.ledger.core;

import dev.karmabyte.ledger.api.LedgerProvider;
import dev.karmabyte.ledger.api.economy.EconomyService;
import dev.karmabyte.ledger.core.economy.SimpleEconomyService;
import org.jetbrains.annotations.NotNull;

/**
 * Default implementation of the Ledger provider.
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public class LedgerProviderImpl implements LedgerProvider {

    private final LedgerPlugin plugin;
    private final SimpleEconomyService economyService;
    private boolean enabled = true;

    public LedgerProviderImpl(LedgerPlugin plugin, SimpleEconomyService economyService) {
        this.plugin = plugin;
        this.economyService = economyService;
    }

    @Override
    @NotNull
    public String getName() {
        return "Ledger";
    }

    @Override
    @NotNull
    public String getVersion() {
        return plugin.getVersion();
    }

    @Override
    @NotNull
    public EconomyService getEconomyService() {
        return economyService;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void onDisable() {
        enabled = false;
    }
}
