
package com.fleetmanagement.vehicleservice.exception;

import java.util.List;

/**
 * Vehicle Already Exists Exception
 */
public class VehicleAlreadyExistsException extends VehicleServiceException {
    public VehicleAlreadyExistsException(String message) {
        super(message);
    }
}