package com.fleetmanagement.vehicleservice.exception;

public class VehicleAssignmentConflictException extends RuntimeException {
    public VehicleAssignmentConflictException(String message) {
        super(message);
    }
}