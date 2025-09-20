
package com.fleetmanagement.deviceservice.exception;

/**
 * Billing Exception
 */
public class BillingException extends DeviceServiceException {
    public BillingException(String message) {
        super(message);
    }

    public BillingException(String message, Throwable cause) {
        super(message, cause);
    }
}