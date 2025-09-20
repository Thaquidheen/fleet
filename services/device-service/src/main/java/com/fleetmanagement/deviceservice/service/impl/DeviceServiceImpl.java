// services/device-service/src/main/java/com/fleetmanagement/deviceservice/service/impl/DeviceServiceImpl.java
package com.fleetmanagement.deviceservice.service.impl;

import com.fleetmanagement.deviceservice.domain.entity.Device;
import com.fleetmanagement.deviceservice.domain.enums.DeviceStatus;
import com.fleetmanagement.deviceservice.dto.request.RegisterDeviceRequest;
import com.fleetmanagement.deviceservice.dto.response.DeviceResponse;
import com.fleetmanagement.deviceservice.exception.DeviceNotFoundException;
import com.fleetmanagement.deviceservice.exception.DeviceAlreadyExistsException;
import com.fleetmanagement.deviceservice.external.client.TraccarApiClient;
import com.fleetmanagement.deviceservice.external.dto.TraccarDevice;
import com.fleetmanagement.deviceservice.repository.DeviceRepository;
import com.fleetmanagement.deviceservice.service.DeviceService;
import com.fleetmanagement.deviceservice.event.publisher.DeviceEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final TraccarApiClient traccarApiClient;
    private final DeviceEventPublisher eventPublisher;

    @Override
    public DeviceResponse registerDevice(RegisterDeviceRequest request) {
        log.info("Registering device: {}", request.getDeviceId());

        // Check if device already exists
        if (deviceRepository.existsByDeviceId(request.getDeviceId())) {
            throw new DeviceAlreadyExistsException("Device already exists: " + request.getDeviceId());
        }

        try {
            // Create device in Traccar first
            TraccarDevice traccarDevice = traccarApiClient.createDevice(
                    request.getDeviceName(),
                    request.getDeviceId()
            );

            // Create device in our system
            Device device = Device.builder()
                    .deviceId(request.getDeviceId())
                    .companyId(request.getCompanyId())
                    .traccarId(traccarDevice.getId())
                    .deviceName(request.getDeviceName())
                    .deviceType(request.getDeviceType())
                    .deviceBrand(request.getDeviceBrand())
                    .deviceModel(request.getDeviceModel())
                    .simCardNumber(request.getSimCardNumber())
                    .phoneNumber(request.getPhoneNumber())
                    .status(DeviceStatus.PENDING_ACTIVATION)
                    .installationDate(LocalDateTime.now())
                    .isActive(true)
                    .createdBy(request.getCreatedBy())
                    .build();

            device = deviceRepository.save(device);

            // Publish device registered event
            eventPublisher.publishDeviceRegistered(device);

            log.info("Device registered successfully: {}", device.getDeviceId());
            return mapToDeviceResponse(device);

        } catch (Exception e) {
            log.error("Error registering device: {}", request.getDeviceId(), e);
            throw new RuntimeException("Failed to register device", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceResponse getDevice(UUID deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found: " + deviceId));
        return mapToDeviceResponse(device);
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceResponse getDeviceByDeviceId(String deviceId) {
        Device device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found: " + deviceId));
        return mapToDeviceResponse(device);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DeviceResponse> getCompanyDevices(UUID companyId, Pageable pageable) {
        Page<Device> devices = deviceRepository.findByCompanyId(companyId, pageable);
        return devices.map(this::mapToDeviceResponse);
    }

    @Override
    public DeviceResponse assignDeviceToVehicle(UUID deviceId, UUID vehicleId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found: " + deviceId));

        device.setVehicleId(vehicleId);
        device.setStatus(DeviceStatus.ACTIVE);
        device = deviceRepository.save(device);

        // Publish device assigned event
        eventPublisher.publishDeviceAssigned(device, vehicleId);

        log.info("Device {} assigned to vehicle {}", deviceId, vehicleId);
        return mapToDeviceResponse(device);
    }

    @Override
    public DeviceResponse assignDeviceToUser(UUID deviceId, UUID userId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found: " + deviceId));

        device.setAssignedUserId(userId);
        device.setStatus(DeviceStatus.ACTIVE);
        device = deviceRepository.save(device);

        // Publish device assigned event
        eventPublisher.publishDeviceAssignedToUser(device, userId);

        log.info("Device {} assigned to user {}", deviceId, userId);
        return mapToDeviceResponse(device);
    }

    @Override
    public void activateDevice(UUID deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found: " + deviceId));

        device.setStatus(DeviceStatus.ACTIVE);
        device = deviceRepository.save(device);

        // Publish device activated event
        eventPublisher.publishDeviceStatusChanged(device, DeviceStatus.ACTIVE);

        log.info("Device {} activated", deviceId);
    }

    @Override
    public void deactivateDevice(UUID deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found: " + deviceId));

        device.setStatus(DeviceStatus.INACTIVE);
        device = deviceRepository.save(device);

        // Publish device deactivated event
        eventPublisher.publishDeviceStatusChanged(device, DeviceStatus.INACTIVE);

        log.info("Device {} deactivated", deviceId);
    }

    @Override
    public void deleteDevice(UUID deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found: " + deviceId));

        try {
            // Delete from Traccar if exists
            if (device.getTraccarId() != null) {
                traccarApiClient.deleteDevice(device.getTraccarId());
            }

            // Soft delete in our system
            device.setIsActive(false);
            device.setStatus(DeviceStatus.DECOMMISSIONED);
            deviceRepository.save(device);

            log.info("Device {} deleted", deviceId);

        } catch (Exception e) {
            log.error("Error deleting device: {}", deviceId, e);
            throw new RuntimeException("Failed to delete device", e);
        }
    }

    @Override
    public void sendCommand(UUID deviceId, String commandType) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found: " + deviceId));

        if (device.getTraccarId() == null) {
            throw new RuntimeException("Device not linked to Traccar: " + deviceId);
        }

        try {
            traccarApiClient.sendCommand(device.getTraccarId(), commandType);
            log.info("Command {} sent to device {}", commandType, deviceId);
        } catch (Exception e) {
            log.error("Error sending command to device: {}", deviceId, e);
            throw new RuntimeException("Failed to send command", e);
        }
    }

    private DeviceResponse mapToDeviceResponse(Device device) {
        return DeviceResponse.builder()
                .id(device.getId())
                .deviceId(device.getDeviceId())
                .companyId(device.getCompanyId())
                .traccarId(device.getTraccarId())
                .deviceName(device.getDeviceName())
                .deviceType(device.getDeviceType())
                .deviceBrand(device.getDeviceBrand())
                .deviceModel(device.getDeviceModel())
                .simCardNumber(device.getSimCardNumber())
                .phoneNumber(device.getPhoneNumber())
                .status(device.getStatus())
                .vehicleId(device.getVehicleId())
                .assignedUserId(device.getAssignedUserId())
                .installationDate(device.getInstallationDate())
                .lastCommunication(device.getLastCommunication())
                .firmwareVersion(device.getFirmwareVersion())
                .isActive(device.getIsActive())
                .createdAt(device.getCreatedAt())
                .updatedAt(device.getUpdatedAt())
                .build();
    }
}

