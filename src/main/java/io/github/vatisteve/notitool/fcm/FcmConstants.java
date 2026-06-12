package io.github.vatisteve.notitool.fcm;

/**
 * Firebase Cloud Messaging platform limits.
 *
 * <p>These are hard limits imposed by the FCM service; the managers use them to split large
 * requests into compliant batches. See the
 * <a href="https://firebase.google.com/docs/cloud-messaging">FCM documentation</a>.
 *
 * @author vatisteve
 * @since 0.2.0
 */
public final class FcmConstants {

    private FcmConstants() {
        throw new AssertionError("No io.github.vatisteve.notitool.fcm.FcmConstants instances for you!");
    }

    /** Maximum number of devices in a single (legacy) device group. */
    public static final int MAX_DEVICE_PER_GROUP = 20;

    /** Maximum number of registration tokens accepted by a single multicast send. */
    public static final int MAX_DEVICE_PER_MULTICAST = 500;

    /** Maximum number of registration tokens accepted by a single subscribe/unsubscribe request. */
    public static final int MAX_SUBSCRIBE_DEVICE_PER_REQUEST = 1000;

    /** Maximum number of topics a single device may be subscribed to. */
    public static final int MAX_SUBSCRIBED_TOPIC_PER_DEVICE = 2000;

}
