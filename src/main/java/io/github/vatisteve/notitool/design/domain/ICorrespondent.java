package io.github.vatisteve.notitool.design.domain;

/**
 * ICorrespondent
 *
 * @author      vatidaniel
 * @since       Jul 20, 2023
 *
 * @param <T>   the correspondent identifier type
 */
public interface ICorrespondent<T> {

    /**
     * @return correspondent id
     */
    T getCorrespondentIdentifier();

}
