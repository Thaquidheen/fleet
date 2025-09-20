package com.fleetmanagement.deviceservice.exception;


/**
 * Command Timeout Exception
 */
public class CommandTimeoutException extends CommandException {
    public CommandTimeoutException(String message) {
        super(message);
    }
}