package com.fleetmanagement.deviceservice.domain.enums;

/**
 * Command Status Enum
 */
public enum CommandStatus {
    PENDING("Command queued for execution"),
    SENT("Command sent to device"),
    ACKNOWLEDGED("Command acknowledged by device"),
    EXECUTING("Command is being executed"),
    EXECUTED("Command successfully executed"),
    FAILED("Command execution failed"),
    TIMEOUT("Command timed out"),
    CANCELLED("Command was cancelled"),
    RETRY("Command queued for retry");

    private final String description;

    CommandStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isTerminal() {
        return this == EXECUTED || this == FAILED || this == TIMEOUT || this == CANCELLED;
    }

    public boolean isSuccessful() {
        return this == EXECUTED;
    }

    public boolean canRetry() {
        return this == FAILED || this == TIMEOUT;
    }
}