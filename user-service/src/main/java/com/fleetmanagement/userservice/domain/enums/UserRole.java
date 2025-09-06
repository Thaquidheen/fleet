// UserRole.java
package com.fleetmanagement.userservice.domain.enums;

public enum UserRole {
    SUPER_ADMIN("Super Administrator", "Full system access across all companies"),
    COMPANY_ADMIN("Company Administrator", "Full access within company scope"),
    FLEET_MANAGER("Fleet Manager", "Manage vehicles and drivers within company"),
    DRIVER("Driver", "Limited access to assigned vehicles and trips"),
    VIEWER("Viewer", "Read-only access to company data");

    private final String displayName;
    private final String description;

    UserRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean canManageUsers() {
        return this == SUPER_ADMIN || this == COMPANY_ADMIN;
    }

    public boolean canManageFleet() {
        return this == SUPER_ADMIN || this == COMPANY_ADMIN || this == FLEET_MANAGER;
    }

    public boolean canViewReports() {
        return this != DRIVER;
    }

    public boolean hasAdminPrivileges() {
        return this == SUPER_ADMIN || this == COMPANY_ADMIN;
    }

    public boolean isSuperAdmin() {
        return this == SUPER_ADMIN;
    }

    public boolean isDriver() {
        return this == DRIVER;
    }
}



// SessionStatus.java
