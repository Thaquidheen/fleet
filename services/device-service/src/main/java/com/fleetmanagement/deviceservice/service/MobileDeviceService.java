
package com.fleetmanagement.deviceservice.service;
import com.fleetmanagement.deviceservice.domain.entity.Device;
import com.fleetmanagement.deviceservice.dto.request.RegisterMobileDeviceRequest;
import com.fleetmanagement.deviceservice.dto.response.MobileDeviceResponse;

import java.util.Optional;
import java.util.UUID;



/**
 * Mobile Device Service Interface
 * Mobile device specific operations
 */
public interface MobileDeviceService {

    /**
     * Register mobile device for driver
     */
    MobileDeviceResponse registerMobileDevice(RegisterMobileDeviceRequest request);

    /**
     * Update mobile device
     */
    MobileDeviceResponse updateMobileDevice(String deviceId, RegisterMobileDeviceRequest request);

    /**
     * Enable driver tracking
     */
    void enableDriverTracking(UUID driverId, String shiftId);

    /**
     * Disable driver tracking
     */
    void disableDriverTracking(UUID driverId);

    /**
     * Send mobile app configuration
     */
    void sendMobileAppConfiguration(String deviceId, UUID companyId);

    /**
     * Send tracking command to mobile app
     */
    void sendTrackingCommand(Device device, String command, String shiftId);

    /**
     * Get mobile device by driver
     */
    Optional<MobileDeviceResponse> getMobileDeviceByDriver(UUID driverId);

    /**
     * Update mobile device location settings
     */
    void updateLocationSettings(String deviceId, Integer updateInterval, Boolean backgroundTracking);
}
