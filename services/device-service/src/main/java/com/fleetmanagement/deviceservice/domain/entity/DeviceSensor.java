package com.fleetmanagement.deviceservice.domain.entity;

import com.fleetmanagement.deviceservice.domain.enums.SensorType;
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
 * Device Sensor Entity
 * Represents sensors attached to devices
 */
@Entity
@Table(name = "device_sensors", indexes = {
        @Index(name = "idx_device_sensor_device", columnList = "device_id"),
        @Index(name = "idx_device_sensor_type", columnList = "sensor_type"),
        @Index(name = "idx_device_sensor_active", columnList = "is_active")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class DeviceSensor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Device this sensor belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    /**
     * Type of sensor
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "sensor_type", nullable = false)
    private SensorType sensorType;

    /**
     * Sensor identifier on the device
     */
    @Column(name = "sensor_identifier", length = 50)
    private String sensorIdentifier;

    /**
     * Human-readable sensor name
     */
    @Column(name = "sensor_name", nullable = false, length = 100)
    private String sensorName;

    /**
     * Whether this sensor is currently active
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Sensor configuration as JSON
     */
    @Column(name = "configuration", columnDefinition = "TEXT")
    private String configuration;

    /**
     * Last reading value
     */
    @Column(name = "last_reading_value", length = 100)
    private String lastReadingValue;

    /**
     * Last reading timestamp
     */
    @Column(name = "last_reading_at")
    private LocalDateTime lastReadingAt;

    /**
     * Calibration data
     */
    @Column(name = "calibration_data", columnDefinition = "TEXT")
    private String calibrationData;

    /**
     * Alert thresholds as JSON
     */
    @Column(name = "alert_thresholds", columnDefinition = "TEXT")
    private String alertThresholds;

    /**
     * Sensor subscriptions
     */
    @OneToMany(mappedBy = "deviceSensor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SensorSubscription> subscriptions = new ArrayList<>();

    /**
     * Audit fields
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Business methods

    /**
     * Check if sensor is active
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    /**
     * Get active subscription
     */
    public SensorSubscription getActiveSubscription() {
        return subscriptions.stream()
                .filter(SensorSubscription::isActive)
                .findFirst()
                .orElse(null);
    }

    /**
     * Update sensor reading
     */
    public void updateReading(String value) {
        this.lastReadingValue = value;
        this.lastReadingAt = LocalDateTime.now();
    }

    /**
     * Activate sensor
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Deactivate sensor
     */
    public void deactivate() {
        this.isActive = false;
    }
}