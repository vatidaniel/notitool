package io.github.vatisteve.notitool.design.domain;

/**
 *  ITopic
 *
 * @author      vatidaniel
 * @since       Jul 22, 2023
 *
 * @param <T>   the topic identifier type
 */
public interface ITopic<T> {

    /**
     * @return topic id
     */
    T getTopicIdentifier();

}
