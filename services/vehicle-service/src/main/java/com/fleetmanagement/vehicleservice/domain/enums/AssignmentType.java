/**
 * Assignment Type Enum
 * Represents different types of driver assignments
 */
public enum AssignmentType {
    PERMANENT("Permanent", "Long-term permanent assignment"),
    TEMPORARY("Temporary", "Short-term temporary assignment"),
    SHIFT("Shift", "Shift-based assignment with specific hours");

    private final String displayName;
    private final String description;

    AssignmentType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean requiresEndDate() {
        return this == TEMPORARY;
    }

    public boolean requiresShiftTimes() {
        return this == SHIFT;
    }
}
