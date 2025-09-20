package com.fleetmanagement.bridgeservice.exception;

public class TraccarApiException extends RuntimeException {
    public TraccarApiException(String message) {
        super(message);
    }

    public TraccarApiException(String message, Throwable cause) {
        super(message, cause);
    }
}