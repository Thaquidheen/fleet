package com.fleetmanagement.vehicleservice.domain.enums;

/**
 * Fuel Type Enum
 * Represents different fuel types for vehicles
 */
public enum FuelType {
    GASOLINE("Gasoline", "Regular gasoline"),
    DIESEL("Diesel", "Diesel fuel"),
    ELECTRIC("Electric", "Electric battery"),
    HYBRID("Hybrid", "Hybrid gasoline-electric"),
    CNG("CNG", "Compressed Natural Gas"),
    LPG("LPG", "Liquefied Petroleum Gas"),
    HYDROGEN("Hydrogen", "Hydrogen fuel cell");

    private final String displayName;
    private final String description;

    FuelType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEcoFriendly() {
        return this == ELECTRIC || this == HYDROGEN || this == HYBRID;
    }

    public boolean requiresSpecialInfrastructure() {
        return this == ELECTRIC || this == HYDROGEN || this == CNG;
    }
}