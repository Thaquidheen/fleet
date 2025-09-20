package com.fleetmanagement.deviceservice.service.impl;

import com.fleetmanagement.deviceservice.domain.entity.*;
import com.fleetmanagement.deviceservice.domain.enums.*;
import com.fleetmanagement.deviceservice.domain.valueobjects.DeviceConfiguration;
import com.fleetmanagement.deviceservice.dto.*;
import com.fleetmanagement.deviceservice.external.dto.*;
import com.fleetmanagement.deviceservice.repository.*;
import com.fleetmanagement.deviceservice.service.*;
import com.fleetmanagement.deviceservice.external.client.*;
import com.fleetmanagement.deviceservice.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Mobile Device Service Implementation
 * Handles mobile device registration, tracking, and management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MobileDeviceServiceImpl implements MobileDeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceUserAssignmentRepository userAssignmentRepository;

    private final DeviceRegistrationService deviceRegistrationService;
    private final TraccarIntegrationService traccarService;
    private final UserServiceClient userClient;

    private final DeviceEventPublisher eventPublisher;
    private final MobileAppNotificationService notificationService;

    @Override
    public MobileDeviceResponse registerMobileDevice(RegisterMobileDeviceRequest request) {
        log.info("Registering mobile device for driver: {}", request.getDriverId());

        try {
            // 1. Validate driver exists
            UserResponse driver = userClient.getUser(request.getDriverId());
            if (driver == null) {
                throw new InvalidMobileDeviceException("Driver not found: " + request.getDriverId());
            }

            if (!Boolean.TRUE.equals(driver.getIsActive())) {
                throw new InvalidMobileDeviceException("Driver is not active: " + request.getDriverId());
            }

            // 2. Check if driver already has a mobile device
            Optional<Device> existingDevice = deviceRepository.findByAssignedUserIdAndDeviceType(
                    request.getDriverId(), DeviceType.MOBILE_PHONE);

            if (existingDevice.isPresent()) {
                log.info("Updating existing mobile device for driver: {}", request.getDriverId());
                return updateMobileDevice(existingDevice.get().getDeviceId(), request);
            }

            // 3. Create device registration request
            RegisterDeviceRequest deviceRequest = buildDeviceRegistrationRequest(request, driver);

            // 4. Register device using device registration service
            DeviceResponse deviceResponse = deviceRegistrationService.registerDevice(deviceRequest);

            // 5. Configure mobile-specific settings in Traccar
            configureMobileDeviceTracking(deviceResponse.getDeviceId(), request);

            // 6. Send configuration to mobile app
            sendMobileAppConfiguration(deviceResponse.getDeviceId(), driver.getCompanyId());

            // 7. Publish mobile device registered event
            eventPublisher.publishMobileDeviceRegistered(deviceResponse.getDeviceId(), request.getDriverId());

            log.info("Successfully registered mobile device: {} for driver: {}",
                    deviceResponse.getDeviceId(), request.getDriverId());

            return mapToMobileDeviceResponse(deviceResponse, request);

        } catch (Exception e) {
            log.error("Failed to register mobile device for driver: {}", request.getDriverId(), e);
            throw new MobileDeviceRegistrationException("Failed to register mobile device: " + e.getMessage(), e);
        }
    }

    @Override
    public MobileDeviceResponse updateMobileDevice(String deviceId, RegisterMobileDeviceRequest request) {
        log.info("Updating mobile device: {} for driver: {}", deviceId, request.getDriverId());

        try {
            Device device = deviceRepository.findByDeviceId(deviceId)
                    .orElseThrow(() -> new DeviceNotFoundException("Mobile device not found: " + deviceId));

            if (device.getDeviceType() != DeviceType.MOBILE_PHONE) {
                throw new InvalidMobileDeviceException("Device is not a mobile device: " + deviceId);
            }

            // Update device configuration
            updateMobileDeviceConfiguration(device, request);

            // Update Traccar attributes
            configureMobileDeviceTracking(deviceId, request);

            // Send updated configuration to mobile app
            sendMobileAppConfiguration(deviceId, device.getCompanyId());

            device = deviceRepository.save(device);

            // Publish update event
            eventPublisher.publishMobileDeviceUpdated(deviceId, request.getDriverId());

            log.info("Successfully updated mobile device: {}", deviceId);

            DeviceResponse deviceResponse = mapDeviceToResponse(device);
            return mapToMobileDeviceResponse(deviceResponse, request);

        } catch (Exception e) {
            log.error("Failed to update mobile device: {}", deviceId, e);
            throw new MobileDeviceUpdateException("Failed to update mobile device: " + e.getMessage(), e);
        }
    }

    @Override
    public void enableDriverTracking(UUID driverId, String shiftId) {
        log.info("Enabling tracking for driver: {} with shift: {}", driverId, shiftId);

        try {
            Device mobileDevice = deviceRepository.findByAssignedUserIdAndDeviceType(
                            driverId, DeviceType.MOBILE_PHONE)
                    .orElseThrow(() -> new DeviceNotFoundException("Mobile device not found for driver: " + driverId));

            // Update device status to active tracking
            mobileDevice.setStatus(DeviceStatus.ACTIVE);
            mobileDevice = deviceRepository.save(mobileDevice);

            // Update user assignment
            DeviceUserAssignment assignment = mobileDevice.getCurrentUserAssignment();
            if (assignment != null) {
                assignment.startTracking(shiftId);
                userAssignmentRepository.save(assignment);
            }

            // Send tracking start command to mobile app
            sendTrackingCommand(mobileDevice, "START_TRACKING", shiftId);

            // Publish event
            eventPublisher.publishDriverTrackingStarted(driverId, mobileDevice.getDeviceId(), shiftId);

            log.info("Successfully enabled tracking for driver: {}", driverId);

        } catch (Exception e) {
            log.error("Failed to enable tracking for driver: {}", driverId, e);
            throw new TrackingException("Failed to enable driver tracking: " + e.getMessage(), e);
        }
    }

    @Override
    public void disableDriverTracking(UUID driverId) {
        log.info("Disabling tracking for driver: {}", driverId);

        try {
            Device mobileDevice = deviceRepository.findByAssignedUserIdAndDeviceType(
                            driverId, DeviceType.MOBILE_PHONE)
                    .orElseThrow(() -> new DeviceNotFoundException("Mobile device not found for driver: " + driverId));

            // Update user assignment
            DeviceUserAssignment assignment = mobileDevice.getCurrentUserAssignment();
            if (assignment != null) {
                String shiftId = assignment.getCurrentShiftId();
                assignment.stopTracking();
                userAssignmentRepository.save(assignment);

                // Publish event
                eventPublisher.publishDriverTrackingStopped(driverId, mobileDevice.getDeviceId(), shiftId);
            }

            // Send tracking stop command to mobile app
            sendTrackingCommand(mobileDevice, "STOP_TRACKING", null);

            log.info("Successfully disabled tracking for driver: {}", driverId);

        } catch (Exception e) {
            log.error("Failed to disable tracking for driver: {}", driverId, e);
            throw new TrackingException("Failed to disable driver tracking: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendMobileAppConfiguration(String deviceId, UUID companyId) {
        log.debug("Sending mobile app configuration for device: {}", deviceId);

        try {
            Device device = deviceRepository.findByDeviceId(deviceId)
                    .orElseThrow(() -> new DeviceNotFoundException("Device not found: " + deviceId));

            Map<String, Object> config = buildMobileAppConfiguration(device, companyId);

            // Send configuration via push notification
            notificationService.sendConfigurationUpdate(deviceId, config);

            log.debug("Mobile app configuration sent for device: {}", deviceId);

        } catch (Exception e) {
            log.error("Failed to send mobile app configuration for device: {}", deviceId, e);
            // Don't throw exception - this is not critical
        }
    }

    @Override
    public void sendTrackingCommand(Device device, String command, String shiftId) {
        log.debug("Sending tracking command: {} to device: {}", command, device.getDeviceId());

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("command", command);
            payload.put("deviceId", device.getDeviceId());
            payload.put("timestamp", LocalDateTime.now().toString());

            if (shiftId != null) {
                payload.put("shiftId", shiftId);
            }

            // Send via push notification
            notificationService.sendTrackingCommand(device.getDeviceId(), payload);

            log.debug("Tracking command sent: {} for device: {}", command, device.getDeviceId());

        } catch (Exception e) {
            log.error("Failed to send tracking command to device: {}", device.getDeviceId(), e);
            throw new CommandException("Failed to send tracking command: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<MobileDeviceResponse> getMobileDeviceByDriver(UUID driverId) {
        log.debug("Getting mobile device for driver: {}", driverId);

        try {
            Optional<Device> deviceOpt = deviceRepository.findByAssignedUserIdAndDeviceType(
                    driverId, DeviceType.MOBILE_PHONE);

            if (deviceOpt.isEmpty()) {
                return Optional.empty();
            }

            Device device = deviceOpt.get();
            DeviceResponse deviceResponse = mapDeviceToResponse(device);

            // Get driver information
            UserResponse driver = userClient.getUser(driverId);
            RegisterMobileDeviceRequest mockRequest = buildMockRequestFromDevice(device, driver);

            MobileDeviceResponse response = mapToMobileDeviceResponse(deviceResponse, mockRequest);
            return Optional.of(response);

        } catch (Exception e) {
            log.error("Failed to get mobile device for driver: {}", driverId, e);
            throw new MobileDeviceException("Failed to get mobile device: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateLocationSettings(String deviceId, Integer updateInterval, Boolean backgroundTracking) {
        log.debug("Updating location settings for device: {}", deviceId);

        try {
            Device device = deviceRepository.findByDeviceId(deviceId)
                    .orElseThrow(() -> new DeviceNotFoundException("Device not found: " + deviceId));

            if (device.getDeviceType() != DeviceType.MOBILE_PHONE) {
                throw new InvalidMobileDeviceException("Device is not a mobile device: " + deviceId);
            }

            // Update device configuration
            DeviceConfiguration config = device.getConfiguration();
            if (config != null && config.getMobileConfiguration() != null) {
                if (updateInterval != null) {
                    config.getMobileConfiguration().setUpdateInterval(updateInterval);
                }
                if (backgroundTracking != null) {
                    config.getMobileConfiguration().setBackgroundTrackingEnabled(backgroundTracking);
                }

                device.setConfiguration(config);
                deviceRepository.save(device);

                // Send updated settings to mobile app
                sendMobileAppConfiguration(deviceId, device.getCompanyId());

                log.debug("Location settings updated for device: {}", deviceId);
            }

        } catch (Exception e) {
            log.error("Failed to update location settings for device: {}", deviceId, e);
            throw new MobileDeviceException("Failed to update location settings: " + e.getMessage(), e);
        }
    }

    // Private helper methods

    private RegisterDeviceRequest buildDeviceRegistrationRequest(RegisterMobileDeviceRequest request, UserResponse driver) {
        return RegisterDeviceRequest.builder()
                .companyId(driver.getCompanyId())
                .deviceName("Mobile - " + driver.getFirstName() + " " + driver.getLastName())
                .imei(request.getPhoneImei())
                .deviceType(DeviceType.MOBILE_PHONE.name())
                .brand(DeviceBrand.MOBILE.name())
                .assignedUserId(request.getDriverId())
                .configuration(buildMobileDeviceConfiguration(request))
                .autoActivate(true)
                .notes("Mobile device for driver: " + driver.getFirstName() + " " + driver.getLastName())
                .build();
    }

    private DeviceConfiguration buildMobileDeviceConfiguration(RegisterMobileDeviceRequest request) {
        return DeviceConfiguration.builder()
                .updateInterval(request.getUpdateInterval())
                .minDistance(10)
                .minAngle(15)
                .timeout(180)
                .locationEnabled(true)
                .sensorsEnabled(false)
                .commandsEnabled(true)
                .mobileConfiguration(DeviceConfiguration.MobileConfiguration.builder()
                        .phoneNumber(request.getPhoneNumber())
                        .appVersion(request.getAppVersion())
                        .operatingSystem(request.getOperatingSystem())
                        .pushNotificationToken(request.getPushToken())
                        .updateInterval(request.getUpdateInterval())
                        .backgroundTrackingEnabled(request.getBackgroundTracking())
                        .batteryOptimization(true)
                        .accuracyLevel("MEDIUM")
                        .build())
                .build();
    }

    private void updateMobileDeviceConfiguration(Device device, RegisterMobileDeviceRequest request) {
        DeviceConfiguration config = device.getConfiguration();
        if (config != null && config.getMobileConfiguration() != null) {
            DeviceConfiguration.MobileConfiguration mobileConfig = config.getMobileConfiguration();

            if (request.getPhoneNumber() != null) {
                mobileConfig.setPhoneNumber(request.getPhoneNumber());
            }
            if (request.getAppVersion() != null) {
                mobileConfig.setAppVersion(request.getAppVersion());
            }
            if (request.getOperatingSystem() != null) {
                mobileConfig.setOperatingSystem(request.getOperatingSystem());
            }
            if (request.getPushToken() != null) {
                mobileConfig.setPushNotificationToken(request.getPushToken());
            }
            if (request.getUpdateInterval() != null) {
                mobileConfig.setUpdateInterval(request.getUpdateInterval());
                config.setUpdateInterval(request.getUpdateInterval());
            }
            if (request.getBackgroundTracking() != null) {
                mobileConfig.setBackgroundTrackingEnabled(request.getBackgroundTracking());
            }

            device.setConfiguration(config);
        }
    }

    private void configureMobileDeviceTracking(String deviceId, RegisterMobileDeviceRequest request) {
        try {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("category", "mobile");
            attributes.put("protocol", "osmand");
            attributes.put("updateInterval", request.getUpdateInterval().toString());
            attributes.put("minDistance", "10");
            attributes.put("driverName", request.getDriverName());
            attributes.put("phoneNumber", request.getPhoneNumber());
            attributes.put("appVersion", request.getAppVersion());
            attributes.put("operatingSystem", request.getOperatingSystem());

            traccarService.updateDeviceAttributes(deviceId, attributes);
            log.debug("Configured mobile device tracking for: {}", deviceId);

        } catch (Exception e) {
            log.error("Failed to configure mobile device tracking for: {}", deviceId, e);
            // Don't throw exception - this is not critical for registration
        }
    }

    private Map<String, Object> buildMobileAppConfiguration(Device device, UUID companyId) {
        Map<String, Object> config = new HashMap<>();

        DeviceConfiguration deviceConfig = device.getConfiguration();
        if (deviceConfig != null) {
            config.put("updateInterval", deviceConfig.getUpdateInterval());
            config.put("minDistance", deviceConfig.getMinDistance());
            config.put("locationEnabled", deviceConfig.getLocationEnabled());
            config.put("commandsEnabled", deviceConfig.getCommandsEnabled());

            if (deviceConfig.getMobileConfiguration() != null) {
                DeviceConfiguration.MobileConfiguration mobileConfig = deviceConfig.getMobileConfiguration();
                config.put("backgroundTracking", mobileConfig.getBackgroundTrackingEnabled());
                config.put("batteryOptimization", mobileConfig.getBatteryOptimization());
                config.put("accuracyLevel", mobileConfig.getAccuracyLevel());
            }
        }

        // Server configuration
        config.put("serverUrl", "http://localhost:8089"); // TODO: Get from configuration
        config.put("deviceId", device.getDeviceId());
        config.put("protocol", "osmand");
        config.put("companyId", companyId.toString());

        return config;
    }

    private MobileDeviceResponse mapToMobileDeviceResponse(DeviceResponse deviceResponse, RegisterMobileDeviceRequest request) {
        DeviceUserAssignment assignment = null;
        if (deviceResponse.getCurrentUserAssignment() != null) {
            // Get assignment details from device response
        }

        return MobileDeviceResponse.builder()
                .id(deviceResponse.getId())
                .deviceId(deviceResponse.getDeviceId())
                .deviceName(deviceResponse.getDeviceName())
                .status(deviceResponse.getStatus())
                .connectionStatus(deviceResponse.getConnectionStatus())
                .companyId(deviceResponse.getCompanyId())
                .driverId(request.getDriverId())
                .driverName(request.getDriverName())
                .phoneNumber(request.getPhoneNumber())
                .appVersion(request.getAppVersion())
                .operatingSystem(request.getOperatingSystem())
                .trackingEnabled(request.getTrackingEnabled())
                .configuration(deviceResponse.getConfiguration())
                .lastCommunication(deviceResponse.getLastCommunication())
                .createdAt(deviceResponse.getCreatedAt())
                .backgroundTracking(request.getBackgroundTracking())
                .updateInterval(request.getUpdateInterval())
                .batteryLevel(deviceResponse.getLatestHealth() != null ? deviceResponse.getLatestHealth().getBatteryLevel() : null)
                .signalStrength(deviceResponse.getLatestHealth() != null ? deviceResponse.getLatestHealth().getSignalStrength() : null)
                .gpsAccuracy(deviceResponse.getLatestHealth() != null ? deviceResponse.getLatestHealth().getGpsAccuracy() : null)
                .build();
    }

    private DeviceResponse mapDeviceToResponse(Device device) {
        // Simplified mapping - in a real implementation, you'd want to load all related entities
        return DeviceResponse.builder()
                .id(device.getId())
                .deviceId(device.getDeviceId())
                .deviceName(device.getDeviceName())
                .deviceType(device.getDeviceType())
                .deviceBrand(device.getDeviceBrand())
                .status(device.getStatus())
                .connectionStatus(device.getConnectionStatus())
                .companyId(device.getCompanyId())
                .traccarId(device.getTraccarId())
                .protocolType(device.getProtocolType())
                .configuration(device.getConfiguration())
                .serialNumber(device.getSerialNumber())
                .firmwareVersion(device.getFirmwareVersion())
                .hardwareVersion(device.getHardwareVersion())
                .lastCommunication(device.getLastCommunication())
                .activatedAt(device.getActivatedAt())
                .createdAt(device.getCreatedAt())
                .createdBy(device.getCreatedBy())
                .build();
    }

    private RegisterMobileDeviceRequest buildMockRequestFromDevice(Device device, UserResponse driver) {
        DeviceConfiguration.MobileConfiguration mobileConfig =
                device.getConfiguration() != null ? device.getConfiguration().getMobileConfiguration() : null;

        return RegisterMobileDeviceRequest.builder()
                .driverId(driver.getId())
                .phoneImei(device.getDeviceId())
                .phoneNumber(mobileConfig != null ? mobileConfig.getPhoneNumber() : null)
                .appVersion(mobileConfig != null ? mobileConfig.getAppVersion() : null)
                .operatingSystem(mobileConfig != null ? mobileConfig.getOperatingSystem() : null)
                .pushToken(mobileConfig != null ? mobileConfig.getPushNotificationToken() : null)
                .driverName(driver.getFirstName() + " " + driver.getLastName())
                .updateInterval(mobileConfig != null ? mobileConfig.getUpdateInterval() : 30)
                .trackingEnabled(device.getCurrentUserAssignment() != null ?
                        device.getCurrentUserAssignment().isTrackingEnabled() : false)
                .backgroundTracking(mobileConfig != null ? mobileConfig.getBackgroundTrackingEnabled() : true)
                .build();
    }
}