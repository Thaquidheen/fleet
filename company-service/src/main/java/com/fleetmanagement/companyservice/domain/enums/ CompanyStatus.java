package com.fleetmanagement.companyservice.domain.enums;

public enum CompanyStatus {
    TRIAL("Trial", "Company is in trial period"),
    ACTIVE("Active", "Company subscription is active"),
    SUSPENDED("Suspended", "Company account is suspended"),
    CANCELLED("Cancelled", "Company subscription cancelled"),
    EXPIRED("Expired", "Company trial/subscription expired");

    private final String displayName;
    private final String description;

    CompanyStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    public boolean isActive() { return this == ACTIVE; }
    public boolean canAccess() { return this == ACTIVE || this == TRIAL; }
}