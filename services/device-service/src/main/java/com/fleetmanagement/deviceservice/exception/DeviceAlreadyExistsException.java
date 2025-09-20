package com.fleetmanagement.deviceservice.exception;

/**
 * Exception thrown when attempting to create a device that already exists
 * 
 * This exception is thrown when trying to register a device with
 * a serial number or IMEI that is already in use.
 */
/**
 * Device Already Exists Exception
 */
public class DeviceAlreadyExistsException extends DeviceServiceException {
    public DeviceAlreadyExistsException(String message) {
        super(message);
    }
}

