
package com.fleetmanagement.deviceservice.exception;

/**
 * Sensor Subscription Exception
 */
public class SensorSubscriptionException extends DeviceServiceException {
    public SensorSubscriptionException(String message) {
        super(message);
    }

    public SensorSubscriptionException(String message, Throwable cause) {
        super(message, cause);
    }
}