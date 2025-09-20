
package com.fleetmanagement.deviceservice.domain.enums;

public enum AssignmentStatus {
    ASSIGNED("Device is assigned"),
    UNASSIGNED("Device is not assigned"),
    TEMPORARY("Device is temporarily assigned"),
    PENDING("Assignment is pending approval"),
    CANCELLED("Assignment was cancelled");

    private final String description;

    AssignmentStatus(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}