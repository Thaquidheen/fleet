
package com.fleetmanagement.deviceservice.exception;


/**
 * Device Assignment Exception
 */
public class DeviceAssignmentException extends DeviceServiceException {
    public DeviceAssignmentException(String message) {
        super(message);
    }

    public DeviceAssignmentException(String message, Throwable cause) {
        super(message, cause);
    }
}