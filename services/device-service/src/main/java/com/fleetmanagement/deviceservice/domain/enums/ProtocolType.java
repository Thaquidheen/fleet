
package com.fleetmanagement.deviceservice.domain.enums;

public enum ProtocolType {
    TCP("TCP", "TCP socket communication"),
    UDP("UDP", "UDP datagram communication"),
    HTTP("HTTP", "HTTP REST communication"),
    HTTPS("HTTPS", "HTTPS secure communication"),
    MQTT("MQTT", "MQTT publish-subscribe"),
    WEBSOCKET("WebSocket", "WebSocket bidirectional communication"),
    SMS("SMS", "SMS text communication");

    private final String name;
    private final String description;

    ProtocolType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
}
