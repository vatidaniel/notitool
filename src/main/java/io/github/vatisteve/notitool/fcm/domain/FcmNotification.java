package io.github.vatisteve.notitool.fcm.domain;

import com.google.firebase.messaging.AndroidConfig.Priority;
import io.github.vatisteve.notitool.design.domain.INotification;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

/**
 * @author vatisteve
 * @since 0.1.0
 */
public interface FcmNotification extends INotification {

    String getTitle();
    String getBody();
    String getIcon();
    String getColor();

    /**
     * Custom key/value data payload delivered alongside the notification (e.g. a deep link or
     * entity id the client app reads). Empty by default.
     * @return the data payload, never {@code null}
     */
    default Map<String, String> getData() {
        return Collections.emptyMap();
    }

    /**
     * @return the Android delivery priority, {@link Priority#NORMAL} by default
     */
    default Priority getPriority() {
        return Priority.NORMAL;
    }

    /**
     * @return how long FCM should retain the message if the device is offline, 1 hour by default
     */
    default Duration getTimeToLive() {
        return Duration.ofHours(1);
    }

    /**
     * @return the iOS badge count, {@code 0} by default
     */
    default int getBadge() {
        return 0;
    }

    default AndroidNotificationData getAndroidNotificationData() {
        return new AndroidNotificationData(getPriority(), getTimeToLive().toMillis(),
                getTitle(), getIcon(), getBody(), getColor(), getData());
    }

    default ApnsNotificationData getApnsNotificationData() {
        return new ApnsNotificationData(getTitle(), getBody(), getBadge(), getData());
    }

    default WebNotificationData getWebNotificationData() {
        return new WebNotificationData(getTitle(), getBody(), getIcon(), getData());
    }

    record AndroidNotificationData(Priority priority, long ttlMillis, String title, String icon, String body,
                                   String color, Map<String, String> data) {}
    record ApnsNotificationData(String title, String body, int badge, Map<String, String> data) {}
    record WebNotificationData(String title, String body, String icon, Map<String, String> data) {}

}
