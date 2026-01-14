# Ledger

**The Economy API for Hytale Servers**

[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Java 25](https://img.shields.io/badge/Java-25-blue.svg)](https://openjdk.org/)

Ledger is a modern, async-first economy API for Hytale servers. It provides a standardized interface for economy operations, similar to what Vault does for Minecraft.

## Features

- **Async by Default** - All I/O operations use `CompletableFuture` to never block the server
- **Multi-Currency Support** - Primary currency + unlimited additional currencies
- **Flexible Storage** - JSON (development) or SQLite (production)
- **Rich Event System** - Pre/Post transaction events with cancel/modify support
- **Transaction Logging** - Full audit trail of all economy operations
- **Payment Notifications** - Players receive messages when they get paid
- **Easy Integration** - Simple API for other mods to integrate

## Installation

1. Download `Ledger-1.0.0.jar` from [Releases]([https://github.com/KarmaByte-SL/Ledger/releases](https://github.com/KarmaByte/Ledger/releases/tag/v1.0.0))
2. Place in your Hytale server's `Mods` folder
3. Restart the server
4. Configuration files will be generated in `mods/KarmaByte_Ledger/`

## Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/balance [player]` | None / `ledger.balance.others` | Check your or another player's balance |
| `/pay <player> <amount>` | None | Send money to another player |
| `/baltop [page]` | None | View richest players |
| `/eco give <player> <amount>` | `ledger.admin` | Give money to a player |
| `/eco take <player> <amount>` | `ledger.admin` | Take money from a player |
| `/eco set <player> <amount>` | `ledger.admin` | Set a player's balance |
| `/eco reset <player>` | `ledger.admin` | Reset balance to default |
| `/ledger reload` | `ledger.admin.reload` | Reload configuration |
| `/ledger info` | `ledger.admin` | View plugin information |
| `/ledger version` | `ledger.admin` | View plugin version |

## Configuration

### config.json

```json
{
  "storageType": "sqlite",
  "currencyId": "coins",
  "currencyDisplayName": "Coins",
  "currencySingular": "coin",
  "currencyPlural": "coins",
  "currencySymbol": "$",
  "currencyDecimals": 2,
  "startingBalance": 100.0,
  "minBalance": 0.0,
  "maxBalance": 1000000000.0,
  "minPayAmount": 0.01,
  "maxPayAmount": 1000000.0,
  "payCooldownSeconds": 0,
  "transactionLogging": true,
  "debug": false
}
```

### Storage Options

| Storage | Best For | File |
|---------|----------|------|
| `json` | Development, testing | `data/*.json` |
| `sqlite` | Production servers | `ledger.db` |

## For Mod Developers

### Adding Ledger API

Add the Ledger API JAR to your project:

```kotlin
// build.gradle.kts
dependencies {
    compileOnly(files("libs/ledger-api-1.0.0.jar"))
}
```

### Basic Usage

```java
import dev.karmabyte.ledger.api.Ledger;
import dev.karmabyte.ledger.api.LedgerProvider;
import dev.karmabyte.ledger.api.economy.EconomyService;

// Get the economy provider
LedgerProvider provider = Ledger.getProvider();
EconomyService economy = provider.getEconomyService();

// Check balance
economy.getBalance(playerUuid).thenAccept(balance -> {
    System.out.println("Balance: " + balance);
});

// Deposit money
economy.deposit(playerUuid, 100.0).thenAccept(result -> {
    if (result.isSuccess()) {
        System.out.println("Deposited " + result.getAmountFormatted());
    }
});

// Withdraw money
economy.withdraw(playerUuid, 50.0).thenAccept(result -> {
    if (result.isSuccess()) {
        giveItemToPlayer(player, item);
    } else if (result.getStatus() == TransactionResult.Status.INSUFFICIENT_FUNDS) {
        player.sendMessage("Not enough money!");
    }
});

// Transfer between players
economy.transfer(fromUuid, toUuid, 25.0).thenAccept(result -> {
    if (result.isSuccess()) {
        System.out.println("Transfer complete!");
    }
});
```

### Listening to Events

Hook into the economy system to add taxes, logging, or custom behavior:

```java
// In your plugin's setup method
getEventRegistry().register(PreTransactionEvent.class, event -> {
    // Cancel large transactions
    if (event.getAmount() > 10000) {
        event.cancel("Amount exceeds server limit");
        return;
    }

    // Apply 5% tax on deposits
    if (event.getType() == TransactionType.DEPOSIT) {
        double taxed = event.getAmount() * 0.95;
        event.setAmount(taxed);
    }
});

getEventRegistry().register(PostTransactionEvent.class, event -> {
    if (event.isSuccess() && event.getType() == TransactionType.TRANSFER) {
        // Log successful transfers
        getLogger().atInfo().log("%s sent %s to someone",
            event.getAccount().getName(),
            event.getResult().getAmountFormatted());
    }
});

getEventRegistry().register(BalanceChangeEvent.class, event -> {
    // Update scoreboards when balance changes
    updatePlayerScoreboard(event.getAccount().getOwner(), event.getNewBalance());
});
```

### Registering Custom Currencies

```java
import dev.karmabyte.ledger.api.economy.Currency;
import dev.karmabyte.ledger.api.economy.CurrencyBuilder;

// Create a custom currency
Currency gems = CurrencyBuilder.create("gems")
    .displayName("Gems")
    .singular("gem")
    .plural("gems")
    .symbol("G")
    .decimals(0)
    .defaultBalance(0.0)
    .minBalance(0.0)
    .maxBalance(100000.0)
    .build();

// Register it
economy.registerCurrency(gems);

// Use the custom currency
economy.deposit(playerUuid, gems, 50.0);
economy.getBalance(playerUuid, gems).thenAccept(balance -> {
    player.sendMessage("You have " + gems.format(balance));
});
```

### API Reference

| Class | Description |
|-------|-------------|
| `Ledger` | Static entry point - `Ledger.getProvider()` |
| `LedgerProvider` | Main provider with economy service access |
| `EconomyService` | All economy operations (balance, deposit, withdraw, transfer) |
| `Account` | Player account with multi-currency balances |
| `Currency` | Currency definition with formatting |
| `TransactionResult` | Operation result with status, amounts, and messages |

### Events

| Event | Cancellable | Description |
|-------|-------------|-------------|
| `PreTransactionEvent` | Yes | Before any transaction - can cancel or modify amount |
| `PostTransactionEvent` | No | After transaction completes with result |
| `BalanceChangeEvent` | No | When any balance changes |

## Building from Source

```bash
git clone https://github.com/KarmaByte-SL/Ledger.git
cd Ledger

# Place hytale-server.jar in libs/ folder
./gradlew build
```

Output JARs:
- `api/build/libs/api-1.0.0-SNAPSHOT.jar` - API for other mods
- `core/build/libs/core-1.0.0-SNAPSHOT.jar` - Full plugin

## Contributing

Contributions are welcome!

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see [LICENSE](LICENSE) for details.

## Author

**KarmaByte** - [GitHub](https://github.com/KarmaByte-SL)

---

Made with love for the Hytale community
