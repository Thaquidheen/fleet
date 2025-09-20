// ===== DeviceHealth.java =====
package com.fleetmanagement.bridgeservice.model.domain;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class DeviceHealth {

    private UUID deviceId;
    private Long traccarDeviceId;
    private UUID companyId;

    // Connection status
    private String status;
    private Instant lastCommunication;
    private Instant lastPosition;

    // Signal quality
    private Integer signalStrength;
    private String networkType;
    private Integer satelliteCount;

    // Device status
    private Double batteryLevel;
    private Boolean powerStatus;
    private String firmwareVersion;

    // Performance metrics
    private Long messageCount;
    private Double dataUsage;
    private Integer errorCount;

    // Health score
    private Integer healthScore;
    private String healthStatus;

    // Timestamps
    private Instant checkedTime;
    private Instant reportedTime;
}