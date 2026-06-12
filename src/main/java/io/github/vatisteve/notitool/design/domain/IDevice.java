package io.github.vatisteve.notitool.design.domain;

/**
 *  IDevice
 *
 * @author      vatidaniel
 * @since       Jun 25, 2023
 *
 * @param <K>   the device token type 
 * @param <T>   the {@link IDeviceType}
 */
public interface IDevice<K, T extends IDeviceType> {

    /**
     * @return the device token
     */
    K getDeviceToken();

    /**
     * deactivate this device
     */
    void deactivate();

    /**
     * activate this device
     */
    void activate();

    /**
     * @return the device type
     */
    T getDeviceType();

    /**
     * @return this device is activating or not
     */
    boolean isActive();
}
