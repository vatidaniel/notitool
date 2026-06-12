package io.github.vatisteve.notitool.fcm;

import com.google.api.core.ApiFutures;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.TopicManagementResponse;
import io.github.vatisteve.notitool.design.exceptions.NotificationException;
import io.github.vatisteve.notitool.fcm.domain.FcmDevice;
import io.github.vatisteve.notitool.fcm.support.TestDevice;
import io.github.vatisteve.notitool.fcm.support.TestTopic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FcmTopicManagerTest {

    @Mock
    FirebaseMessaging fcm;

    FcmTopicManager manager;

    @BeforeEach
    void setUp() {
        manager = new FcmTopicManager(fcm);
    }

    @Test
    void subscribe_inactiveDevice_skipsFirebase() throws Exception {
        FcmTopicResponse response = manager.subscribe(TestTopic.of("news"), TestDevice.inactive("token"));

        assertEquals(0, response.getSuccessCount());
        verifyNoInteractions(fcm);
    }

    @Test
    void subscribe_splitsInto1000SizedChunksAndAggregates() throws Exception {
        TopicManagementResponse fcmResponse = mock(TopicManagementResponse.class);
        when(fcmResponse.getSuccessCount()).thenReturn(100);
        when(fcmResponse.getFailureCount()).thenReturn(0);
        when(fcm.subscribeToTopic(anyList(), eq("news"))).thenReturn(fcmResponse);

        FcmTopicResponse response = manager.subscribe(TestTopic.of("news"), activeDevices(2500));

        // 2500 / 1000 -> 3 chunks of 1000, 1000, 500
        verify(fcm, times(3)).subscribeToTopic(anyList(), eq("news"));
        assertEquals(300, response.getSuccessCount());
    }

    @Test
    void subscribe_filtersInactiveDevices() throws Exception {
        TopicManagementResponse fcmResponse = mock(TopicManagementResponse.class);
        when(fcmResponse.getSuccessCount()).thenReturn(1);
        when(fcmResponse.getFailureCount()).thenReturn(0);
        when(fcm.subscribeToTopic(anyList(), anyString())).thenReturn(fcmResponse);

        List<FcmDevice> devices = List.of(TestDevice.active("a"), TestDevice.inactive("b"));
        manager.subscribe(TestTopic.of("news"), devices);

        // only one chunk, containing a single (active) token
        verify(fcm).subscribeToTopic(List.of("a"), "news");
    }

    @Test
    void unsubscribe_doesNotFilterInactiveDevices() throws Exception {
        TopicManagementResponse fcmResponse = mock(TopicManagementResponse.class);
        when(fcmResponse.getSuccessCount()).thenReturn(2);
        when(fcmResponse.getFailureCount()).thenReturn(0);
        when(fcm.unsubscribeFromTopic(anyList(), anyString())).thenReturn(fcmResponse);

        List<FcmDevice> devices = List.of(TestDevice.active("a"), TestDevice.inactive("b"));
        manager.unsubscribe(TestTopic.of("news"), devices);

        verify(fcm).unsubscribeFromTopic(List.of("a", "b"), "news");
    }

    @Test
    void subscribe_firebaseError_isWrappedAsNotificationException() throws Exception {
        FirebaseMessagingException boom = mock(FirebaseMessagingException.class);
        when(boom.getMessage()).thenReturn("boom");
        when(fcm.subscribeToTopic(anyList(), anyString())).thenThrow(boom);

        NotificationException ex = assertThrows(NotificationException.class,
                () -> manager.subscribe(TestTopic.of("news"), activeDevices(3)));
        assertEquals("boom", ex.getMessage());
    }

    @Test
    void subscribeAsync_aggregatesAcrossChunks() throws Exception {
        TopicManagementResponse fcmResponse = mock(TopicManagementResponse.class);
        when(fcmResponse.getSuccessCount()).thenReturn(50);
        when(fcmResponse.getFailureCount()).thenReturn(0);
        when(fcm.subscribeToTopicAsync(anyList(), anyString()))
                .thenReturn(ApiFutures.immediateFuture(fcmResponse));

        var response = manager.subscribeAsync(TestTopic.of("news"), activeDevices(2001)).get();

        // 2001 / 1000 -> 3 chunks
        verify(fcm, times(3)).subscribeToTopicAsync(anyList(), anyString());
        assertEquals(150, response.getSuccessCount());
    }

    private static List<FcmDevice> activeDevices(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> (FcmDevice) TestDevice.active("token-" + i))
                .toList();
    }
}
