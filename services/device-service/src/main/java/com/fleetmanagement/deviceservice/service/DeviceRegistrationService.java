package com.fleetmanagement.deviceservice.service;

import com.fleetmanagement.deviceservice.domain.entity.Device;
import com.fleetmanagement.deviceservice.domain.enums.DeviceType;
import com.fleetmanagement.deviceservice.dto.DeviceResponse;
import com.fleetmanagement.deviceservice.dto.request.RegisterDeviceRequest;
import com.fleetmanagement.deviceservice.dto.request.RegisterMobileDeviceRequest;
import com.fleetmanagement.deviceservice.dto.response.MobileDeviceResponse;
import java.util.List;
import java.util.UUID;

/**
 * Device Registration Service Interface
 * Device registration and configuration
 */
public interface DeviceRegistrationService {

    /**
     * Register hardware device
     */
    DeviceResponse registerDevice(RegisterDeviceRequest request);

    /**
     * Register mobile device
     */
    MobileDeviceResponse registerMobileDevice(RegisterMobileDeviceRequest request);

    /**
     * Configure device by type
     */
    void configureDevice(Device device, RegisterDeviceRequest request);

    /**
     * Validate company device limits
     */
    void validateCompanyDeviceLimits(UUID companyId, DeviceType deviceType);

    /**
     * Initialize sensor subscriptions
     */
    void initializeSensorSubscriptions(Device device, List<String> sensorTypes);

    /**
     * Send device configuration
     */
    void sendDeviceConfiguration(Device device);
}

