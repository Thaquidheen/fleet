/**
 * Priority Level Enum
 * Represents priority levels for maintenance and other operations
 */
public enum PriorityLevel {
    CRITICAL(1, "Critical", "Immediate attention required"),
    HIGH(2, "High", "High priority, schedule soon"),
    NORMAL(3, "Normal", "Normal priority"),
    LOW(4, "Low", "Low priority, can be deferred");

    private final int level;
    private final String displayName;
    private final String description;

    PriorityLevel(int level, String displayName, String description) {
        this.level = level;
        this.displayName = displayName;
        this.description = description;
    }

    public int getLevel() {
        return level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCritical() {
        return this == CRITICAL;
    }

    public boolean isHighPriority() {
        return this == CRITICAL || this == HIGH;
    }

    public static PriorityLevel fromLevel(int level) {
        for (PriorityLevel priority : values()) {
            if (priority.level == level) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Invalid priority level: " + level);
    }
}