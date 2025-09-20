package com.fleetmanagement.deviceservice.exception;


/**
 * External Service Exception
 */
public class ExternalServiceException extends DeviceServiceException {
    public ExternalServiceException(String message) {
        super(message);
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}