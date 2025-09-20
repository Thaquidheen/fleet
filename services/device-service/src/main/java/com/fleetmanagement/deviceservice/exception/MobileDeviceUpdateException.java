
package com.fleetmanagement.deviceservice.exception;

/**
 * Mobile Device Update Exception
 */
public class MobileDeviceUpdateException extends MobileDeviceException {
    public MobileDeviceUpdateException(String message) {
        super(message);
    }

    public MobileDeviceUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}