// SensorReadingEvent.java
package com.fleetmanagement.bridgeservice.model.events;

import com.fleetmanagement.bridgeservice.model.domain.SensorData;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class SensorReadingEvent {

    private String eventId;
    private String eventType;
    private Instant timestamp;

    // Device information
    private UUID deviceId;
    private Long traccarDeviceId;
    private UUID companyId;
    private String deviceName;

    // Sensor data
    private SensorData sensorData;

    // Event metadata
    private String source;
    private String version;

    public static SensorReadingEvent from(SensorData sensorData) {
        return SensorReadingEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("device.sensor.reading")
                .timestamp(Instant.now())
                .deviceId(sensorData.getDeviceId())
                .traccarDeviceId(sensorData.getTraccarDeviceId())
                .companyId(sensorData.getCompanyId())
                .sensorData(sensorData)
                .source("bridge-service")
                .version("1.0")
                .build();
    }
}