

package com.fleetmanagement.deviceservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fleetmanagement.deviceservice.domain.enums.DeviceBrand;
import com.fleetmanagement.deviceservice.domain.enums.DeviceType;
import com.fleetmanagement.deviceservice.domain.enums.SensorType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


/**
 * Billing Report Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BillingReportResponse {

    private UUID companyId;
    private String companyName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime reportPeriodStart;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime reportPeriodEnd;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime generatedAt;

    // Summary
    private BillingSummary summary;

    // Device costs
    private List<DeviceBillingItem> deviceBillingItems;

    // Sensor costs
    private List<SensorBillingItem> sensorBillingItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillingSummary {
        private Integer totalDevices;
        private Integer activeDevices;
        private Integer totalSensors;
        private Integer activeSensors;
        private Double totalAmount;
        private Double deviceCosts;
        private Double sensorCosts;
        private String currency;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceBillingItem {
        private String deviceId;
        private String deviceName;
        private DeviceType deviceType;
        private DeviceBrand deviceBrand;
        private Double monthlyCost;
        private Integer activeDays;
        private Double proRatedAmount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SensorBillingItem {
        private String deviceId;
        private String deviceName;
        private SensorType sensorType;
        private String sensorName;
        private Double monthlyPrice;
        private Integer activeDays;
        private Double proRatedAmount;
    }
}
