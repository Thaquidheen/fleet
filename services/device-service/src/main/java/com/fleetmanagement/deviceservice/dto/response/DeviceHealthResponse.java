
package com.fleetmanagement.deviceservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


/**
 * Device Health Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceHealthResponse {

    private UUID id;
    private String deviceId;
    private String deviceName;
    private UUID companyId;
    private HealthLevel healthLevel;
    private Integer healthScore;

    private Integer batteryLevel;
    private Integer signalStrength;
    private Double gpsAccuracy;
    private Integer satelliteCount;
    private Double deviceTemperature;
    private Integer memoryUsage;
    private Integer cpuUsage;
    private Long networkLatency;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastCommunication;

    private Integer communicationFailures;
    private Long uptimeSeconds;
    private Map<String, Object> additionalMetrics;
    private String notes;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime recordedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    // Health indicators
    private Boolean isCritical;
    private Boolean needsAttention;
    private List<String> healthIssues;
    private List<String> recommendations;
}
