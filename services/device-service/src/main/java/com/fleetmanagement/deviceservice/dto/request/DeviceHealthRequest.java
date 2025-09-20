
package com.fleetmanagement.deviceservice.dto.request;


import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;


import java.util.Map;


/**
 * Device Health Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceHealthRequest {

    @Min(value = 0, message = "Health score must be between 0 and 100")
    @Max(value = 100, message = "Health score must be between 0 and 100")
    private Integer healthScore;

    @Min(value = 0, message = "Battery level must be between 0 and 100")
    @Max(value = 100, message = "Battery level must be between 0 and 100")
    private Integer batteryLevel;

    @Min(value = 0, message = "Signal strength must be between 0 and 100")
    @Max(value = 100, message = "Signal strength must be between 0 and 100")
    private Integer signalStrength;

    @DecimalMin(value = "0.0", message = "GPS accuracy must be positive")
    private Double gpsAccuracy;

    @Min(value = 0, message = "Satellite count must be non-negative")
    private Integer satelliteCount;

    private Double deviceTemperature;

    @Min(value = 0, message = "Memory usage must be between 0 and 100")
    @Max(value = 100, message = "Memory usage must be between 0 and 100")
    private Integer memoryUsage;

    @Min(value = 0, message = "CPU usage must be between 0 and 100")
    @Max(value = 100, message = "CPU usage must be between 0 and 100")
    private Integer cpuUsage;

    @Min(value = 0, message = "Network latency must be non-negative")
    private Long networkLatency;

    @Min(value = 0, message = "Communication failures must be non-negative")
    private Integer communicationFailures;

    @Min(value = 0, message = "Uptime must be non-negative")
    private Long uptimeSeconds;

    private Map<String, Object> additionalMetrics;
    private String notes;
}
