package io.github.vatisteve.notitool.design.domain;

import java.util.List;

/**
 *  IDeviceGroup
 *
 * @author      vatidaniel
 * @since       Jul 22, 2023
 *
 * @param <D>   the {@link IDevice}
 */
public interface IDeviceGroup<D extends IDevice<?, ?>> {

    /**
     * @return devices in group
     */
    List<D> getDevices();

}
