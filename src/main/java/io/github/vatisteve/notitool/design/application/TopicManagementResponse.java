package io.github.vatisteve.notitool.design.application;

/**
 * Topic management response model.
 *
 * <p>Exposes the outcome of a subscribe / unsubscribe operation.
 *
 * @author vatisteve
 * @since 0.1.0
 */
public interface TopicManagementResponse {

    /**
     * @return number of tokens successfully subscribed / unsubscribed
     */
    int getSuccessCount();

    /**
     * @return number of tokens that failed to subscribe / unsubscribe
     */
    int getFailureCount();

    /**
     * @return {@code true} if nothing failed
     */
    default boolean isSuccessful() {
        return getFailureCount() == 0;
    }

}
