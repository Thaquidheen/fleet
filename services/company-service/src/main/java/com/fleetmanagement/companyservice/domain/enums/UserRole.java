package com.fleetmanagement.companyservice.domain.enums;

public enum UserRole {
    SUPER_ADMIN("Super Administrator"),
    COMPANY_ADMIN("Company Administrator"),
    MANAGER("Manager"),
    DRIVER("Driver"),
    VIEWER("Viewer");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}