package com.fleetmanagement.vehicleservice.domain.enums;

/**
 * Vehicle Status Enum
 * Represents the current status of a vehicle in the fleet
 */
public enum VehicleStatus {
    ACTIVE("Active", "Vehicle is operational and available"),
    MAINTENANCE("Maintenance", "Vehicle is undergoing maintenance"),
    RETIRED("Retired", "Vehicle has been retired from service"),
    SOLD("Sold", "Vehicle has been sold"),
    INACTIVE("Inactive", "Vehicle is temporarily inactive");

    private final String displayName;
    private final String description;

    VehicleStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isOperational() {
        return this == ACTIVE;
    }

    public boolean isAvailableForAssignment() {
        return this == ACTIVE;
    }

    public boolean canBeAssignedToDriver() {
        return this == ACTIVE;
    }
}
