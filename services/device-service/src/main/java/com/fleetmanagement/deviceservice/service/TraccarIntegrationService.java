package com.fleetmanagement.deviceservice.service;

import com.fleetmanagement.deviceservice.domain.entity.Device;
import com.fleetmanagement.deviceservice.domain.enums.DeviceBrand;
import com.fleetmanagement.deviceservice.domain.enums.DeviceType;
import com.fleetmanagement.deviceservice.dto.request.DeviceCommandRequest;
import com.fleetmanagement.deviceservice.dto.response.CommandExecutionResponse;
import com.fleetmanagement.deviceservice.external.dto.TraccarDevice;
import com.fleetmanagement.deviceservice.external.dto.TraccarPosition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Traccar Integration Service Interface
 * Traccar server integration
 */
public interface TraccarIntegrationService {

    /**
     * Create device in Traccar
     */
    TraccarDevice createTraccarDevice(String deviceName, String uniqueId, DeviceType deviceType);

    /**
     * Update device in Traccar
     */
    TraccarDevice updateTraccarDevice(Long traccarId, String deviceName, Device device);

    /**
     * Delete device from Traccar
     */
    void deleteTraccarDevice(Long traccarId);

    /**
     * Get device from Traccar
     */
    Optional<TraccarDevice> getTraccarDevice(Long traccarId);

    /**
     * Update device attributes in Traccar
     */
    void updateDeviceAttributes(String deviceId, Map<String, Object> attributes);

    /**
     * Send command to device via Traccar
     */
    void sendCommandToDevice(Long traccarId, String commandType, Map<String, Object> parameters);

    /**
     * Get device positions from Traccar
     */
    List<TraccarPosition> getDevicePositions(Long traccarId, LocalDateTime from, LocalDateTime to);

    /**
     * Get latest device position from Traccar
     */
    Optional<TraccarPosition> getLatestDevicePosition(Long traccarId);

    /**
     * Sync device with Traccar
     */
    void syncDeviceWithTraccar(Device device);

    /**
     * Configure device for specific brand
     */
    void configureDeviceForBrand(Device device, DeviceBrand brand);
}


