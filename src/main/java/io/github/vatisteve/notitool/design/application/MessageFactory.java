package io.github.vatisteve.notitool.design.application;

import io.github.vatisteve.notitool.design.domain.IDevice;
import io.github.vatisteve.notitool.design.domain.INotification;
import io.github.vatisteve.notitool.design.domain.ITopic;

import java.util.List;

/**
 * MessageFactory
 *
 * @author      vatidaniel
 * @since       Jul 20, 2023
 *
 * @param <N>   the {@link INotification}
 * @param <M>   the message class
 * @param <A>   the multicast message class
 * @param <D>   the {@link IDevice}
 * @param <T>   the {@link ITopic}
 */
public interface MessageFactory<N extends INotification, M, A, D extends IDevice<?, ?>, T extends ITopic<?>> {

    /**
     * Create new message
     * @param notification the {@link INotification}
     * @param device the {@link IDevice}
     * @return new message instance
     */
    M createMessage(N notification, D device);

    /**
     * Create new multicast message
     * @param notification the {@link INotification}
     * @param devices a list of {@link IDevice}
     * @return new multicast message instance
     */
    A createMulticastMessage(N notification, List<D> devices);

    /**
     * Create new message with specific topic
     * @param notification the {@link INotification}
     * @param topic the {@link ITopic}
     * @return new message instance
     */
    M createMessage(N notification, T topic);

}
