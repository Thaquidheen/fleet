package com.fleetmanagement.deviceservice.exception;



/**
 * Device Registration Exception
 */
public class DeviceRegistrationException extends DeviceServiceException {
    public DeviceRegistrationException(String message) {
        super(message);
    }

    public DeviceRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}