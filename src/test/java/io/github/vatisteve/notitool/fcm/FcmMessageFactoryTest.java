package io.github.vatisteve.notitool.fcm;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import io.github.vatisteve.notitool.fcm.domain.FcmDeviceType;
import io.github.vatisteve.notitool.fcm.support.TestDevice;
import io.github.vatisteve.notitool.fcm.support.TestNotification;
import io.github.vatisteve.notitool.fcm.support.TestTopic;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class FcmMessageFactoryTest {

    private final FcmMessageFactory factory = new FcmMessageFactory();

    @Test
    void createMessage_dispatchesByDeviceType() {
        for (FcmDeviceType type : FcmDeviceType.values()) {
            Message message = factory.createMessage(TestNotification.simple(), TestDevice.active("token", type));
            assertNotNull(message, "message should be built for device type " + type);
        }
    }

    @Test
    void createMessage_nullDeviceType_buildsAllPlatformsMessage() {
        Message message = factory.createMessage(TestNotification.simple(), TestDevice.active("token"));
        assertNotNull(message);
    }

    @Test
    void createMessage_withDataPayload_doesNotThrow() {
        TestNotification notification = TestNotification.withData(Map.of("deepLink", "app://home", "id", "42"));
        assertNotNull(factory.createMessage(notification, TestDevice.active("token")));
        for (FcmDeviceType type : FcmDeviceType.values()) {
            assertNotNull(factory.createMessage(notification, TestDevice.active("token", type)));
        }
    }

    @Test
    void createMessage_forTopic_buildsMessage() {
        Message message = factory.createMessage(TestNotification.simple(), TestTopic.of("news"));
        assertNotNull(message);
    }

    @Test
    void createMulticastMessage_buildsMessage() {
        MulticastMessage message = factory.createMulticastMessage(TestNotification.simple(),
                List.of(TestDevice.active("a"), TestDevice.active("b")));
        assertNotNull(message);
    }
}
