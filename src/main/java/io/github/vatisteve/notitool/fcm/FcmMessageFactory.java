package io.github.vatisteve.notitool.fcm;

import com.google.firebase.messaging.*;
import io.github.vatisteve.notitool.design.application.MessageFactory;
import io.github.vatisteve.notitool.fcm.domain.FcmDevice;
import io.github.vatisteve.notitool.fcm.domain.FcmNotification;
import io.github.vatisteve.notitool.fcm.domain.FcmTopic;

import java.util.List;

/**
 * @author vatisteve
 * @since 0.1.0
 */
public class FcmMessageFactory implements MessageFactory<FcmNotification, Message, MulticastMessage, FcmDevice, FcmTopic> {

    public Message androidMessageForDeviceToken(FcmNotification.AndroidNotificationData and, String token) {
        return Message.builder()
                .setAndroidConfig(getAndroidConfig(and))
                .putAllData(and.data())
                .setToken(token)
                .build();
    }

    public Message androidMessageForTopic(FcmNotification.AndroidNotificationData and, String topic) {
        return Message.builder()
                .setAndroidConfig(getAndroidConfig(and))
                .putAllData(and.data())
                .setTopic(topic)
                .build();
    }

    private AndroidConfig getAndroidConfig(FcmNotification.AndroidNotificationData and) {
        return AndroidConfig.builder()
                .setTtl(and.ttlMillis())
                .setPriority(and.priority())
                .setNotification(AndroidNotification.builder()
                        .setTitle(and.title())
                        .setBody(and.body())
                        .setIcon(and.icon())
                        .setColor(and.color())
                        .build())
                .build();
    }

    public Message apnsMessageForDeviceToken(FcmNotification.ApnsNotificationData ind, String token) {
        return Message.builder()
                .setApnsConfig(getApnsConfig(ind))
                .putAllData(ind.data())
                .setToken(token)
                .build();
    }

    public Message apnsMessageForTopic(FcmNotification.ApnsNotificationData ind, String topic) {
        return Message.builder()
                .setApnsConfig(getApnsConfig(ind))
                .putAllData(ind.data())
                .setTopic(topic)
                .build();
    }

    private ApnsConfig getApnsConfig(FcmNotification.ApnsNotificationData ind) {
        return ApnsConfig.builder()
                .putHeader("apns-priority", "10")   // immediately
                .setAps(Aps.builder()
                        .setAlert(ApsAlert.builder()
                                .setTitle(ind.title())
                                .setBody(ind.body())
                                .build())
                        .setBadge(ind.badge())
                        .build())
                .build();
    }

    public Message webPushMessageForDeviceToken(FcmNotification.WebNotificationData wnd, String token) {
        return Message.builder()
                .setWebpushConfig(getWebpushConfig(wnd))
                .putAllData(wnd.data())
                .setToken(token)
                .build();
    }

    public Message webPushMessageForTopic(FcmNotification.WebNotificationData wnd, String topic) {
        return Message.builder()
                .setWebpushConfig(getWebpushConfig(wnd))
                .putAllData(wnd.data())
                .setTopic(topic)
                .build();
    }

    private WebpushConfig getWebpushConfig(FcmNotification.WebNotificationData wnd) {
        return WebpushConfig.builder()
                .setNotification(new WebpushNotification(wnd.title(), wnd.body(), wnd.icon()))
                .build();
    }

    public Message allPlatformsMessageForDeviceToken(FcmNotification nd, String token) {
        return Message.builder()
                .setNotification(getAllPlatformsMessageConfig(nd))
                .setAndroidConfig(getAndroidConfig(nd.getAndroidNotificationData()))
                .setApnsConfig(getApnsConfig(nd.getApnsNotificationData()))
                .setWebpushConfig(getWebpushConfig(nd.getWebNotificationData()))
                .putAllData(nd.getData())
                .setToken(token)
                .build();
    }

    public Message allPlatformsMessageForTopic(FcmNotification nd, String topic) {
        return Message.builder()
                .setNotification(getAllPlatformsMessageConfig(nd))
                .setAndroidConfig(getAndroidConfig(nd.getAndroidNotificationData()))
                .setApnsConfig(getApnsConfig(nd.getApnsNotificationData()))
                .setWebpushConfig(getWebpushConfig(nd.getWebNotificationData()))
                .putAllData(nd.getData())
                .setTopic(topic)
                .build();
    }

    public Notification getAllPlatformsMessageConfig(FcmNotification nd) {
        return Notification.builder()
                .setTitle(nd.getTitle())
                .setBody(nd.getBody())
                .build();
    }

    @Override
    public Message createMessage(FcmNotification notification, FcmDevice device) {
        if (device.getDeviceType() == null)  {
            return allPlatformsMessageForDeviceToken(notification, device.getDeviceToken());
        }
        return switch(device.getDeviceType()) {
        case ANDROID -> androidMessageForDeviceToken(notification.getAndroidNotificationData(), device.getDeviceToken());
        case IOS -> apnsMessageForDeviceToken(notification.getApnsNotificationData(), device.getDeviceToken());
        case WEB -> webPushMessageForDeviceToken(notification.getWebNotificationData(), device.getDeviceToken());
        };
    }

    @Override
    public MulticastMessage createMulticastMessage(FcmNotification notification, List<FcmDevice> devices) {
        return MulticastMessage.builder()
                .setNotification(getAllPlatformsMessageConfig(notification))
                .setAndroidConfig(getAndroidConfig(notification.getAndroidNotificationData()))
                .setApnsConfig(getApnsConfig(notification.getApnsNotificationData()))
                .setWebpushConfig(getWebpushConfig(notification.getWebNotificationData()))
                .putAllData(notification.getData())
                .addAllTokens(devices.stream().map(FcmDevice::getDeviceToken).toList())
                .build();
    }

    @Override
    public Message createMessage(FcmNotification notification, FcmTopic topic) {
        return allPlatformsMessageForTopic(notification, topic.getTopicIdentifier());
    }

}
