package dev.karmabyte.ledger.api.util;

import org.jetbrains.annotations.NotNull;

/**
 * A three-state boolean value: TRUE, FALSE, or UNDEFINED.
 *
 * <p>Useful for configuration options that can inherit from a parent setting.
 *
 * @author KarmaByte
 * @since 1.0.0
 */
public enum TriState {

    /**
     * Explicitly true.
     */
    TRUE,

    /**
     * Explicitly false.
     */
    FALSE,

    /**
     * Not set, inherit from default.
     */
    UNDEFINED;

    /**
     * Convert a boolean to a TriState.
     *
     * @param value the boolean value
     * @return TRUE or FALSE
     */
    @NotNull
    public static TriState of(boolean value) {
        return value ? TRUE : FALSE;
    }

    /**
     * Convert a nullable Boolean to a TriState.
     *
     * @param value the Boolean value, or null
     * @return TRUE, FALSE, or UNDEFINED
     */
    @NotNull
    public static TriState of(Boolean value) {
        if (value == null) {
            return UNDEFINED;
        }
        return value ? TRUE : FALSE;
    }

    /**
     * Get as boolean, treating UNDEFINED as false.
     *
     * @return true if TRUE
     */
    public boolean asBoolean() {
        return this == TRUE;
    }

    /**
     * Get as boolean with a default for UNDEFINED.
     *
     * @param defaultValue value to use if UNDEFINED
     * @return the boolean value
     */
    public boolean asBoolean(boolean defaultValue) {
        return switch (this) {
            case TRUE -> true;
            case FALSE -> false;
            case UNDEFINED -> defaultValue;
        };
    }

    /**
     * Get as nullable Boolean.
     *
     * @return true, false, or null if UNDEFINED
     */
    public Boolean asNullableBoolean() {
        return switch (this) {
            case TRUE -> true;
            case FALSE -> false;
            case UNDEFINED -> null;
        };
    }

    /**
     * Check if this is not UNDEFINED.
     *
     * @return true if TRUE or FALSE
     */
    public boolean isDefined() {
        return this != UNDEFINED;
    }
}
