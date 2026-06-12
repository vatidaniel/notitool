package io.github.vatisteve.notitool.design.application;

/**
 * Message management response model.
 *
 * <p>Exposes the outcome of a send operation so callers do not have to downcast to a
 * provider-specific type to read success / failure counts.
 *
 * @author vatisteve
 * @since 0.1.0
 */
public interface MessageManagementResponse {

    /**
     * @return number of messages the push service accepted
     */
    int getSuccessCount();

    /**
     * @return number of messages the push service rejected
     */
    int getFailureCount();

    /**
     * @return {@code true} if nothing failed (note: a no-op send with zero successes is also successful)
     */
    default boolean isSuccessful() {
        return getFailureCount() == 0;
    }

}
