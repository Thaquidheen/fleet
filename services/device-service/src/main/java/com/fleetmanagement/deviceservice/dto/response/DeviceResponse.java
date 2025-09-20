
package com.fleetmanagement.deviceservice.dto;

import com.fleetmanagement.deviceservice.domain.enums.*;
import com.fleetmanagement.deviceservice.domain.valueobjects.DeviceConfiguration;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Device Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceResponse {

    private UUID id;
    private String deviceId;
    private String deviceName;
    private DeviceType deviceType;
    private DeviceBrand deviceBrand;
    private DeviceStatus status;
    private ConnectionStatus connectionStatus;

    private UUID companyId;
    private Long traccarId;
    private ProtocolType protocolType;

    private DeviceConfiguration configuration;

    private String serialNumber;
    private String firmwareVersion;
    private String hardwareVersion;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastCommunication;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime activatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private String createdBy;

    // Assignment information
    private VehicleAssignmentInfo currentVehicleAssignment;
    private UserAssignmentInfo currentUserAssignment;

    // Sensor information
    private List<SensorInfo> activeSensors;
    private Double monthlySensorCost;

    // Health information
    private HealthInfo latestHealth;

    // Statistics
    private DeviceStatistics statistics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleAssignmentInfo {
        private UUID vehicleId;
        private String vehicleName;
        private String installationLocation;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime assignedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserAssignmentInfo {
        private UUID userId;
        private String userName;
        private Boolean trackingEnabled;
        private String currentShiftId;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime assignedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SensorInfo {
        private UUID sensorId;
        private SensorType sensorType;
        private String sensorName;
        private Boolean isActive;
        private Double monthlyPrice;
        private String lastReadingValue;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime lastReadingAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HealthInfo {
        private HealthLevel healthLevel;
        private Integer healthScore;
        private Integer batteryLevel;
        private Integer signalStrength;
        private Double gpsAccuracy;
        private Integer communicationFailures;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime recordedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceStatistics {
        private Long totalCommands;
        private Long successfulCommands;
        private Long failedCommands;
        private Double uptime;
        private Long totalDataPoints;
    }
}
