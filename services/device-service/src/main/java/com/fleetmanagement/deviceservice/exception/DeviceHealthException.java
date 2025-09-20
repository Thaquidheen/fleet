package com.fleetmanagement.deviceservice.exception;

/**
 * Device Health Exception
 */
public class DeviceHealthException extends DeviceServiceException {
    public DeviceHealthException(String message) {
        super(message);
    }

    public DeviceHealthException(String message, Throwable cause) {
        super(message, cause);
    }
}