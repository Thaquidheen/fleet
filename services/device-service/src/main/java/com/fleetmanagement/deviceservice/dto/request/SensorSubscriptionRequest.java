
package com.fleetmanagement.deviceservice.dto.request;


import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;


import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SensorSubscriptionRequest {

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotNull(message = "Company ID is required")
    private UUID companyId;

    @NotNull(message = "Sensor type is required")
    private SensorType sensorType;

    private String sensorName;
    private String sensorIdentifier;

    @DecimalMin(value = "0.0", message = "Monthly price must be positive")
    private Double monthlyPrice;

    private String configuration;
    private String alertThresholds;
    private String calibrationData;

    @Builder.Default
    private Boolean autoRenewal = true;

    @Builder.Default
    private Integer billingCycleDays = 30;

    private String notes;
}
