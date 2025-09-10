package com.fleetmanagement.vehicleservice.domain.enums;

/**
 * Vehicle Category Enum
 * Represents the operational category of vehicles
 */
public enum VehicleCategory {
    DELIVERY("Delivery", "Vehicles used for delivery services"),
    PASSENGER("Passenger", "Vehicles for passenger transportation"),
    HEAVY_EQUIPMENT("Heavy Equipment", "Heavy machinery and equipment"),
    CONSTRUCTION("Construction", "Construction site vehicles"),
    EMERGENCY("Emergency", "Emergency response vehicles"),
    COMMERCIAL("Commercial", "General commercial use vehicles"),
    PERSONAL("Personal", "Personal use vehicles");

    private final String displayName;
    private final String description;

    VehicleCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCommercialUse() {
        return this == DELIVERY || this == COMMERCIAL || this == CONSTRUCTION;
    }

    public boolean requiresSpecialInsurance() {
        return this == HEAVY_EQUIPMENT || this == CONSTRUCTION || this == EMERGENCY;
    }
}