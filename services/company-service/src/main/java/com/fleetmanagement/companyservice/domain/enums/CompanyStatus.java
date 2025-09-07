package com.fleetmanagement.companyservice.domain.enums;

/**
 * Represents the various statuses a company can have within the fleet management system.
 * Each status defines the company's level of access and billing state.
 */
public enum CompanyStatus {
    TRIAL("Trial", "Company is in a free trial period and has full access"),
    ACTIVE("Active", "Company has an active subscription and full access"),
    SUSPENDED("Suspended", "Company account is temporarily suspended, access is blocked"),
    CANCELLED("Cancelled", "Company has cancelled their subscription, access will be revoked at the end of the billing period"),
    EXPIRED("Expired", "Company trial or subscription has expired, access is blocked");

    private final String displayName;
    private final String description;

    /**
     * Constructor for the enum constant.
     * @param displayName The user-friendly name of the status.
     * @param description A brief explanation of what the status means.
     */
    CompanyStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Gets the user-friendly display name for the status.
     * @return A string representing the display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the detailed description of the status.
     * @return A string containing the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks if the company's subscription is currently active.
     * @return true if the status is ACTIVE, false otherwise.
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * Checks if the company should have access to the system's features.
     * Access is granted during TRIAL and ACTIVE periods.
     * @return true if the company can access services, false otherwise.
     */
    public boolean canAccess() {
        return this == ACTIVE || this == TRIAL;
    }
}
