
package com.fleetmanagement.deviceservice.exception;


/**
 * Mobile Device Registration Exception
 */
public class MobileDeviceRegistrationException extends MobileDeviceException {
    public MobileDeviceRegistrationException(String message) {
        super(message);
    }

    public MobileDeviceRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}