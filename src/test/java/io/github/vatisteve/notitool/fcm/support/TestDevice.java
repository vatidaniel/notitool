package io.github.vatisteve.notitool.fcm.support;

import io.github.vatisteve.notitool.fcm.domain.FcmDevice;
import io.github.vatisteve.notitool.fcm.domain.FcmDeviceType;

/**
 * Simple mutable {@link FcmDevice} test fixture.
 */
public class TestDevice implements FcmDevice {

    private final String token;
    private final FcmDeviceType type;
    private boolean active;

    public TestDevice(String token, FcmDeviceType type, boolean active) {
        this.token = token;
        this.type = type;
        this.active = active;
    }

    public static TestDevice active(String token) {
        return new TestDevice(token, null, true);
    }

    public static TestDevice active(String token, FcmDeviceType type) {
        return new TestDevice(token, type, true);
    }

    public static TestDevice inactive(String token) {
        return new TestDevice(token, null, false);
    }

    @Override
    public String getDeviceToken() {
        return token;
    }

    @Override
    public void deactivate() {
        this.active = false;
    }

    @Override
    public void activate() {
        this.active = true;
    }

    @Override
    public FcmDeviceType getDeviceType() {
        return type;
    }

    @Override
    public boolean isActive() {
        return active;
    }
}
