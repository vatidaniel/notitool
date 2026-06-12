package io.github.vatisteve.notitool.fcm;

import io.github.vatisteve.notitool.design.application.MessageManagementResponse;

/**
 * @author vatisteve
 * @since 0.1.0
 */
public class FcmMessageResponse implements MessageManagementResponse {

    private final boolean batch;
    private final String messageId;
    private final int successCount;
    private final int failureCount;

    public FcmMessageResponse() {
        this.batch = false;
        this.messageId = null;
        this.successCount = 0;
        this.failureCount = 0;
    }

    public FcmMessageResponse(String messageId) {
        this.batch = false;
        this.messageId = messageId;
        this.successCount = 1;
        this.failureCount = 0;
    }

    public FcmMessageResponse(int successCount, int failureCount) {
        this.batch = true;
        this.messageId = null;
        this.successCount = successCount;
        this.failureCount = failureCount;
    }

    /**
     * Aggregate the success / failure counts of two batch responses. Used when a multicast send is
     * split into several FCM-sized chunks and the per-chunk results need to be combined into one.
     *
     * @param other the response to add to this one
     * @return a new batch {@link FcmMessageResponse} holding the summed counts
     */
    public FcmMessageResponse merge(FcmMessageResponse other) {
        return new FcmMessageResponse(this.successCount + other.successCount,
                this.failureCount + other.failureCount);
    }

    /**
     * @return is batch message or not
     */
    public boolean isBatch() {
        return batch;
    }

    /**
     * @return the messageId, or {@code null} for batch / no-op responses
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * @return the successCount
     */
    @Override
    public int getSuccessCount() {
        return successCount;
    }

    /**
     * @return the failureCount
     */
    @Override
    public int getFailureCount() {
        return failureCount;
    }

}
