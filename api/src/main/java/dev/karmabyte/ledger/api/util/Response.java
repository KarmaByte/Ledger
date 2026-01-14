package dev.karmabyte.ledger.api.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Generic response wrapper for operations that may succeed or fail.
 *
 * <p>Similar to {@link java.util.Optional} but includes error information.
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * Response<Account> response = getAccount(playerId);
 *
 * response.ifSuccess(account -> {
 *     // Use the account
 * }).ifFailure(error -> {
 *     logger.warning("Failed: " + error);
 * });
 *
 * // Or with map/flatMap
 * Response<Double> balance = response.map(Account::getBalance);
 * }</pre>
 *
 * @param <T> the type of value on success
 * @author KarmaByte
 * @since 1.0.0
 */
public final class Response<T> {

    private final T value;
    private final String error;
    private final boolean success;

    private Response(@Nullable T value, @Nullable String error, boolean success) {
        this.value = value;
        this.error = error;
        this.success = success;
    }

    /**
     * Create a successful response.
     *
     * @param value the result value
     * @param <T> the value type
     * @return success response
     */
    @NotNull
    public static <T> Response<T> success(@NotNull T value) {
        return new Response<>(value, null, true);
    }

    /**
     * Create a successful response with no value.
     *
     * @param <T> the value type
     * @return success response
     */
    @NotNull
    public static <T> Response<T> success() {
        return new Response<>(null, null, true);
    }

    /**
     * Create a failure response.
     *
     * @param error the error message
     * @param <T> the value type
     * @return failure response
     */
    @NotNull
    public static <T> Response<T> failure(@NotNull String error) {
        return new Response<>(null, error, false);
    }

    /**
     * Create a failure response from an exception.
     *
     * @param exception the exception
     * @param <T> the value type
     * @return failure response
     */
    @NotNull
    public static <T> Response<T> failure(@NotNull Throwable exception) {
        return new Response<>(null, exception.getMessage(), false);
    }

    /**
     * Check if this response is successful.
     *
     * @return true if success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Check if this response is a failure.
     *
     * @return true if failure
     */
    public boolean isFailure() {
        return !success;
    }

    /**
     * Get the value if successful.
     *
     * @return optional value
     */
    @NotNull
    public Optional<T> getValue() {
        return Optional.ofNullable(value);
    }

    /**
     * Get the value or throw.
     *
     * @return the value
     * @throws IllegalStateException if failure
     */
    @NotNull
    public T getOrThrow() {
        if (!success || value == null) {
            throw new IllegalStateException(error != null ? error : "Operation failed");
        }
        return value;
    }

    /**
     * Get the value or a default.
     *
     * @param defaultValue the default
     * @return value or default
     */
    @Nullable
    public T getOrElse(@Nullable T defaultValue) {
        return success && value != null ? value : defaultValue;
    }

    /**
     * Get the error message if failure.
     *
     * @return optional error
     */
    @NotNull
    public Optional<String> getError() {
        return Optional.ofNullable(error);
    }

    /**
     * Execute action if successful.
     *
     * @param action the action
     * @return this response for chaining
     */
    @NotNull
    public Response<T> ifSuccess(@NotNull Consumer<T> action) {
        if (success && value != null) {
            action.accept(value);
        }
        return this;
    }

    /**
     * Execute action if failure.
     *
     * @param action the action with error message
     * @return this response for chaining
     */
    @NotNull
    public Response<T> ifFailure(@NotNull Consumer<String> action) {
        if (!success) {
            action.accept(error != null ? error : "Unknown error");
        }
        return this;
    }

    /**
     * Map the value if successful.
     *
     * @param mapper the mapping function
     * @param <U> the new type
     * @return mapped response
     */
    @NotNull
    public <U> Response<U> map(@NotNull Function<T, U> mapper) {
        if (success && value != null) {
            return Response.success(mapper.apply(value));
        }
        return Response.failure(error != null ? error : "No value");
    }

    /**
     * FlatMap the value if successful.
     *
     * @param mapper the mapping function
     * @param <U> the new type
     * @return mapped response
     */
    @NotNull
    public <U> Response<U> flatMap(@NotNull Function<T, Response<U>> mapper) {
        if (success && value != null) {
            return mapper.apply(value);
        }
        return Response.failure(error != null ? error : "No value");
    }

    @Override
    public String toString() {
        if (success) {
            return "Response.success(" + value + ")";
        } else {
            return "Response.failure(" + error + ")";
        }
    }
}
