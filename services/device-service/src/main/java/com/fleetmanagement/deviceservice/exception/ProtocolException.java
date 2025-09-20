
package com.fleetmanagement.deviceservice.exception;

/**
 * Protocol Exception
 */
public class ProtocolException extends DeviceServiceException {
    public ProtocolException(String message) {
        super(message);
    }

    public ProtocolException(String message, Throwable cause) {
        super(message, cause);
    }
}