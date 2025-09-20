package com.fleetmanagement.bridgeservice.model.events;

import com.fleetmanagement.bridgeservice.model.domain.DeviceHealth;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class DeviceHeartbeatEvent {

    private String eventId;
    private String eventType;
    private Instant timestamp;

    // Device information
    private UUID deviceId;
    private Long traccarDeviceId;
    private UUID companyId;
    private String deviceName;

    // Health data
    private DeviceHealth deviceHealth;

    // Event metadata
    private String source;
    private String version;

    public static DeviceHeartbeatEvent from(DeviceHealth deviceHealth) {
        return DeviceHeartbeatEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("device.heartbeat")
                .timestamp(Instant.now())
                .deviceId(deviceHealth.getDeviceId())
                .traccarDeviceId(deviceHealth.getTraccarDeviceId())
                .companyId(deviceHealth.getCompanyId())
                .deviceHealth(deviceHealth)
                .source("bridge-service")
                .version("1.0")
                .build();
    }
}