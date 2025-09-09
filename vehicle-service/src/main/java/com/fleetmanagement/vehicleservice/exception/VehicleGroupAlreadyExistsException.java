
package com.fleetmanagement.vehicleservice.exception;

import java.util.List;

/**
 * Vehicle Group Already Exists Exception
 */
public class VehicleGroupAlreadyExistsException extends VehicleServiceException {
    public VehicleGroupAlreadyExistsException(String message) {
        super(message);
    }
}