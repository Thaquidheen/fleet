

package com.fleetmanagement.deviceservice.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.UUID;


/**
 * Register Mobile Device Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterMobileDeviceRequest {

    @NotNull(message = "Driver ID is required")
    private UUID driverId;

    @NotBlank(message = "Phone IMEI is required")
    private String phoneImei;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;

    @NotBlank(message = "App version is required")
    private String appVersion;

    @NotBlank(message = "Operating system is required")
    private String operatingSystem;

    private String pushToken;
    private String driverName;

    @Builder.Default
    private Integer updateInterval = 30;

    @Builder.Default
    private Boolean trackingEnabled = false;

    @Builder.Default
    private Boolean backgroundTracking = true;

    private String notes;
}
