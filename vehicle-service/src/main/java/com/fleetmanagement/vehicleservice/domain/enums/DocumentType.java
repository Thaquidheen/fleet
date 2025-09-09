/**
 * Document Type Enum
 * Represents different types of vehicle documents
 */
public enum DocumentType {
    REGISTRATION("Registration", "Vehicle registration documents"),
    INSURANCE("Insurance", "Insurance policy documents"),
    INSPECTION("Inspection", "Vehicle inspection certificates"),
    LICENSE("License", "Special licenses and permits"),
    OTHER("Other", "Other vehicle-related documents");

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
        return this == REGISTRATION || this == INSURANCE || this == INSPECTION || this == LICENSE;
    }

    public boolean isMandatory() {
        return this == REGISTRATION || this == INSURANCE;
    }
}