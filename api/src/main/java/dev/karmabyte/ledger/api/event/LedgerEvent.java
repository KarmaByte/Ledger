package dev.karmabyte.ledger.api.event;

import com.hypixel.hytale.event.IEvent;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

/**
 * Base class for all Ledger events.
 *
 * <p>All Ledger events implement Hytale's {@link IEvent} interface,
 * allowing them to be registered and handled through Hytale's event system.
 *
 * <h2>Example - Listening to Ledger events:</h2>
 * <pre>{@code
 * plugin.getEventRegistry().register(PreTransactionEvent.class, event -> {
 *     if (event.getAmount() > 10000) {
 *         event.cancel("Amount too large");
 *     }
 * });
 * }</pre>
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public abstract class LedgerEvent implements IEvent<Void> {

    private final Instant timestamp;

    protected LedgerEvent() {
        this.timestamp = Instant.now();
    }

    /**
     * Get when this event was fired.
     *
     * @return the timestamp
     */
    @NotNull
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Get the event name.
     *
     * @return simple class name
     */
    @NotNull
    public String getEventName() {
        return getClass().getSimpleName();
    }
}
