
package com.fleetmanagement.deviceservice.domain.enums;


public enum ConnectionStatus {
    CONNECTED("Device is connected and communicating"),
    DISCONNECTED("Device is not connected"),
    RECONNECTING("Device is attempting to reconnect"),
    IDLE("Device is connected but idle"),
    ERROR("Device connection has errors");

    private final String description;

    ConnectionStatus(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}
