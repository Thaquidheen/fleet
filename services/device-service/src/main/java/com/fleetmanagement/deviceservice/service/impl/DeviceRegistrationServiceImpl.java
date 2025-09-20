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
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Device Registration Service Implementation
 * Handles device registration, configuration, and initial setup
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeviceRegistrationServiceImpl implements DeviceRegistrationService {

    private final DeviceRepository deviceRepository;
    private final DeviceSensorRepository deviceSensorRepository;
    private final DeviceVehicleAssignmentRepository vehicleAssignmentRepository;
    private final DeviceUserAssignmentRepository userAssignmentRepository;

    private final TraccarIntegrationService traccarService;
    private final CompanyServiceClient companyClient;
    private final VehicleServiceClient vehicleClient;
    private final UserServiceClient userClient;
    private final SensorSubscriptionService sensorSubscriptionService;

    private final DeviceEventPublisher eventPublisher;

    @Override
    public DeviceResponse registerDevice(RegisterDeviceRequest request) {
        log.info("Registering device: {} for company: {}", request.getImei(), request.getCompanyId());

        try {
            // 1. Validate request and company limits
            validateDeviceRegistrationRequest(request);
            validateCompanyDeviceLimits(request.getCompanyId(), DeviceType.valueOf(request.getDeviceType()));

            // 2. Check if device already exists
            if (deviceRepository.existsByDeviceId(request.getImei())) {
                throw new DeviceAlreadyExistsException("Device with IMEI " + request.getImei() + " already exists");
            }

            // 3. Create device in Traccar first
            TraccarDevice traccarDevice = traccarService.createTraccarDevice(
                    request.getDeviceName(),
                    request.getImei(),
                    DeviceType.valueOf(request.getDeviceType())
            );
            log.debug("Created Traccar device with ID: {}", traccarDevice.getId());

            // 4. Create device entity
            Device device = createDeviceEntity(request, traccarDevice.getId());
            device = deviceRepository.save(device);
            log.debug("Saved device entity with ID: {}", device.getId());

            // 5. Configure device based on type and brand
            configureDevice(device, request);

            // 6. Handle vehicle assignment if provided
            if (request.getAssignedVehicleId() != null) {
                assignDeviceToVehicle(device, request.getAssignedVehicleId(), request.getCompanyId());
            }

            // 7. Handle user assignment if provided (for mobile devices)
            if (request.getAssignedUserId() != null) {
                assignDeviceToUser(device, request.getAssignedUserId(), request.getCompanyId());
            }

            // 8. Initialize sensor subscriptions if requested
            if (request.getSensorTypes() != null && !request.getSensorTypes().isEmpty()) {
                initializeSensorSubscriptions(device, request.getSensorTypes());
            }

            // 9. Activate device if requested
            if (Boolean.TRUE.equals(request.getAutoActivate())) {
                device.activate();
                deviceRepository.save(device);
            }

            // 10. Send device configuration to Traccar
            sendDeviceConfiguration(device);

            // 11. Publish device registered event
            eventPublisher.publishDeviceRegistered(device);

            log.info("Successfully registered device: {} with ID: {}", device.getDeviceId(), device.getId());
            return mapToDeviceResponse(device);

        } catch (Exception e) {
            log.error("Failed to register device: {} for company: {}", request.getImei(), request.getCompanyId(), e);
            throw new DeviceRegistrationException("Failed to register device: " + e.getMessage(), e);
        }
    }

    @Override
    public void validateCompanyDeviceLimits(UUID companyId, DeviceType deviceType) {
        try {
            // Get company information
            CompanyResponse company = companyClient.getCompany(companyId);
            if (company == null) {
                throw new InvalidCompanyException("Company not found: " + companyId);
            }

            if (!Boolean.TRUE.equals(company.getIsActive())) {
                throw new InvalidCompanyException("Company is not active: " + companyId);
            }

            // Check general device limit
            long currentDeviceCount = deviceRepository.countByCompanyIdAndStatus(companyId, DeviceStatus.ACTIVE);
            if (currentDeviceCount >= company.getMaxDevices()) {
                throw new DeviceRegistrationException(
                        String.format("Company has reached maximum device limit: %d/%d",
                                currentDeviceCount, company.getMaxDevices()));
            }

            // Check mobile device specific limits
            if (deviceType == DeviceType.MOBILE_PHONE) {
                long currentMobileCount = deviceRepository.countByCompanyIdAndDeviceType(companyId, DeviceType.MOBILE_PHONE);
                Integer maxMobileDevices = company.getMaxMobileDevices() != null ? company.getMaxMobileDevices() : company.getMaxDevices();

                if (currentMobileCount >= maxMobileDevices) {
                    throw new DeviceRegistrationException(
                            String.format("Company has reached maximum mobile device limit: %d/%d",
                                    currentMobileCount, maxMobileDevices));
                }
            }

            log.debug("Device limits validated for company: {} - Current: {}, Max: {}",
                    companyId, currentDeviceCount, company.getMaxDevices());

        } catch (Exception e) {
            if (e instanceof DeviceRegistrationException || e instanceof InvalidCompanyException) {
                throw e;
            }
            throw new DeviceRegistrationException("Failed to validate company device limits: " + e.getMessage(), e);
        }
    }

    @Override
    public void configureDevice(Device device, RegisterDeviceRequest request) {
        log.debug("Configuring device: {} of type: {}", device.getDeviceId(), device.getDeviceType());

        try {
            DeviceBrand brand = device.getDeviceBrand();
            DeviceType type = device.getDeviceType();

            // Configure based on device brand and type
            switch (brand) {
                case TELTONIKA -> configureTeltonikaDevice(device, request);
                case QUECLINK -> configureQueclinkDevice(device, request);
                case CONCOX -> configureConcoxDevice(device, request);
                case MEITRACK -> configureMeitrackDevice(device, request);
                case MOBILE -> configureMobileDevice(device, request);
                default -> configureGenericDevice(device, request);
            }

            // Configure Traccar device attributes
            traccarService.configureDeviceForBrand(device, brand);

            log.debug("Device configuration completed for: {}", device.getDeviceId());

        } catch (Exception e) {
            log.error("Failed to configure device: {}", device.getDeviceId(), e);
            throw new DeviceConfigurationException("Failed to configure device: " + e.getMessage(), e);
        }
    }

    @Override
    public void initializeSensorSubscriptions(Device device, List<String> sensorTypes) {
        log.debug("Initializing sensor subscriptions for device: {}", device.getDeviceId());

        try {
            for (String sensorTypeStr : sensorTypes) {
                SensorType sensorType = SensorType.valueOf(sensorTypeStr);

                // Create sensor subscription request
                SensorSubscriptionRequest subscriptionRequest = SensorSubscriptionRequest.builder()
                        .deviceId(device.getDeviceId())
                        .companyId(device.getCompanyId())
                        .sensorType(sensorType)
                        .sensorName(sensorType.getDisplayName())
                        .monthlyPrice(sensorType.getBaseMonthlyPrice())
                        .autoRenewal(true)
                        .build();

                // Subscribe to sensor
                sensorSubscriptionService.subscribeSensorToDevice(subscriptionRequest);
                log.debug("Initialized sensor subscription: {} for device: {}", sensorType, device.getDeviceId());
            }

        } catch (Exception e) {
            log.error("Failed to initialize sensor subscriptions for device: {}", device.getDeviceId(), e);
            throw new SensorSubscriptionException("Failed to initialize sensor subscriptions: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendDeviceConfiguration(Device device) {
        log.debug("Sending device configuration to Traccar for device: {}", device.getDeviceId());

        try {
            Map<String, Object> attributes = buildDeviceAttributes(device);
            traccarService.updateDeviceAttributes(device.getDeviceId(), attributes);
            log.debug("Device configuration sent successfully for: {}", device.getDeviceId());

        } catch (Exception e) {
            log.error("Failed to send device configuration for: {}", device.getDeviceId(), e);
            // Don't throw exception here as device is already registered
            // Just log the error and continue
        }
    }

    // Private helper methods

    private void validateDeviceRegistrationRequest(RegisterDeviceRequest request) {
        if (request.getCompanyId() == null) {
            throw new InvalidDeviceRequestException("Company ID is required");
        }
        if (request.getImei() == null || request.getImei().trim().isEmpty()) {
            throw new InvalidDeviceRequestException("Device IMEI is required");
        }
        if (request.getDeviceName() == null || request.getDeviceName().trim().isEmpty()) {
            throw new InvalidDeviceRequestException("Device name is required");
        }
        if (request.getDeviceType() == null) {
            throw new InvalidDeviceRequestException("Device type is required");
        }
        if (request.getBrand() == null) {
            throw new InvalidDeviceRequestException("Device brand is required");
        }

        // Validate enum values
        try {
            DeviceType.valueOf(request.getDeviceType());
            DeviceBrand.valueOf(request.getBrand());
        } catch (IllegalArgumentException e) {
            throw new InvalidDeviceRequestException("Invalid device type or brand: " + e.getMessage());
        }
    }

    private Device createDeviceEntity(RegisterDeviceRequest request, Long traccarId) {
        DeviceConfiguration configuration = request.getConfiguration() != null
                ? request.getConfiguration()
                : createDefaultConfiguration(DeviceType.valueOf(request.getDeviceType()));

        return Device.builder()
                .deviceId(request.getImei())
                .deviceName(request.getDeviceName())
                .deviceType(DeviceType.valueOf(request.getDeviceType()))
                .deviceBrand(DeviceBrand.valueOf(request.getBrand()))
                .companyId(request.getCompanyId())
                .traccarId(traccarId)
                .status(DeviceStatus.INACTIVE) // Will be activated if autoActivate is true
                .connectionStatus(ConnectionStatus.DISCONNECTED)
                .protocolType(determineProtocolType(DeviceBrand.valueOf(request.getBrand())))
                .configuration(configuration)
                .serialNumber(request.getSerialNumber())
                .firmwareVersion(request.getFirmwareVersion())
                .hardwareVersion(request.getHardwareVersion())
                .createdAt(LocalDateTime.now())
                .build();
    }

    private DeviceConfiguration createDefaultConfiguration(DeviceType deviceType) {
        DeviceConfiguration.DeviceConfigurationBuilder builder = DeviceConfiguration.builder()
                .locationEnabled(true)
                .sensorsEnabled(true)
                .commandsEnabled(true);

        // Set device type specific defaults
        switch (deviceType) {
            case MOBILE_PHONE -> {
                builder.updateInterval(30)
                        .minDistance(50)
                        .minAngle(15)
                        .timeout(180);
            }
            case GPS_TRACKER, OBD_TRACKER -> {
                builder.updateInterval(60)
                        .minDistance(100)
                        .minAngle(30)
                        .timeout(300);
            }
            default -> {
                builder.updateInterval(120)
                        .minDistance(200)
                        .minAngle(45)
                        .timeout(600);
            }
        }

        return builder.build();
    }

    private ProtocolType determineProtocolType(DeviceBrand brand) {
        return switch (brand) {
            case TELTONIKA, CONCOX, MEITRACK -> ProtocolType.TCP;
            case QUECLINK -> ProtocolType.HTTP;
            case MOBILE -> ProtocolType.WEBSOCKET;
            default -> ProtocolType.TCP;
        };
    }

    private void assignDeviceToVehicle(Device device, UUID vehicleId, UUID companyId) {
        try {
            // Validate vehicle exists and belongs to company
            VehicleResponse vehicle = vehicleClient.getVehicle(vehicleId);
            if (vehicle == null || !vehicle.getCompanyId().equals(companyId)) {
                throw new InvalidVehicleAssignmentException("Vehicle not found or doesn't belong to company");
            }

            // Create vehicle assignment
            DeviceVehicleAssignment assignment = DeviceVehicleAssignment.builder()
                    .device(device)
                    .vehicleId(vehicleId)
                    .companyId(companyId)
                    .status(AssignmentStatus.ASSIGNED)
                    .assignedAt(LocalDateTime.now())
                    .assignedBy(companyId) // TODO: Get actual user ID
                    .build();

            vehicleAssignmentRepository.save(assignment);
            log.debug("Assigned device: {} to vehicle: {}", device.getDeviceId(), vehicleId);

        } catch (Exception e) {
            log.error("Failed to assign device to vehicle", e);
            throw new DeviceAssignmentException("Failed to assign device to vehicle: " + e.getMessage(), e);
        }
    }

    private void assignDeviceToUser(Device device, UUID userId, UUID companyId) {
        try {
            // Validate user exists and belongs to company
            UserResponse user = userClient.getUser(userId);
            if (user == null || !user.getCompanyId().equals(companyId)) {
                throw new InvalidUserAssignmentException("User not found or doesn't belong to company");
            }

            // Create user assignment
            DeviceUserAssignment assignment = DeviceUserAssignment.builder()
                    .device(device)
                    .userId(userId)
                    .companyId(companyId)
                    .status(AssignmentStatus.ASSIGNED)
                    .assignedAt(LocalDateTime.now())
                    .assignedBy(companyId) // TODO: Get actual user ID
                    .trackingEnabled(false) // Default to disabled, can be enabled later
                    .build();

            userAssignmentRepository.save(assignment);
            log.debug("Assigned device: {} to user: {}", device.getDeviceId(), userId);

        } catch (Exception e) {
            log.error("Failed to assign device to user", e);
            throw new DeviceAssignmentException("Failed to assign device to user: " + e.getMessage(), e);
        }
    }

    // Device brand specific configuration methods

    private void configureTeltonikaDevice(Device device, RegisterDeviceRequest request) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("protocol", "teltonika");
        attributes.put("port", "8091");
        attributes.put("updateInterval", device.getConfiguration().getUpdateInterval());
        attributes.put("minDistance", device.getConfiguration().getMinDistance());

        device.getConfiguration().setTraccarAttributes(attributes.toString());
        log.debug("Configured Teltonika device: {}", device.getDeviceId());
    }

    private void configureQueclinkDevice(Device device, RegisterDeviceRequest request) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("protocol", "queclink");
        attributes.put("port", "8092");
        attributes.put("updateInterval", device.getConfiguration().getUpdateInterval());

        device.getConfiguration().setTraccarAttributes(attributes.toString());
        log.debug("Configured Queclink device: {}", device.getDeviceId());
    }

    private void configureConcoxDevice(Device device, RegisterDeviceRequest request) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("protocol", "concox");
        attributes.put("port", "8093");
        attributes.put("updateInterval", device.getConfiguration().getUpdateInterval());

        device.getConfiguration().setTraccarAttributes(attributes.toString());
        log.debug("Configured Concox device: {}", device.getDeviceId());
    }

    private void configureMeitrackDevice(Device device, RegisterDeviceRequest request) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("protocol", "meitrack");
        attributes.put("updateInterval", device.getConfiguration().getUpdateInterval());

        device.getConfiguration().setTraccarAttributes(attributes.toString());
        log.debug("Configured Meitrack device: {}", device.getDeviceId());
    }

    private void configureMobileDevice(Device device, RegisterDeviceRequest request) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("protocol", "osmand");
        attributes.put("category", "mobile");
        attributes.put("updateInterval", "30");
        attributes.put("minDistance", "10");

        device.getConfiguration().setTraccarAttributes(attributes.toString());
        log.debug("Configured mobile device: {}", device.getDeviceId());
    }

    private void configureGenericDevice(Device device, RegisterDeviceRequest request) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("protocol", "generic");
        attributes.put("updateInterval", device.getConfiguration().getUpdateInterval());

        device.getConfiguration().setTraccarAttributes(attributes.toString());
        log.debug("Configured generic device: {}", device.getDeviceId());
    }

    private Map<String, Object> buildDeviceAttributes(Device device) {
        Map<String, Object> attributes = new HashMap<>();

        // Basic attributes
        attributes.put("deviceType", device.getDeviceType().name());
        attributes.put("deviceBrand", device.getDeviceBrand().name());
        attributes.put("companyId", device.getCompanyId().toString());

        // Configuration attributes
        if (device.getConfiguration() != null) {
            attributes.put("updateInterval", device.getConfiguration().getUpdateInterval());
            attributes.put("minDistance", device.getConfiguration().getMinDistance());
            attributes.put("minAngle", device.getConfiguration().getMinAngle());
            attributes.put("timeout", device.getConfiguration().getTimeout());
            attributes.put("locationEnabled", device.getConfiguration().getLocationEnabled());
            attributes.put("sensorsEnabled", device.getConfiguration().getSensorsEnabled());
            attributes.put("commandsEnabled", device.getConfiguration().getCommandsEnabled());
        }

        // Hardware attributes
        if (device.getSerialNumber() != null) {
            attributes.put("serialNumber", device.getSerialNumber());
        }
        if (device.getFirmwareVersion() != null) {
            attributes.put("firmwareVersion", device.getFirmwareVersion());
        }
        if (device.getHardwareVersion() != null) {
            attributes.put("hardwareVersion", device.getHardwareVersion());
        }

        return attributes;
    }

    private DeviceResponse mapToDeviceResponse(Device device) {
        // TODO: Implement proper mapping to DeviceResponse
        // This would include loading related entities (assignments, sensors, health, etc.)
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
}