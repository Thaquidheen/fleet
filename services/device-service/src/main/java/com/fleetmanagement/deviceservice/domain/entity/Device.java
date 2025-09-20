package com.fleetmanagement.deviceservice.domain.entity;

import com.fleetmanagement.deviceservice.domain.enums.*;
import com.fleetmanagement.deviceservice.domain.valueobjects.DeviceConfiguration;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Device Entity
 * Core entity representing IoT devices in the fleet management system
 * Supports multiple device brands and types with Traccar integration
 */
@Entity
@Table(name = "devices", indexes = {
        @Index(name = "idx_device_company", columnList = "companyId"),
        @Index(name = "idx_device_imei", columnList = "deviceId"),
        @Index(name = "idx_device_traccar", columnList = "traccarId"),
        @Index(name = "idx_device_status", columnList = "status"),
        @Index(name = "idx_device_type_brand", columnList = "deviceType, deviceBrand")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Unique device identifier (IMEI for hardware devices, generated for mobile)
     */
    @Column(name = "device_id", unique = true, nullable = false, length = 50)
    private String deviceId;

    /**
     * Human-readable device name
     */
    @Column(name = "device_name", nullable = false, length = 100)
    private String deviceName;

    /**
     * Device type (GPS_TRACKER, MOBILE_PHONE, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false)
    private DeviceType deviceType;

    /**
     * Device brand (TELTONIKA, QUECLINK, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "device_brand", nullable = false)
    private DeviceBrand deviceBrand;

    /**
     * Current operational status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private DeviceStatus status = DeviceStatus.ACTIVE;

    /**
     * Company that owns this device
     */
    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    /**
     * Traccar device ID for integration
     */
    @Column(name = "traccar_id")
    private Long traccarId;

    /**
     * Communication protocol type
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "protocol_type")
    private ProtocolType protocolType;

    /**
     * Current connection status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "connection_status")
    @Builder.Default
    private ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED;

    /**
     * Device configuration as JSON
     */
    @Embedded
    private DeviceConfiguration configuration;

    /**
     * Device serial number or hardware identifier
     */
    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    /**
     * Firmware version
     */
    @Column(name = "firmware_version", length = 50)
    private String firmwareVersion;

    /**
     * Hardware version
     */
    @Column(name = "hardware_version", length = 50)
    private String hardwareVersion;

    /**
     * Last communication timestamp
     */
    @Column(name = "last_communication")
    private LocalDateTime lastCommunication;

    /**
     * Device activation date
     */
    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    /**
     * Device deactivation date
     */
    @Column(name = "deactivated_at")
    private LocalDateTime deactivatedAt;

    /**
     * Device sensors (one-to-many relationship)
     */
    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DeviceSensor> sensors = new ArrayList<>();

    /**
     * Device-vehicle assignments
     */
    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DeviceVehicleAssignment> vehicleAssignments = new ArrayList<>();

    /**
     * Device-user assignments (for mobile devices)
     */
    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DeviceUserAssignment> userAssignments = new ArrayList<>();

    /**
     * Device commands
     */
    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DeviceCommand> commands = new ArrayList<>();

    /**
     * Device health records
     */
    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DeviceHealth> healthRecords = new ArrayList<>();

    /**
     * Audit fields
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // Business methods

    /**
     * Check if device is currently active
     */
    public boolean isActive() {
        return status == DeviceStatus.ACTIVE;
    }

    /**
     * Check if device is connected
     */
    public boolean isConnected() {
        return connectionStatus == ConnectionStatus.CONNECTED;
    }

    /**
     * Check if device is a mobile device
     */
    public boolean isMobileDevice() {
        return deviceType == DeviceType.MOBILE_PHONE;
    }

    /**
     * Check if device is a hardware GPS tracker
     */
    public boolean isHardwareTracker() {
        return deviceType == DeviceType.GPS_TRACKER || deviceType == DeviceType.OBD_TRACKER;
    }

    /**
     * Get current vehicle assignment
     */
    public DeviceVehicleAssignment getCurrentVehicleAssignment() {
        return vehicleAssignments.stream()
                .filter(assignment -> assignment.getStatus() == AssignmentStatus.ASSIGNED)
                .findFirst()
                .orElse(null);
    }

    /**
     * Get current user assignment (for mobile devices)
     */
    public DeviceUserAssignment getCurrentUserAssignment() {
        return userAssignments.stream()
                .filter(assignment -> assignment.getStatus() == AssignmentStatus.ASSIGNED)
                .findFirst()
                .orElse(null);
    }

    /**
     * Get active sensors
     */
    public List<DeviceSensor> getActiveSensors() {
        return sensors.stream()
                .filter(sensor -> sensor.isActive())
                .toList();
    }

    /**
     * Calculate monthly sensor cost
     */
    public double calculateMonthlySensorCost() {
        return getActiveSensors().stream()
                .flatMap(sensor -> sensor.getSubscriptions().stream())
                .filter(subscription -> subscription.isActive())
                .mapToDouble(subscription -> subscription.getMonthlyPrice())
                .sum();
    }

    /**
     * Update connection status and last communication
     */
    public void updateConnectionStatus(ConnectionStatus newStatus) {
        this.connectionStatus = newStatus;
        if (newStatus == ConnectionStatus.CONNECTED) {
            this.lastCommunication = LocalDateTime.now();
        }
    }

    /**
     * Activate device
     */
    public void activate() {
        this.status = DeviceStatus.ACTIVE;
        this.activatedAt = LocalDateTime.now();
        this.deactivatedAt = null;
    }

    /**
     * Deactivate device
     */
    public void deactivate() {
        this.status = DeviceStatus.INACTIVE;
        this.deactivatedAt = LocalDateTime.now();
        this.connectionStatus = ConnectionStatus.DISCONNECTED;
    }
}
