package com.fleetmanagement.deviceservice.exception;

/**
 * Traccar Integration Exception
 */
public class TraccarIntegrationException extends DeviceServiceException {
    public TraccarIntegrationException(String message) {
        super(message);
    }

    public TraccarIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}