
package com.fleetmanagement.deviceservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fleetmanagement.deviceservice.domain.enums.ConnectionStatus;
import com.fleetmanagement.deviceservice.domain.enums.DeviceStatus;
import com.fleetmanagement.deviceservice.domain.enums.HealthLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Device Status Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceStatusResponse {

    private String deviceId;
    private String deviceName;
    private DeviceStatus status;
    private ConnectionStatus connectionStatus;
    private HealthLevel healthLevel;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastCommunication;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime statusChangedAt;

    // Real-time information
    private Double latitude;
    private Double longitude;
    private Double speed;
    private Double heading;
    private String address;

    // Device metrics
    private Integer batteryLevel;
    private Integer signalStrength;
    private Double gpsAccuracy;
    private Integer satelliteCount;

    // Assignment information
    private UUID assignedVehicleId;
    private String assignedVehicleName;
    private UUID assignedUserId;
    private String assignedUserName;

    // Active sensors
    private List<String> activeSensorTypes;
    private Integer totalSensors;
    private Integer activeSensors;

    // Alerts and issues
    private List<String> activeAlerts;
    private List<String> healthIssues;
    private Boolean needsAttention;
}