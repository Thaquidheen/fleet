/**
 * Assignment Status Enum
 * Represents the status of vehicle-driver assignments
 */
public enum AssignmentStatus {
    ASSIGNED("Assigned", "Vehicle is currently assigned to driver"),
    UNASSIGNED("Unassigned", "Vehicle is not assigned to any driver"),
    TEMPORARY("Temporary", "Temporary assignment"),
    EXPIRED("Expired", "Assignment has expired");

    private final String displayName;
    private final String description;

    AssignmentStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return this == ASSIGNED || this == TEMPORARY;
    }

    public boolean isAvailableForNewAssignment() {
        return this == UNASSIGNED || this == EXPIRED;
    }
}