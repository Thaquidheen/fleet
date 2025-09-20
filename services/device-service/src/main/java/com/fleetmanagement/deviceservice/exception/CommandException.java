package com.fleetmanagement.deviceservice.exception;


/**
 * Command Exception
 */
public class CommandException extends DeviceServiceException {
    public CommandException(String message) {
        super(message);
    }

    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }
}