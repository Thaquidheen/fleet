
package com.fleetmanagement.deviceservice.exception;


/**
 * Tracking Exception
 */
public class TrackingException extends DeviceServiceException {
    public TrackingException(String message) {
        super(message);
    }

    public TrackingException(String message, Throwable cause) {
        super(message, cause);
    }
}