package com.fleetmanagement.deviceservice.exception;



/**
 * Device Configuration Exception
 */
public class DeviceConfigurationException extends DeviceServiceException {
    public DeviceConfigurationException(String message) {
        super(message);
    }

    public DeviceConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}