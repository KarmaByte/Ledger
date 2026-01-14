package dev.karmabyte.ledger.api.event;

import com.hypixel.hytale.event.ICancellable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for events that can be cancelled.
 *
 * <p>Extends Hytale's {@link ICancellable} interface for compatibility
 * with the native event system, while adding support for cancel reasons.
 *
 * <p><strong>Note:</strong> Implementing classes must store the cancel reason
 * themselves by overriding {@link #cancel(String)} and {@link #getCancelReason()}.
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public interface Cancellable extends ICancellable {

    /**
     * Get the reason for cancellation.
     *
     * <p>Implementing classes must store and return the reason
     * set by {@link #cancel(String)}.
     *
     * @return the reason, or null if not cancelled or no reason provided
     */
    @Nullable
    String getCancelReason();

    /**
     * Cancel this event with a reason.
     *
     * <p><strong>Important:</strong> Implementing classes should override
     * this method to store the reason. The default implementation only
     * sets the cancelled state without storing the reason.
     *
     * @param reason the reason for cancellation
     */
    void cancel(@NotNull String reason);
}
