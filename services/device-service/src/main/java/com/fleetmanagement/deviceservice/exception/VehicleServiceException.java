
package com.fleetmanagement.deviceservice.exception;

/**
 * Vehicle Service Exception
 */
public class VehicleServiceException extends ExternalServiceException {
    public VehicleServiceException(String message) {
        super(message);
    }

    public VehicleServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}