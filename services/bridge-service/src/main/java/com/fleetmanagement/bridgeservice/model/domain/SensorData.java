package com.fleetmanagement.bridgeservice.model.domain;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class SensorData {

    private UUID deviceId;
    private Long traccarDeviceId;
    private UUID companyId;

    // Sensor readings
    private Double fuelLevel;
    private Double temperature;
    private Double batteryLevel;
    private Double engineHours;
    private Double weight;
    private Double pressure;
    private Double humidity;

    // Timestamps
    private Instant readingTime;
    private Instant processedTime;

    // Metadata
    private String sensorType;
    private String unit;
    private Boolean valid;
    private String source;
}