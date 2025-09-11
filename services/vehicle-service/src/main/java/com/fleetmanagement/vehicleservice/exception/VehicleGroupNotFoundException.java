package com.fleetmanagement.vehicleservice.exception;

public class VehicleGroupNotFoundException extends RuntimeException {
    public VehicleGroupNotFoundException(String message) {
        super(message);
    }
}