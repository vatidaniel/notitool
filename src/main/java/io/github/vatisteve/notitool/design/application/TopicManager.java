package io.github.vatisteve.notitool.design.application;

import io.github.vatisteve.notitool.design.domain.IDevice;
import io.github.vatisteve.notitool.design.domain.ITopic;
import io.github.vatisteve.notitool.design.exceptions.NotificationException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 *  TopicManager
 *
 * @author      tinhnv
 * @since       Jul 16, 2023
 *
 * @param <T>   the {@link ITopic}
 * @param <D>   the {@link IDevice}
 */
public interface TopicManager<T extends ITopic<?>, D extends IDevice<?, ?>> {

    /**
     * Subscribe device to topic
     * @param topic the {@link ITopic}
     * @param device the {@link IDevice}
     * @return the {@link TopicManagementResponse}
     * @throws NotificationException error occurred when subscribe device to topic
     */
    TopicManagementResponse subscribe(T topic, D device) throws NotificationException;

    /**
     * Subscribe a list of device to topic
     * @param topic the {@link ITopic}
     * @param devices the {@link IDevice}
     * @return the {@link TopicManagementResponse}
     * @throws NotificationException if have any error occurred when subscribe device to topic
     */
    TopicManagementResponse subscribe(T topic, List<D> devices) throws NotificationException;

    /**
     * Unsubscribe device from topic
     * @param topic the {@link ITopic}
     * @param device the {@link IDevice}
     * @return the {@link TopicManagementResponse}
     * @throws NotificationException error occurred when unsubscribe device to topic
     */
    TopicManagementResponse unsubscribe(T topic, D device) throws NotificationException;

    /**
     * Unsubscribe a list of device from topic
     * @param topic the {@link ITopic}
     * @param devices the {@link IDevice}
     * @return the {@link TopicManagementResponse}
     * @throws NotificationException if have any error occurred when subscribe device to topic
     */
    TopicManagementResponse unsubscribe(T topic, List<D> devices) throws NotificationException;

    /**
     * Asynchronous, non-blocking variant of {@link #subscribe(ITopic, List)}.
     * @param topic the {@link ITopic}
     * @param devices the {@link IDevice}
     * @return a future completing with the aggregated {@link TopicManagementResponse}
     */
    CompletableFuture<TopicManagementResponse> subscribeAsync(T topic, List<D> devices);

    /**
     * Asynchronous, non-blocking variant of {@link #unsubscribe(ITopic, List)}.
     * @param topic the {@link ITopic}
     * @param devices the {@link IDevice}
     * @return a future completing with the aggregated {@link TopicManagementResponse}
     */
    CompletableFuture<TopicManagementResponse> unsubscribeAsync(T topic, List<D> devices);

}
