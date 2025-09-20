package com.fleetmanagement.deviceservice.event.publisher;

import com.fleetmanagement.deviceservice.domain.entity.Device;
import com.fleetmanagement.deviceservice.domain.enums.ConnectionStatus;
import com.fleetmanagement.deviceservice.event.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Device Event Publisher
 * Publishes device-related events to Kafka topics
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeviceEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publish device registered event
     */
    public void publishDeviceRegistered(Device device) {
        try {
            DeviceRegisteredEvent event = DeviceRegisteredEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .deviceId(device.getDeviceId())
                    .deviceName(device.getDeviceName())
                    .deviceType(device.getDeviceType())
                    .deviceBrand(device.getDeviceBrand())
                    .companyId(device.getCompanyId())
                    .traccarId(device.getTraccarId())
                    .status(device.getStatus())
                    .build();

            kafkaTemplate.send("device.registered", device.getDeviceId(), event);
            log.debug("Published device registered event for device: {}", device.getDeviceId());

        } catch (Exception e) {
            log.error("Failed to publish device registered event for device: {}", device.getDeviceId(), e);
        }
    }

    /**
     * Publish mobile device registered event
     */
    public void publishMobileDeviceRegistered(String deviceId, UUID driverId) {
        try {
            MobileDeviceRegisteredEvent event = MobileDeviceRegisteredEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .deviceId(deviceId)
                    .driverId(driverId)
                    .build();

            kafkaTemplate.send("device.mobile.registered", deviceId, event);
            log.debug("Published mobile device registered event for device: {}", deviceId);

        } catch (Exception e) {
            log.error("Failed to publish mobile device registered event for device: {}", deviceId, e);
        }
    }

    /**
     * Publish mobile device updated event
     */
    public void publishMobileDeviceUpdated(String deviceId, UUID driverId) {
        try {
            MobileDeviceUpdatedEvent event = MobileDeviceUpdatedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .deviceId(deviceId)
                    .driverId(driverId)
                    .build();

            kafkaTemplate.send("device.mobile.updated", deviceId, event);
            log.debug("Published mobile device updated event for device: {}", deviceId);

        } catch (Exception e) {
            log.error("Failed to publish mobile device updated event for device: {}", deviceId, e);
        }
    }

    /**
     * Publish device assigned event
     */
    public void publishDeviceAssigned(String deviceId, UUID vehicleId, UUID companyId, UUID assignedBy) {
        try {
            DeviceAssignedEvent event = DeviceAssignedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .deviceId(deviceId)
                    .vehicleId(vehicleId)
                    .companyId(companyId)
                    .assignedBy(assignedBy)
                    .build();

            kafkaTemplate.send("device.assigned", deviceId, event);
            log.debug("Published device assigned event for device: {} to vehicle: {}", deviceId, vehicleId);

        } catch (Exception e) {
            log.error("Failed to publish device assigned event for device: {}", deviceId, e);
        }
    }

    /**
     * Publish device unassigned event
     */
    public void publishDeviceUnassigned(String deviceId, UUID vehicleId, UUID companyId, UUID unassignedBy, String reason) {
        try {
            DeviceUnassignedEvent event = DeviceUnassignedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .deviceId(deviceId)
                    .vehicleId(vehicleId)
                    .companyId(companyId)
                    .unassignedBy(unassignedBy)
                    .reason(reason)
                    .build();

            kafkaTemplate.send("device.unassigned", deviceId, event);
            log.debug("Published device unassigned event for device: {} from vehicle: {}", deviceId, vehicleId);

        } catch (Exception e) {
            log.error("Failed to publish device unassigned event for device: {}", deviceId, e);
        }
    }

    /**
     * Publish device status changed event
     */
    public void publishDeviceStatusChanged(Device device, String previousStatus, String changedBy) {
        try {
            DeviceStatusChangedEvent event = DeviceStatusChangedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .deviceId(device.getDeviceId())
                    .companyId(device.getCompanyId())
                    .previousStatus(previousStatus)
                    .newStatus(device.getStatus().name())
                    .changedBy(changedBy)
                    .build();

            kafkaTemplate.send("device.status.changed", device.getDeviceId(), event);
            log.debug("Published device status changed event for device: {} from {} to {}",
                    device.getDeviceId(), previousStatus, device.getStatus());

        } catch (Exception e) {
            log.error("Failed to publish device status changed event for device: {}", device.getDeviceId(), e);
        }
    }

    /**
     * Publish device connection status changed event
     */
    public void publishDeviceConnectionStatusChanged(String deviceId, ConnectionStatus previousStatus,
                                                     ConnectionStatus newStatus, UUID companyId) {
        try {
            DeviceConnectionStatusEvent event = DeviceConnectionStatusEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .deviceId(deviceId)
                    .companyId(companyId)
                    .previousStatus(previousStatus.name())
                    .newStatus(newStatus.name())
                    .build();

            kafkaTemplate.send("device.connection.status", deviceId, event);
            log.debug("Published device connection status changed event for device: {} from {} to {}",
                    deviceId, previousStatus, newStatus);

        } catch (Exception e) {
            log.error("Failed to publish device connection status changed event for device: {}", deviceId, e);
        }
    }

    /**
     * Publish driver tracking started event
     */
    public void publishDriverTrackingStarted(UUID driverId, String deviceId, String shiftId) {
        try {
            DriverTrackingStartedEvent event = DriverTrackingStartedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .driverId(driverId)
                    .deviceId(deviceId)
                    .shiftId(shiftId)
                    .build();

            kafkaTemplate.send("device.tracking.started", deviceId, event);
            log.debug("Published driver tracking started event for driver: {} with device: {}", driverId, deviceId);

        } catch (Exception e) {
            log.error("Failed to publish driver tracking started event for driver: {}", driverId, e);
        }
    }

    /**
     * Publish driver tracking stopped event
     */
    public void publishDriverTrackingStopped(UUID driverId, String deviceId, String shiftId) {
        try {
            DriverTrackingStoppedEvent event = DriverTrackingStoppedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .driverId(driverId)
                    .deviceId(deviceId)
                    .shiftId(shiftId)
                    .build();

            kafkaTemplate.send("device.tracking.stopped", deviceId, event);
            log.debug("Published driver tracking stopped event for driver: {} with device: {}", driverId, deviceId);

        } catch (Exception e) {
            log.error("Failed to publish driver tracking stopped event for driver: {}", driverId, e);
        }
    }

    /**
     * Publish device command sent event
     */
    public void publishDeviceCommandSent(String deviceId, UUID commandId, String commandType, UUID initiatedBy) {
        try {
            DeviceCommandSentEvent event = DeviceCommandSentEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .deviceId(deviceId)
                    .commandId(commandId)
                    .commandType(commandType)
                    .initiatedBy(initiatedBy)
                    .build();

            kafkaTemplate.send("device.command.sent", deviceId, event);
            log.debug("Published device command sent event for device: {} command: {}", deviceId, commandType);

        } catch (Exception e) {
            log.error("Failed to publish device command sent event for device: {}", deviceId, e);
        }
    }

    /**
     * Publish device health update event
     */
    public void publishDeviceHealthUpdate(String deviceId, UUID companyId, String healthLevel, Integer healthScore) {
        try {
            DeviceHealthUpdateEvent event = DeviceHealthUpdateEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .deviceId(deviceId)
                    .companyId(companyId)
                    .healthLevel(healthLevel)
                    .healthScore(healthScore)
                    .build();

            kafkaTemplate.send("device.health.updated", deviceId, event);
            log.debug("Published device health update event for device: {} health: {}", deviceId, healthLevel);

        } catch (Exception e) {
            log.error("Failed to publish device health update event for device: {}", deviceId, e);
        }
    }
}