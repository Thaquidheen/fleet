package com.fleetmanagement.deviceservice.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;

import com.fleetmanagement.deviceservice.dto.DeviceResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Device List Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceListResponse {

    private List<DeviceResponse> devices;
    private PaginationInfo pagination;
    private DeviceListStatistics statistics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo {
        private Integer page;
        private Integer size;
        private Long totalElements;
        private Integer totalPages;
        private Boolean hasNext;
        private Boolean hasPrevious;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceListStatistics {
        private Long totalDevices;
        private Long activeDevices;
        private Long connectedDevices;
        private Long mobileDevices;
        private Long devicesWithSensors;
        private Double totalMonthlySensorRevenue;
    }
}
