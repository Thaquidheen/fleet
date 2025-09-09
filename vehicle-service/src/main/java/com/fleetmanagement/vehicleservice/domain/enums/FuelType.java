/**
 * Fuel Type Enum
 * Represents different fuel types for vehicles
 */
public enum FuelType {
    GASOLINE("Gasoline", "Regular gasoline fuel", false),
    DIESEL("Diesel", "Diesel fuel", false),
    ELECTRIC("Electric", "Electric battery powered", true),
    HYBRID("Hybrid", "Hybrid gasoline-electric", true),
    CNG("CNG", "Compressed Natural Gas", true),
    LPG("LPG", "Liquefied Petroleum Gas", true);

    private final String displayName;
    private final String description;
    private final boolean isEcoFriendly;

    FuelType(String displayName, String description, boolean isEcoFriendly) {
        this.displayName = displayName;
        this.description = description;
        this.isEcoFriendly = isEcoFriendly;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEcoFriendly() {
        return isEcoFriendly;
    }

    public boolean requiresSpecialInfrastructure() {
        return this == ELECTRIC || this == CNG || this == LPG;
    }

    public boolean isTraditionalFuel() {
        return this == GASOLINE || this == DIESEL;
    }
}