package com.fleetmanagement.deviceservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fleetmanagement.deviceservice.domain.enums.ConnectionStatus;
import com.fleetmanagement.deviceservice.domain.enums.DeviceStatus;
import com.fleetmanagement.deviceservice.domain.valueobjects.DeviceConfiguration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Mobile Device Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MobileDeviceResponse {

    private UUID id;
    private String deviceId;
    private String deviceName;
    private DeviceStatus status;
    private ConnectionStatus connectionStatus;

    private UUID companyId;
    private UUID driverId;
    private String driverName;

    private String phoneNumber;
    private String appVersion;
    private String operatingSystem;
    private Boolean trackingEnabled;

    private DeviceConfiguration configuration;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastCommunication;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    // Tracking information
    private String currentShiftId;
    private Boolean backgroundTracking;
    private Integer updateInterval;

    // Health information
    private Integer batteryLevel;
    private Integer signalStrength;
    private Double gpsAccuracy;
}