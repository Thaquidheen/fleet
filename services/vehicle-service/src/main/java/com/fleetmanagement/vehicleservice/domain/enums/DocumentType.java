package com.fleetmanagement.vehicleservice.domain.enums;

/**
 * Document Type Enum
 * Represents different types of vehicle documents
 */
public enum DocumentType {
    REGISTRATION("Registration", "Vehicle registration documents"),
    INSURANCE("Insurance", "Insurance policy documents"),
    INSPECTION("Inspection", "Vehicle inspection certificates"),
    MAINTENANCE("Maintenance", "Maintenance records and certificates"),
    WARRANTY("Warranty", "Warranty information and documents"),
    PURCHASE("Purchase", "Purchase agreements and invoices"),
    LEASE("Lease", "Lease agreements and contracts"),
    OTHER("Other", "Other miscellaneous documents");

    private final String displayName;
    private final String description;

    DocumentType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean hasExpiryDate() {
        return this == REGISTRATION || this == INSURANCE || this == INSPECTION || this == WARRANTY;
    }

    public boolean isRequired() {
        return this == REGISTRATION || this == INSURANCE;
    }
}