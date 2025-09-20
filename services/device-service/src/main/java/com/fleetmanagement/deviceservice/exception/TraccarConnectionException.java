package com.fleetmanagement.deviceservice.exception;



public class TraccarConnectionException extends TraccarIntegrationException {
    public TraccarConnectionException(String message) {
        super(message);
    }

    public TraccarConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}