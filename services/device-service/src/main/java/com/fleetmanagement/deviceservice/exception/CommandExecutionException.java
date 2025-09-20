
package com.fleetmanagement.deviceservice.exception;


/**
 * Command Execution Exception
 */
public class CommandExecutionException extends CommandException {
    public CommandExecutionException(String message) {
        super(message);
    }

    public CommandExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}