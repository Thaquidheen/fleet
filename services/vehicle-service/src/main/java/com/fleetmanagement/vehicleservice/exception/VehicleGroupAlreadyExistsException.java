package com.fleetmanagement.vehicleservice.exception;

public class VehicleGroupAlreadyExistsException extends RuntimeException {
    public VehicleGroupAlreadyExistsException(String message) {
        super(message);
    }
}