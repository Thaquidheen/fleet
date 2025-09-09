/**
 * Maintenance Type Enum
 * Represents different types of vehicle maintenance
 */
public enum MaintenanceType {
    OIL_CHANGE("Oil Change", "Engine oil and filter change"),
    TIRE_ROTATION("Tire Rotation", "Tire rotation and alignment"),
    BRAKE_SERVICE("Brake Service", "Brake inspection and service"),
    ENGINE_SERVICE("Engine Service", "Engine maintenance and tuning"),
    TRANSMISSION_SERVICE("Transmission Service", "Transmission fluid and service"),
    INSPECTION("Inspection", "Regular vehicle inspection"),
    REPAIR("Repair", "General repairs and fixes"),
    PREVENTIVE("Preventive", "Preventive maintenance"),
    EMERGENCY("Emergency", "Emergency repairs"),
    RECALL("Recall", "Manufacturer recall service");

    private final String displayName;
    private final String description;

    MaintenanceType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isScheduled() {
        return this == OIL_CHANGE || this == TIRE_ROTATION || this == INSPECTION || this == PREVENTIVE;
    }

    public boolean isUrgent() {
        return this == EMERGENCY || this == RECALL;
    }
}
