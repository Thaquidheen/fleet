/**
 * Group Type Enum
 * Represents different types of vehicle groups for fleet organization
 */
public enum GroupType {
    DEPARTMENT("Department", "Grouped by organizational department"),
    REGION("Region", "Grouped by geographical region"),
    TYPE("Type", "Grouped by vehicle type"),
    CUSTOM("Custom", "Custom grouping criteria"),
    LOCATION("Location", "Grouped by physical location");

    private final String displayName;
    private final String description;

    GroupType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean supportsHierarchy() {
        return this == DEPARTMENT || this == REGION || this == LOCATION;
    }

    public boolean isSystemDefined() {
        return this == DEPARTMENT || this == REGION || this == TYPE || this == LOCATION;
    }
}