package com.fleetmanagement.userservice.domain.enums;

public enum SessionStatus {
    ACTIVE("Active", "Session is currently active"),
    EXPIRED("Expired", "Session has expired"),
    REVOKED("Revoked", "Session has been manually revoked");


    private final String displayName;
    private final String description;

    SessionStatus(String displayName, String description) {
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

    public boolean isInactive() {
        return this != ACTIVE;
    }
}