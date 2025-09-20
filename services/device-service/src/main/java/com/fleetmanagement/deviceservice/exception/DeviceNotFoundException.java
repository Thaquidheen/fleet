package com.fleetmanagement.deviceservice.exception;

/**
 * Exception thrown when a device is not found
 * 
 * This exception is thrown when attempting to access a device
 * that does not exist in the system.
 */
/**
 * Device Not Found Exception
 */
public class DeviceNotFoundException extends DeviceServiceException {
    public DeviceNotFoundException(String message) {
        super(message);
    }
}

