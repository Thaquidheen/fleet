package com.fleetmanagement.vehicleservice.domain.enums;

/**
 * Assignment Status Enum
 * Represents the current status of a vehicle assignment
 */
public enum AssignmentStatus {
    ACTIVE("Active", "Assignment is currently active"),
    PENDING("Pending", "Assignment is scheduled for future"),
    COMPLETED("Completed", "Assignment has been completed"),
    CANCELLED("Cancelled", "Assignment was cancelled"),
    EXPIRED("Expired", "Assignment has expired"),
    SUSPENDED("Suspended", "Assignment is temporarily suspended");

    private final String displayName;
    private final String description;

    AssignmentStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean canBeModified() {
        return this == ACTIVE || this == PENDING || this == SUSPENDED;
    }

    public boolean isTerminated() {
        return this == COMPLETED || this == CANCELLED || this == EXPIRED;
    }
}