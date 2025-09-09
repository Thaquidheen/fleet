// BulkOperationType.java (Enum)
package com.fleetmanagement.companyservice.domain.enums;

public enum BulkOperationType {
    CREATE("Create multiple users"),
    UPDATE("Update multiple users"),
    DELETE("Delete multiple users"),
    ACTIVATE("Activate multiple users"),
    DEACTIVATE("Deactivate multiple users"),
    CHANGE_ROLE("Change role for multiple users");

    private final String description;

    BulkOperationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}