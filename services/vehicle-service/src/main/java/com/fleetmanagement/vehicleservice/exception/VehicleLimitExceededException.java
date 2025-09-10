package com.fleetmanagement.vehicleservice.exception;

public class VehicleLimitExceededException extends RuntimeException {
    public VehicleLimitExceededException(String message) {
        super(message);
    }

    public VehicleLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}