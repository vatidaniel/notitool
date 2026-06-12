package io.github.vatisteve.notitool.fcm;

import com.google.common.collect.Lists;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.TopicManagementResponse;
import io.github.vatisteve.notitool.design.application.TopicManager;
import io.github.vatisteve.notitool.design.domain.IDevice;
import io.github.vatisteve.notitool.design.exceptions.NotificationException;
import io.github.vatisteve.notitool.fcm.domain.FcmDevice;
import io.github.vatisteve.notitool.fcm.domain.FcmTopic;
import io.github.vatisteve.notitool.fcm.internal.ApiFutureAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author vatidaniel
 * @since 0.1.0
 */
public class FcmTopicManager implements TopicManager<FcmTopic, FcmDevice> {

    private static final Logger log = LoggerFactory.getLogger(FcmTopicManager.class);

    private final FirebaseMessaging fcm;

    public FcmTopicManager(FirebaseMessaging fcm) {
        this.fcm = fcm;
    }

    @Override
    public FcmTopicResponse subscribe(FcmTopic topic, FcmDevice device) throws NotificationException {
        if (!device.isActive()) {
            log.debug("Skipping subscribe of inactive device to topic {}", topic.getTopicIdentifier());
            return new FcmTopicResponse(0, 0);
        }
        return subscribe(topic, List.of(device));
    }

    @Override
    public FcmTopicResponse subscribe(FcmTopic topic, List<FcmDevice> devices) throws NotificationException {
        // only active devices should be subscribed
        List<String> tokens = devices.stream().filter(IDevice::isActive).map(FcmDevice::getDeviceToken).toList();
        return batchTopicOperation(tokens, topic, true);
    }

    @Override
    public FcmTopicResponse unsubscribe(FcmTopic topic, FcmDevice device) throws NotificationException {
        return unsubscribe(topic, List.of(device));
    }

    @Override
    public FcmTopicResponse unsubscribe(FcmTopic topic, List<FcmDevice> devices) throws NotificationException {
        // unsubscribe is not filtered by active state on purpose: inactive/stale tokens still need cleanup
        List<String> tokens = devices.stream().map(FcmDevice::getDeviceToken).toList();
        return batchTopicOperation(tokens, topic, false);
    }

    @Override
    public CompletableFuture<io.github.vatisteve.notitool.design.application.TopicManagementResponse> subscribeAsync(
            FcmTopic topic, List<FcmDevice> devices) {
        List<String> tokens = devices.stream().filter(IDevice::isActive).map(FcmDevice::getDeviceToken).toList();
        return batchTopicOperationAsync(tokens, topic, true);
    }

    @Override
    public CompletableFuture<io.github.vatisteve.notitool.design.application.TopicManagementResponse> unsubscribeAsync(
            FcmTopic topic, List<FcmDevice> devices) {
        List<String> tokens = devices.stream().map(FcmDevice::getDeviceToken).toList();
        return batchTopicOperationAsync(tokens, topic, false);
    }

    /**
     * Split {@code tokens} into FCM-sized chunks and subscribe / unsubscribe each, aggregating the
     * per-chunk success / failure counts.
     */
    private FcmTopicResponse batchTopicOperation(List<String> tokens, FcmTopic topic, boolean subscribe)
            throws NotificationException {
        if (tokens.isEmpty()) {
            return new FcmTopicResponse(0, 0);
        }
        FcmTopicResponse aggregate = new FcmTopicResponse(0, 0);
        for (List<String> chunk : Lists.partition(tokens, FcmConstants.MAX_SUBSCRIBE_DEVICE_PER_REQUEST)) {
            try {
                TopicManagementResponse response = subscribe
                        ? fcm.subscribeToTopic(chunk, topic.getTopicIdentifier())
                        : fcm.unsubscribeFromTopic(chunk, topic.getTopicIdentifier());
                aggregate = aggregate.merge(new FcmTopicResponse(response.getSuccessCount(), response.getFailureCount()));
            } catch (FirebaseMessagingException e) {
                log.error("Failed to {} chunk of {} token(s) for topic {}: {}",
                        subscribe ? "subscribe" : "unsubscribe", chunk.size(), topic.getTopicIdentifier(), e.getMessage());
                throw new NotificationException(e.getMessage());
            }
        }
        return aggregate;
    }

    private CompletableFuture<io.github.vatisteve.notitool.design.application.TopicManagementResponse> batchTopicOperationAsync(
            List<String> tokens, FcmTopic topic, boolean subscribe) {
        if (tokens.isEmpty()) {
            return CompletableFuture.completedFuture(new FcmTopicResponse(0, 0));
        }
        List<CompletableFuture<FcmTopicResponse>> chunkFutures = Lists.partition(tokens, FcmConstants.MAX_SUBSCRIBE_DEVICE_PER_REQUEST)
                .stream()
                .map(chunk -> ApiFutureAdapter.toCompletable(subscribe
                                ? fcm.subscribeToTopicAsync(chunk, topic.getTopicIdentifier())
                                : fcm.unsubscribeFromTopicAsync(chunk, topic.getTopicIdentifier()))
                        .thenApply(response -> new FcmTopicResponse(response.getSuccessCount(), response.getFailureCount())))
                .toList();
        return CompletableFuture.allOf(chunkFutures.toArray(CompletableFuture[]::new))
                .thenApply(ignored -> chunkFutures.stream()
                        .map(CompletableFuture::join)
                        .reduce(new FcmTopicResponse(0, 0), FcmTopicResponse::merge));
    }

}
