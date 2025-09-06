// UserStatus.java
package com.fleetmanagement.userservice.domain.enums;

public enum UserStatus {
    ACTIVE("Active", "User account is active and functional"),
    INACTIVE("Inactive", "User account is temporarily disabled"),
    LOCKED("Locked", "User account is locked due to security reasons"),
    SUSPENDED("Suspended", "User account is suspended by administrator"),
    PENDING_VERIFICATION("Pending Verification", "User account is pending email verification"),
    EXPIRED("Expired", "User account has expired");

    private final String displayName;
    private final String description;

    UserStatus(String displayName, String description) {
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

    public boolean canLogin() {
        return this == ACTIVE || this == PENDING_VERIFICATION;
    }

    public boolean requiresVerification() {
        return this == PENDING_VERIFICATION;
    }

    public boolean isBlocked() {
        return this == LOCKED || this == SUSPENDED || this == EXPIRED;
    }
}

