package com.fleetmanagement.deviceservice.domain.enums;


public enum HealthLevel {
    EXCELLENT("Device operating at optimal levels"),
    GOOD("Device operating normally"),
    FAIR("Device showing minor issues"),
    POOR("Device has significant issues"),
    CRITICAL("Device requires immediate attention");

    private final String description;

    HealthLevel(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}