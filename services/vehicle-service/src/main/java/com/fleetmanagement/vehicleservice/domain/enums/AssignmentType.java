package com.fleetmanagement.vehicleservice.domain.enums;

/**
 * Assignment Type Enum
 * Represents different types of vehicle assignments
 */
public enum AssignmentType {
    PERMANENT("Permanent", "Long-term or permanent assignment"),
    TEMPORARY("Temporary", "Short-term temporary assignment"),
    SHIFT("Shift", "Shift-based assignment"),
    PROJECT("Project", "Project-specific assignment"),
    ROTATION("Rotation", "Part of a rotation schedule"),
    EMERGENCY("Emergency", "Emergency or urgent assignment");

    private final String displayName;
    private final String description;

    AssignmentType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean hasEndDate() {
        return this != PERMANENT;
    }

    public boolean requiresShiftTimes() {
        return this == SHIFT || this == ROTATION;
    }

    public boolean isFlexible() {
        return this == TEMPORARY || this == PROJECT;
    }
}