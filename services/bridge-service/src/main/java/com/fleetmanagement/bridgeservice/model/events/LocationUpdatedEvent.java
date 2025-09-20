package com.fleetmanagement.bridgeservice.model.events;

import com.fleetmanagement.bridgeservice.model.domain.LocationData;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class LocationUpdatedEvent {

    private String eventId;
    private String eventType;
    private Instant timestamp;

    // Device information
    private UUID deviceId;
    private Long traccarDeviceId;
    private UUID companyId;
    private String deviceName;

    // Location data
    private LocationData locationData;

    // Event metadata
    private String source;
    private String version;

    public static LocationUpdatedEvent from(LocationData locationData) {
        return LocationUpdatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("device.location.updated")
                .timestamp(Instant.now())
                .deviceId(locationData.getDeviceId())
                .traccarDeviceId(locationData.getTraccarDeviceId())
                .companyId(locationData.getCompanyId())
                .locationData(locationData)
                .source("bridge-service")
                .version("1.0")
                .build();
    }
}