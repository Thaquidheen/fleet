



// services/device-service/src/main/java/com/fleetmanagement/deviceservice/domain/enums/DeviceStatus.java
package com.fleetmanagement.deviceservice.domain.enums;

public enum DeviceStatus {
    ACTIVE("Device is active and operational"),
    INACTIVE("Device is inactive or powered off"),
    MAINTENANCE("Device is under maintenance"),
    FAULTY("Device has technical issues"),
    DECOMMISSIONED("Device is permanently removed from service");

    private final String description;

    DeviceStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}


