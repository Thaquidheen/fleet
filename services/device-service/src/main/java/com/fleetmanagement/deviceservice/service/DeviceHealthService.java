package com.fleetmanagement.deviceservice.service;

import com.fleetmanagement.deviceservice.domain.entity.DeviceHealth;
import com.fleetmanagement.deviceservice.dto.request.DeviceHealthRequest;
import com.fleetmanagement.deviceservice.dto.response.DeviceHealthResponse;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Device Health Service Interface
 * Device health monitoring and diagnostics
 */
public interface DeviceHealthService {

    /**
     * Record device health
     */
    DeviceHealth recordDeviceHealth(String deviceId, DeviceHealthRequest healthData);

    /**
     * Get latest device health
     */
    Optional<DeviceHealthResponse> getLatestDeviceHealth(String deviceId);

    /**
     * Get device health history
     */
    List<DeviceHealthResponse> getDeviceHealthHistory(String deviceId, LocalDateTime from, LocalDateTime to);

    /**
     * Check device health
     */
    DeviceHealthResponse checkDeviceHealth(String deviceId);

    /**
     * Get devices with poor health
     */
    List<DeviceHealthResponse> getDevicesWithPoorHealth(UUID companyId);

    /**
     * Get devices needing attention
     */
    List<DeviceHealthResponse> getDevicesNeedingAttention(UUID companyId);

    /**
     * Update device communication status
     */
    void updateCommunicationStatus(String deviceId, boolean successful);

    /**
     * Calculate device health score
     */
    Integer calculateHealthScore(DeviceHealth healthRecord);

    /**
     * Schedule health check
     */
    void scheduleHealthCheck(String deviceId);

    /**
     * Process health alerts
     */
    void processHealthAlerts();
}


