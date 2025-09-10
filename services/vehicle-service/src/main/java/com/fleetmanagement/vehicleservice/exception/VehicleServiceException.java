package com.fleetmanagement.vehicleservice.exception;

/**
 * Base exception class for Vehicle Service
 * All custom exceptions in the vehicle service should extend this class
 */
public class VehicleServiceException extends RuntimeException {

    public VehicleServiceException(String message) {
        super(message);
    }

    public VehicleServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public VehicleServiceException(Throwable cause) {
        super(cause);
    }
}