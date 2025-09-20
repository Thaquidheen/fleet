

package com.fleetmanagement.deviceservice.domain.enums;

public enum DeviceBrand {
    TELTONIKA("Teltonika", "TCP", true, "Most popular GPS trackers"),
    QUECLINK("Queclink", "TCP/HTTP", true, "Professional GPS tracking solutions"),
    CONCOX("Concox", "TCP", true, "Cost-effective tracking devices"),
    MEITRACK("Meitrack", "TCP/UDP/HTTP", true, "Multi-protocol tracking devices"),
    CALAMP("CalAmp", "LM_DIRECT", false, "Enterprise fleet management"),
    GEOTAB("Geotab", "GO_DEVICE", false, "Comprehensive fleet solutions"),
    GENERIC("Generic", "HTTP", true, "Generic protocol support"),
    MOBILE("Mobile", "HTTP/WebSocket", true, "Mobile phone tracking");

    private final String displayName;
    private final String supportedProtocols;
    private final boolean isImplemented;
    private final String description;

    DeviceBrand(String displayName, String supportedProtocols, boolean isImplemented, String description) {
        this.displayName = displayName;
        this.supportedProtocols = supportedProtocols;
        this.isImplemented = isImplemented;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getSupportedProtocols() { return supportedProtocols; }
    public boolean isImplemented() { return isImplemented; }
    public String getDescription() { return description; }
}