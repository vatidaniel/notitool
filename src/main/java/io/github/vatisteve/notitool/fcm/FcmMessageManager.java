package io.github.vatisteve.notitool.fcm;

import com.google.common.collect.Lists;
import com.google.firebase.messaging.*;
import io.github.vatisteve.notitool.design.application.MessageManagementResponse;
import io.github.vatisteve.notitool.design.application.MessageManager;
import io.github.vatisteve.notitool.design.domain.IDevice;
import io.github.vatisteve.notitool.design.domain.IDeviceGroup;
import io.github.vatisteve.notitool.design.exceptions.NotificationException;
import io.github.vatisteve.notitool.fcm.domain.FcmDevice;
import io.github.vatisteve.notitool.fcm.domain.FcmNotification;
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
public class FcmMessageManager implements MessageManager<FcmNotification, FcmDevice, FcmTopic> {

    private static final Logger log = LoggerFactory.getLogger(FcmMessageManager.class);

    private final FirebaseMessaging fcm;
    private final FcmMessageFactory mf;

    public FcmMessageManager(FirebaseMessaging fcm) {
        this(fcm, new FcmMessageFactory());
    }

    public FcmMessageManager(FirebaseMessaging fcm, FcmMessageFactory messageFactory) {
        this.fcm = fcm;
        this.mf = messageFactory;
    }

    @Override
    public FcmMessageResponse send(FcmNotification notification, FcmDevice device) throws NotificationException {
        if (!device.isActive()) {
            log.debug("Skipping send to inactive device");
            return new FcmMessageResponse();
        }
        Message message = mf.createMessage(notification, device);
        try {
            String messageId = fcm.send(message);
            return new FcmMessageResponse(messageId);
        } catch (FirebaseMessagingException e) {
            throw new NotificationException(e.getMessage());
        }
    }

    @Override
    public FcmMessageResponse sendMulticast(FcmNotification notification, List<FcmDevice> devices)
            throws NotificationException {
        List<FcmDevice> activeDevices = devices.stream().filter(IDevice::isActive).toList();
        if (activeDevices.isEmpty()) {
            log.debug("No active devices to multicast to");
            return new FcmMessageResponse(0, 0);
        }
        FcmMessageResponse aggregate = new FcmMessageResponse(0, 0);
        for (List<FcmDevice> chunk : Lists.partition(activeDevices, FcmConstants.MAX_DEVICE_PER_MULTICAST)) {
            MulticastMessage message = mf.createMulticastMessage(notification, chunk);
            try {
                BatchResponse response = fcm.sendEachForMulticast(message);
                aggregate = aggregate.merge(new FcmMessageResponse(response.getSuccessCount(), response.getFailureCount()));
            } catch (FirebaseMessagingException e) {
                log.error("Failed to send multicast chunk of {} device(s): {}", chunk.size(), e.getMessage());
                throw new NotificationException(e.getMessage());
            }
        }
        return aggregate;
    }

    @Override
    public FcmMessageResponse sendToTopic(FcmNotification notification, FcmTopic topic)
            throws NotificationException {
        Message message = mf.createMessage(notification, topic);
        try {
            String messageId = fcm.send(message);
            return new FcmMessageResponse(messageId);
        } catch (FirebaseMessagingException e) {
            throw new NotificationException(e.getMessage());
        }
    }

    @Override
    public FcmMessageResponse sendToDeviceGroup(FcmNotification notification, IDeviceGroup<FcmDevice> group)
            throws NotificationException {
        /*
         * The management of device groups and sending messages to device groups is typically done via the app server
         *  using the FCM HTTP v1 API. The Firebase SDKs for client-side platforms such as Android, iOS, and the web
         *  do not provide methods for managing device groups or sending messages to device groups.
         */
        throw new UnsupportedOperationException("Not implement yet!");
    }

    @Override
    public CompletableFuture<MessageManagementResponse> sendAsync(FcmNotification notification, FcmDevice device) {
        if (!device.isActive()) {
            log.debug("Skipping async send to inactive device");
            return CompletableFuture.completedFuture(new FcmMessageResponse());
        }
        Message message = mf.createMessage(notification, device);
        return ApiFutureAdapter.toCompletable(fcm.sendAsync(message))
                .thenApply(messageId -> new FcmMessageResponse(messageId));
    }

    @Override
    public CompletableFuture<MessageManagementResponse> sendMulticastAsync(FcmNotification notification, List<FcmDevice> devices) {
        List<FcmDevice> activeDevices = devices.stream().filter(IDevice::isActive).toList();
        if (activeDevices.isEmpty()) {
            log.debug("No active devices to multicast to (async)");
            return CompletableFuture.completedFuture(new FcmMessageResponse(0, 0));
        }
        List<CompletableFuture<FcmMessageResponse>> chunkFutures = Lists.partition(activeDevices, FcmConstants.MAX_DEVICE_PER_MULTICAST)
                .stream()
                .map(chunk -> {
                    MulticastMessage message = mf.createMulticastMessage(notification, chunk);
                    return ApiFutureAdapter.toCompletable(fcm.sendEachForMulticastAsync(message))
                            .thenApply(response -> new FcmMessageResponse(response.getSuccessCount(), response.getFailureCount()));
                })
                .toList();
        return CompletableFuture.allOf(chunkFutures.toArray(CompletableFuture[]::new))
                .thenApply(ignored -> chunkFutures.stream()
                        .map(CompletableFuture::join)
                        .reduce(new FcmMessageResponse(0, 0), FcmMessageResponse::merge));
    }

    @Override
    public CompletableFuture<MessageManagementResponse> sendToTopicAsync(FcmNotification notification, FcmTopic topic) {
        Message message = mf.createMessage(notification, topic);
        return ApiFutureAdapter.toCompletable(fcm.sendAsync(message))
                .thenApply(messageId -> new FcmMessageResponse(messageId));
    }

}
