package com.fleetmanagement.deviceservice.service;
import com.fleetmanagement.deviceservice.domain.enums.*;

import com.fleetmanagement.deviceservice.dto.DeviceResponse;
import com.fleetmanagement.deviceservice.dto.request.UpdateDeviceRequest;
import com.fleetmanagement.deviceservice.dto.response.DeviceListResponse;
import org.springframework.data.domain.Pageable;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Device Service Interface
 * Core device management operations
 */
public interface DeviceService {

    /**
     * Get device by ID
     */
    Optional<DeviceResponse> getDeviceById(UUID deviceId);

    /**
     * Get device by device ID (IMEI)
     */
    Optional<DeviceResponse> getDeviceByDeviceId(String deviceId);

    /**
     * Get all devices for a company
     */
    DeviceListResponse getDevicesByCompany(UUID companyId, Pageable pageable);

    /**
     * Search devices
     */
    DeviceListResponse searchDevices(UUID companyId, String searchTerm, Pageable pageable);

    /**
     * Filter devices by criteria
     */
    DeviceListResponse filterDevices(
            UUID companyId,
            DeviceStatus status,
            DeviceType deviceType,
            ConnectionStatus connectionStatus,
            Pageable pageable
    );

    /**
     * Update device
     */
    DeviceResponse updateDevice(String deviceId, UpdateDeviceRequest request);

    /**
     * Activate device
     */
    DeviceResponse activateDevice(String deviceId, UUID userId);

    /**
     * Deactivate device
     */
    DeviceResponse deactivateDevice(String deviceId, UUID userId, String reason);

    /**
     * Update device connection status
     */
    void updateConnectionStatus(String deviceId, ConnectionStatus status);

    /**
     * Get device statistics for company
     */
    DeviceListResponse.DeviceListStatistics getDeviceStatistics(UUID companyId);

    /**
     * Bulk device operations
     */
    List<DeviceResponse> bulkUpdateDevices(UUID companyId, List<UUID> deviceIds, UpdateDeviceRequest request);

    /**
     * Get devices needing health check
     */
    List<DeviceResponse> getDevicesNeedingHealthCheck();
}

