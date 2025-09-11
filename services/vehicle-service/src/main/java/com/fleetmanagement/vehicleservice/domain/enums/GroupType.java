// ===== GroupType.java =====
package com.fleetmanagement.vehicleservice.domain.enums;

/**
 * Group Type Enum
 * Represents different types of vehicle groups for fleet organization
 */
public enum GroupType {
    DEPARTMENT("Department", "Departmental vehicle group"),
    LOCATION("Location", "Location-based vehicle group"),
    FLEET("Fleet", "Fleet-specific vehicle group"),
    PROJECT("Project", "Project-based vehicle group"),
    TEMPORARY("Temporary", "Temporary vehicle group"),
    CUSTOM("Custom", "Custom vehicle group");


    private final String displayName;
    private final String description;

    GroupType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

}
