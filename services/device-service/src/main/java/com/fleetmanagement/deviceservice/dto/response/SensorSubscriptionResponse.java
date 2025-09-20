package com.fleetmanagement.deviceservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import com.fleetmanagement.deviceservice.domain.enums.SensorType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
// ===== SENSOR SUBSCRIPTION DTOs =====

/**
 * Sensor Subscription Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SensorSubscriptionResponse {

    private UUID id;
    private UUID deviceSensorId;
    private String deviceId;
    private String deviceName;
    private UUID companyId;
    private SensorType sensorType;
    private String sensorName;
    private Double monthlyPrice;
    private Boolean isActive;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime subscriptionStart;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime subscriptionEnd;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime billingCycleStart;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime billingCycleEnd;

    private Boolean autoRenewal;
    private String configuration;
    private String alertThresholds;
    private String lastReadingValue;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastReadingAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private String createdBy;
    private String notes;
}