// PermissionType.java
package com.fleetmanagement.userservice.domain.enums;

public enum PermissionType {
    READ("Read", "Permission to view/read data"),
    WRITE("Write", "Permission to create/update data"),
    DELETE("Delete", "Permission to delete data"),
    ADMIN("Admin", "Administrative permissions"),
    EXECUTE("Execute", "Permission to execute operations"),
    APPROVE("Approve", "Permission to approve requests"),
    EXPORT("Export", "Permission to export data"),
    IMPORT("Import", "Permission to import data");

    private final String displayName;
    private final String description;

    PermissionType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isReadOperation() {
        return this == READ;
    }

    public boolean isWriteOperation() {
        return this == WRITE || this == DELETE;
    }

    public boolean isAdminOperation() {
        return this == ADMIN;
    }
}