

package com.fleetmanagement.deviceservice.domain.enums;



public enum SensorType {
    TEMPERATURE("Temperature", 8.00, 15.00, "celsius", "Cold chain monitoring"),
    FUEL("Fuel Level", 8.00, 18.00, "liters", "Fuel monitoring and theft detection"),
    WEIGHT("Weight/Load", 10.00, 20.00, "kg", "Cargo weight monitoring"),
    HUMIDITY("Humidity", 5.00, 12.00, "percentage", "Environmental monitoring"),
    PRESSURE("Pressure", 5.00, 12.00, "psi", "Tire or hydraulic pressure"),
    DOOR("Door Sensor", 3.00, 8.00, "boolean", "Door open/close monitoring"),
    ENGINE_HOURS("Engine Hours", 6.00, 12.00, "hours", "Engine usage tracking"),
    RPM("Engine RPM", 4.00, 10.00, "rpm", "Engine performance monitoring"),
    SPEED("Speed Monitor", 3.00, 8.00, "km/h", "Speed monitoring and alerts"),
    ENVIRONMENTAL("Environmental", 5.00, 12.00, "mixed", "Air quality and environmental monitoring");

    private final String displayName;
    private final double baseMonthlyPrice;
    private final double maxMonthlyPrice;
    private final String unit;
    private final String description;

    SensorType(String displayName, double baseMonthlyPrice, double maxMonthlyPrice, String unit, String description) {
        this.displayName = displayName;
        this.baseMonthlyPrice = baseMonthlyPrice;
        this.maxMonthlyPrice = maxMonthlyPrice;
        this.unit = unit;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public double getBaseMonthlyPrice() { return baseMonthlyPrice; }
    public double getMaxMonthlyPrice() { return maxMonthlyPrice; }
    public String getUnit() { return unit; }
    public String getDescription() { return description; }
}