package com.fleetmanagement.deviceservice.dto.request;


import com.fleetmanagement.deviceservice.domain.valueobjects.DeviceConfiguration;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// ===== REQUEST DTOs =====

/**
 * Register Device Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterDeviceRequest {

    @NotNull(message = "Company ID is required")
    private UUID companyId;

    @NotBlank(message = "Device name is required")
    @Size(max = 100, message = "Device name must be less than 100 characters")
    private String deviceName;

    @NotBlank(message = "Device ID (IMEI) is required")
    @Size(max = 50, message = "Device ID must be less than 50 characters")
    private String imei;

    @NotBlank(message = "Device type is required")
    private String deviceType;

    @NotBlank(message = "Device brand is required")
    private String brand;

    private String serialNumber;
    private String firmwareVersion;
    private String hardwareVersion;

    private UUID assignedVehicleId;
    private UUID assignedUserId;

    private DeviceConfiguration configuration;

    private List<String> sensorTypes;

    @Builder.Default
    private Boolean autoActivate = true;

    private String notes;
}

