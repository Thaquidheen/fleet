/**
 * Vehicle Type Enum
 * Represents different types of vehicles in the fleet
 */
public enum VehicleType {
    CAR("Car", "Standard passenger cars"),
    TRUCK("Truck", "Commercial trucks and pickups"),
    MOTORCYCLE("Motorcycle", "Motorcycles and scooters"),
    BUS("Bus", "Buses and coaches"),
    VAN("Van", "Vans and minivans"),
    CONSTRUCTION("Construction", "Construction and heavy equipment"),
    MARINE("Marine", "Boats and marine vehicles"),
    AGRICULTURAL("Agricultural", "Farm and agricultural equipment");

    private final String displayName;
    private final String description;

    VehicleType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCommercialVehicle() {
        return this == TRUCK || this == BUS || this == VAN || this == CONSTRUCTION;
    }

    public boolean requiresSpecialLicense() {
        return this == TRUCK || this == BUS || this == CONSTRUCTION || this == MARINE;
    }
}