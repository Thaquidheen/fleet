// services/device-service/src/main/java/com/fleetmanagement/deviceservice/domain/enums/DeviceType.java
package com.fleetmanagement.deviceservice.domain.enums;

public enum DeviceType {
    GPS_TRACKER("GPS Tracker", "Basic location tracking"),
    OBD_TRACKER("OBD Tracker", "OBD-II port connected tracker"),
    ASSET_TRACKER("Asset Tracker", "Non-vehicle asset tracking"),
    MOBILE_PHONE("Mobile Phone", "Smartphone as tracking device"),
    DASH_CAM("Dash Camera", "Video recording with GPS"),
    FUEL_SENSOR("Fuel Sensor", "Fuel level monitoring"),
    TEMPERATURE_SENSOR("Temperature Sensor", "Temperature monitoring"),
    CARGO_SENSOR("Cargo Sensor", "Cargo monitoring and security");

    private final String displayName;
    private final String description;

    DeviceType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}