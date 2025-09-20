package com.fleetmanagement.deviceservice.exception;


/**
 * User Service Exception
 */
public class UserServiceException extends ExternalServiceException {
    public UserServiceException(String message) {
        super(message);
    }

    public UserServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}