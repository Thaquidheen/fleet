package com.fleetmanagement.vehicleservice.exception;

public class DriverNotAvailableException extends RuntimeException {

    public DriverNotAvailableException(String message) {
        super(message);
    }

    public DriverNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}