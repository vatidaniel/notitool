package io.github.vatisteve.notitool.fcm;

import com.google.api.core.ApiFutures;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import io.github.vatisteve.notitool.design.application.MessageManagementResponse;
import io.github.vatisteve.notitool.design.exceptions.NotificationException;
import io.github.vatisteve.notitool.fcm.domain.FcmDevice;
import io.github.vatisteve.notitool.fcm.support.TestDevice;
import io.github.vatisteve.notitool.fcm.support.TestNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FcmMessageManagerTest {

    @Mock
    FirebaseMessaging fcm;

    FcmMessageManager manager;

    @BeforeEach
    void setUp() {
        manager = new FcmMessageManager(fcm);
    }

    @Test
    void send_inactiveDevice_skipsFirebaseAndReturnsZero() throws Exception {
        FcmMessageResponse response = manager.send(TestNotification.simple(), TestDevice.inactive("token"));

        assertEquals(0, response.getSuccessCount());
        assertEquals(0, response.getFailureCount());
        verifyNoInteractions(fcm);
    }

    @Test
    void send_activeDevice_returnsMessageId() throws Exception {
        when(fcm.send(any(Message.class))).thenReturn("message-1");

        FcmMessageResponse response = manager.send(TestNotification.simple(), TestDevice.active("token"));

        assertEquals("message-1", response.getMessageId());
        assertEquals(1, response.getSuccessCount());
    }

    @Test
    void send_firebaseError_isWrappedAsNotificationException() throws Exception {
        FirebaseMessagingException boom = mock(FirebaseMessagingException.class);
        when(boom.getMessage()).thenReturn("boom");
        when(fcm.send(any(Message.class))).thenThrow(boom);

        NotificationException ex = assertThrows(NotificationException.class,
                () -> manager.send(TestNotification.simple(), TestDevice.active("token")));
        assertEquals("boom", ex.getMessage());
    }

    @Test
    void sendMulticast_splitsInto500SizedChunksAndAggregates() throws Exception {
        BatchResponse batchResponse = mock(BatchResponse.class);
        when(batchResponse.getSuccessCount()).thenReturn(10);
        when(batchResponse.getFailureCount()).thenReturn(1);
        when(fcm.sendEachForMulticast(any(MulticastMessage.class))).thenReturn(batchResponse);

        List<FcmDevice> devices = activeDevices(1200);

        FcmMessageResponse response = manager.sendMulticast(TestNotification.simple(), devices);

        // 1200 / 500 -> 3 chunks of 500, 500, 200
        verify(fcm, times(3)).sendEachForMulticast(any(MulticastMessage.class));
        assertEquals(30, response.getSuccessCount());
        assertEquals(3, response.getFailureCount());
    }

    @Test
    void sendMulticast_allInactive_doesNotCallFirebase() throws Exception {
        List<FcmDevice> devices = IntStream.range(0, 50)
                .mapToObj(i -> (FcmDevice) TestDevice.inactive("token-" + i))
                .toList();

        FcmMessageResponse response = manager.sendMulticast(TestNotification.simple(), devices);

        assertEquals(0, response.getSuccessCount());
        verify(fcm, never()).sendEachForMulticast(any(MulticastMessage.class));
    }

    @Test
    void sendAsync_completesWithMessageId() throws Exception {
        when(fcm.sendAsync(any(Message.class))).thenReturn(ApiFutures.immediateFuture("async-1"));

        MessageManagementResponse response = manager.sendAsync(TestNotification.simple(), TestDevice.active("token"))
                .get();

        assertEquals(1, response.getSuccessCount());
        assertEquals("async-1", ((FcmMessageResponse) response).getMessageId());
    }

    @Test
    void sendMulticastAsync_aggregatesAcrossChunks() throws Exception {
        BatchResponse batchResponse = mock(BatchResponse.class);
        when(batchResponse.getSuccessCount()).thenReturn(7);
        when(batchResponse.getFailureCount()).thenReturn(0);
        when(fcm.sendEachForMulticastAsync(any(MulticastMessage.class)))
                .thenReturn(ApiFutures.immediateFuture(batchResponse));

        MessageManagementResponse response = manager.sendMulticastAsync(TestNotification.simple(), activeDevices(1001))
                .get();

        // 1001 / 500 -> 3 chunks
        verify(fcm, times(3)).sendEachForMulticastAsync(any(MulticastMessage.class));
        assertEquals(21, response.getSuccessCount());
    }

    private static List<FcmDevice> activeDevices(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> (FcmDevice) TestDevice.active("token-" + i))
                .toList();
    }
}
