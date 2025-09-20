package com.fleetmanagement.bridgeservice.model.traccar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TraccarPosition {

    private Long id;

    @JsonProperty("deviceId")
    private Long deviceId;

    @JsonProperty("deviceTime")
    private Instant deviceTime;

    @JsonProperty("fixTime")
    private Instant fixTime;

    @JsonProperty("serverTime")
    private Instant serverTime;

    private Boolean valid;

    private Double latitude;

    private Double longitude;

    private Double altitude;

    private Double speed;

    private Double course;

    private String address;

    private Double accuracy;

    private String protocol;

    @JsonProperty("attributes")
    private Map<String, Object> attributes;

    // Helper methods for common attributes
    public Double getBatteryLevel() {
        return getAttributeAsDouble("battery");
    }

    public Double getFuelLevel() {
        return getAttributeAsDouble("fuel");
    }

    public Double getTemperature() {
        return getAttributeAsDouble("temp1");
    }

    public Boolean getIgnition() {
        return getAttributeAsBoolean("ignition");
    }

    public Double getOdometer() {
        return getAttributeAsDouble("odometer");
    }

    private Double getAttributeAsDouble(String key) {
        Object value = attributes != null ? attributes.get(key) : null;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }

    private Boolean getAttributeAsBoolean(String key) {
        Object value = attributes != null ? attributes.get(key) : null;
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return null;
    }
}