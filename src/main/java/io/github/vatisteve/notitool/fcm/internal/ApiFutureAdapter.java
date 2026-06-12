package io.github.vatisteve.notitool.fcm.internal;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.CompletableFuture;

/**
 * Bridges Firebase's Guava-based {@link ApiFuture} to the JDK {@link CompletableFuture} so callers
 * can compose asynchronous sends with standard {@code java.util.concurrent} primitives.
 *
 * @author vatidaniel
 * @since 0.2.0
 */
public final class ApiFutureAdapter {

    private ApiFutureAdapter() {
    }

    /**
     * Wrap an {@link ApiFuture} in a {@link CompletableFuture}. The callback runs on the
     * {@link MoreExecutors#directExecutor() direct executor}, i.e. on whichever thread completes the
     * underlying future, so no extra thread pool is introduced.
     *
     * @param apiFuture the Firebase future to adapt
     * @param <T>       the result type
     * @return a {@link CompletableFuture} that completes (or fails) with the same outcome
     */
    public static <T> CompletableFuture<T> toCompletable(ApiFuture<T> apiFuture) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        ApiFutures.addCallback(apiFuture, new ApiFutureCallback<>() {
            @Override
            public void onFailure(Throwable t) {
                completableFuture.completeExceptionally(t);
            }

            @Override
            public void onSuccess(T result) {
                completableFuture.complete(result);
            }
        }, MoreExecutors.directExecutor());
        return completableFuture;
    }

}
