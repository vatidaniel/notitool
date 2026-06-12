package io.github.vatisteve.notitool.fcm;

import io.github.vatisteve.notitool.design.application.TopicManagementResponse;

/**
 * @author vatidaniel
 * @since 0.1.0
 */
public record FcmTopicResponse(int successCount, int failureCount) implements TopicManagementResponse {

    /**
     * Aggregate the counts of two responses, used when a subscribe / unsubscribe request is split
     * into several FCM-sized chunks.
     *
     * @param other the response to add to this one
     * @return a new {@link FcmTopicResponse} holding the summed counts
     */
    public FcmTopicResponse merge(FcmTopicResponse other) {
        return new FcmTopicResponse(this.successCount + other.successCount,
                this.failureCount + other.failureCount);
    }

    @Override
    public int getSuccessCount() {
        return successCount;
    }

    @Override
    public int getFailureCount() {
        return failureCount;
    }
}
