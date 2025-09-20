package com.fleetmanagement.deviceservice.controller;

import com.fleetmanagement.deviceservice.dto.response.DeviceHealthResponse;
import com.fleetmanagement.deviceservice.dto.common.ApiResponse;
import com.fleetmanagement.deviceservice.dto.common.PagedResponse;
import com.fleetmanagement.deviceservice.service.DeviceHealthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Device Health Monitoring
 * 
 * Provides endpoints for monitoring device health status,
 * performance metrics, and health-related alerts.
 */
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Tag(name = "Device Health Monitoring", description = "Device health status monitoring and performance metrics")
public class DeviceHealthController {

    private final DeviceHealthService deviceHealthService;

    /**
     * Get health status of all devices
     * 
     * @param pageable pagination parameters
     * @param companyId filter by company ID
     * @param healthLevel filter by health level
     * @return paginated list of device health status
     */
    @GetMapping("/devices")
    @Operation(summary = "Get device health status", description = "Retrieve health status of all devices with optional filtering")
    public ResponseEntity<ApiResponse<PagedResponse<DeviceHealthResponse>>> getDeviceHealthStatus(
            @PageableDefault(size = 20, sort = "lastChecked", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(description = "Filter by company ID") @RequestParam(required = false) Long companyId,
            @Parameter(description = "Filter by health level") @RequestParam(required = false) String healthLevel) {
        
        PagedResponse<DeviceHealthResponse> healthStatus = deviceHealthService
                .getDeviceHealthStatus(pageable, companyId, healthLevel);
        return ResponseEntity.ok(ApiResponse.success(healthStatus, "Device health status retrieved successfully"));
    }

    /**
     * Get health status of specific device
     * 
     * @param deviceId device ID
     * @return device health information
     */
    @GetMapping("/devices/{deviceId}")
    @Operation(summary = "Get device health by ID", description = "Retrieve health status of a specific device")
    public ResponseEntity<ApiResponse<DeviceHealthResponse>> getDeviceHealthById(
            @Parameter(description = "Device ID") @PathVariable Long deviceId) {
        DeviceHealthResponse healthStatus = deviceHealthService.getDeviceHealthById(deviceId);
        return ResponseEntity.ok(ApiResponse.success(healthStatus, "Device health status retrieved successfully"));
    }

    /**
     * Get devices with critical health issues
     * 
     * @param pageable pagination parameters
     * @param companyId filter by company ID
     * @return paginated list of devices with critical health issues
     */
    @GetMapping("/devices/critical")
    @Operation(summary = "Get devices with critical health", description = "Retrieve devices with critical health issues")
    public ResponseEntity<ApiResponse<PagedResponse<DeviceHealthResponse>>> getDevicesWithCriticalHealth(
            @PageableDefault(size = 20, sort = "lastChecked", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(description = "Filter by company ID") @RequestParam(required = false) Long companyId) {
        
        PagedResponse<DeviceHealthResponse> criticalDevices = deviceHealthService
                .getDevicesWithCriticalHealth(pageable, companyId);
        return ResponseEntity.ok(ApiResponse.success(criticalDevices, "Critical health devices retrieved successfully"));
    }

    /**
     * Get devices with warning health status
     * 
     * @param pageable pagination parameters
     * @param companyId filter by company ID
     * @return paginated list of devices with warning health status
     */
    @GetMapping("/devices/warning")
    @Operation(summary = "Get devices with warning health", description = "Retrieve devices with warning health status")
    public ResponseEntity<ApiResponse<PagedResponse<DeviceHealthResponse>>> getDevicesWithWarningHealth(
            @PageableDefault(size = 20, sort = "lastChecked", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(description = "Filter by company ID") @RequestParam(required = false) Long companyId) {
        
        PagedResponse<DeviceHealthResponse> warningDevices = deviceHealthService
                .getDevicesWithWarningHealth(pageable, companyId);
        return ResponseEntity.ok(ApiResponse.success(warningDevices, "Warning health devices retrieved successfully"));
    }

    /**
     * Get healthy devices
     * 
     * @param pageable pagination parameters
     * @param companyId filter by company ID
     * @return paginated list of healthy devices
     */
    @GetMapping("/devices/healthy")
    @Operation(summary = "Get healthy devices", description = "Retrieve devices with healthy status")
    public ResponseEntity<ApiResponse<PagedResponse<DeviceHealthResponse>>> getHealthyDevices(
            @PageableDefault(size = 20, sort = "lastChecked", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(description = "Filter by company ID") @RequestParam(required = false) Long companyId) {
        
        PagedResponse<DeviceHealthResponse> healthyDevices = deviceHealthService
                .getHealthyDevices(pageable, companyId);
        return ResponseEntity.ok(ApiResponse.success(healthyDevices, "Healthy devices retrieved successfully"));
    }

    /**
     * Get device health history
     * 
     * @param deviceId device ID
     * @param pageable pagination parameters
     * @return paginated device health history
     */
    @GetMapping("/devices/{deviceId}/history")
    @Operation(summary = "Get device health history", description = "Retrieve health history for a specific device")
    public ResponseEntity<ApiResponse<PagedResponse<DeviceHealthResponse>>> getDeviceHealthHistory(
            @Parameter(description = "Device ID") @PathVariable Long deviceId,
            @PageableDefault(size = 50, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        
        PagedResponse<DeviceHealthResponse> healthHistory = deviceHealthService
                .getDeviceHealthHistory(deviceId, pageable);
        return ResponseEntity.ok(ApiResponse.success(healthHistory, "Device health history retrieved successfully"));
    }

    /**
     * Get health metrics summary
     * 
     * @param companyId company ID (optional)
     * @return health metrics summary
     */
    @GetMapping("/metrics/summary")
    @Operation(summary = "Get health metrics summary", description = "Retrieve overall health metrics summary")
    public ResponseEntity<ApiResponse<Object>> getHealthMetricsSummary(
            @Parameter(description = "Company ID") @RequestParam(required = false) Long companyId) {
        Object metricsSummary = deviceHealthService.getHealthMetricsSummary(companyId);
        return ResponseEntity.ok(ApiResponse.success(metricsSummary, "Health metrics summary retrieved successfully"));
    }

    /**
     * Get health alerts
     * 
     * @param pageable pagination parameters
     * @param companyId filter by company ID
     * @param severity filter by alert severity
     * @return paginated list of health alerts
     */
    @GetMapping("/alerts")
    @Operation(summary = "Get health alerts", description = "Retrieve health alerts and notifications")
    public ResponseEntity<ApiResponse<PagedResponse<Object>>> getHealthAlerts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(description = "Filter by company ID") @RequestParam(required = false) Long companyId,
            @Parameter(description = "Filter by alert severity") @RequestParam(required = false) String severity) {
        
        PagedResponse<Object> alerts = deviceHealthService.getHealthAlerts(pageable, companyId, severity);
        return ResponseEntity.ok(ApiResponse.success(alerts, "Health alerts retrieved successfully"));
    }

    /**
     * Acknowledge health alert
     * 
     * @param alertId alert ID
     * @return success response
     */
    @PostMapping("/alerts/{alertId}/acknowledge")
    @Operation(summary = "Acknowledge health alert", description = "Acknowledge a health alert")
    public ResponseEntity<ApiResponse<Void>> acknowledgeHealthAlert(
            @Parameter(description = "Alert ID") @PathVariable Long alertId) {
        deviceHealthService.acknowledgeHealthAlert(alertId);
        return ResponseEntity.ok(ApiResponse.success(null, "Health alert acknowledged successfully"));
    }

    /**
     * Get device performance metrics
     * 
     * @param deviceId device ID
     * @param timeRange time range for metrics (e.g., "1h", "24h", "7d")
     * @return device performance metrics
     */
    @GetMapping("/devices/{deviceId}/performance")
    @Operation(summary = "Get device performance metrics", description = "Retrieve performance metrics for a specific device")
    public ResponseEntity<ApiResponse<Object>> getDevicePerformanceMetrics(
            @Parameter(description = "Device ID") @PathVariable Long deviceId,
            @Parameter(description = "Time range") @RequestParam(defaultValue = "24h") String timeRange) {
        Object performanceMetrics = deviceHealthService.getDevicePerformanceMetrics(deviceId, timeRange);
        return ResponseEntity.ok(ApiResponse.success(performanceMetrics, "Device performance metrics retrieved successfully"));
    }

    /**
     * Trigger health check for device
     * 
     * @param deviceId device ID
     * @return health check result
     */
    @PostMapping("/devices/{deviceId}/check")
    @Operation(summary = "Trigger device health check", description = "Manually trigger health check for a specific device")
    public ResponseEntity<ApiResponse<DeviceHealthResponse>> triggerDeviceHealthCheck(
            @Parameter(description = "Device ID") @PathVariable Long deviceId) {
        DeviceHealthResponse healthCheck = deviceHealthService.triggerDeviceHealthCheck(deviceId);
        return ResponseEntity.ok(ApiResponse.success(healthCheck, "Device health check completed successfully"));
    }
}

