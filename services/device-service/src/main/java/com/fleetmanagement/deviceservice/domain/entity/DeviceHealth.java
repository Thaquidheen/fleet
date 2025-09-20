

package com.fleetmanagement.deviceservice.domain.entity;

import com.fleetmanagement.deviceservice.domain.enums.CommandStatus;
import com.fleetmanagement.deviceservice.domain.enums.HealthLevel;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Device Health Entity
 * Tracks device health and performance metrics
 */
@Entity
@Table(name = "device_health", indexes = {
        @Index(name = "idx_device_health_device", columnList = "device_id"),
        @Index(name = "idx_device_health_level", columnList = "health_level"),
        @Index(name = "idx_device_health_recorded", columnList = "recorded_at")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class DeviceHealth {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Device this health record belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    /**
     * Overall health level
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "health_level", nullable = false)
    private HealthLevel healthLevel;

    /**
     * Health score (0-100)
     */
    @Column(name = "health_score", nullable = false)
    @Builder.Default
    private Integer healthScore = 100;

    /**
     * Battery level percentage (for mobile devices)
     */
    @Column(name = "battery_level")
    private Integer batteryLevel;

    /**
     * Signal strength (0-100)
     */
    @Column(name = "signal_strength")
    private Integer signalStrength;

    /**
     * GPS accuracy in meters
     */
    @Column(name = "gps_accuracy")
    private Double gpsAccuracy;

    /**
     * Number of satellites
     */
    @Column(name = "satellite_count")
    private Integer satelliteCount;

    /**
     * Device temperature in Celsius
     */
    @Column(name = "device_temperature")
    private Double deviceTemperature;

    /**
     * Memory usage percentage
     */
    @Column(name = "memory_usage")
    private Integer memoryUsage;

    /**
     * CPU usage percentage
     */
    @Column(name = "cpu_usage")
    private Integer cpuUsage;

    /**
     * Network latency in milliseconds
     */
    @Column(name = "network_latency")
    private Long networkLatency;

    /**
     * Last successful communication
     */
    @Column(name = "last_communication")
    private LocalDateTime lastCommunication;

    /**
     * Communication failure count in last 24 hours
     */
    @Column(name = "communication_failures")
    @Builder.Default
    private Integer communicationFailures = 0;

    /**
     * Uptime in seconds
     */
    @Column(name = "uptime_seconds")
    private Long uptimeSeconds;

    /**
     * Additional health metrics as JSON
     */
    @Column(name = "additional_metrics", columnDefinition = "TEXT")
    private String additionalMetrics;

    /**
     * Health check notes
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * When this health record was captured
     */
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

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
     * Check if device health is critical
     */
    public boolean isCritical() {
        return healthLevel == HealthLevel.CRITICAL || healthScore < 20;
    }

    /**
     * Check if device needs attention
     */
    public boolean needsAttention() {
        return healthLevel == HealthLevel.POOR || healthLevel == HealthLevel.CRITICAL;
    }

    /**
     * Calculate health score based on metrics
     */
    public void calculateHealthScore() {
        int score = 100;

        // Reduce score based on various factors
        if (batteryLevel != null && batteryLevel < 20) score -= 20;
        if (signalStrength != null && signalStrength < 30) score -= 15;
        if (communicationFailures > 5) score -= 25;
        if (gpsAccuracy != null && gpsAccuracy > 50) score -= 10;
        if (deviceTemperature != null && (deviceTemperature > 60 || deviceTemperature < -20)) score -= 15;

        this.healthScore = Math.max(0, score);

        // Update health level based on score
        if (healthScore >= 80) this.healthLevel = HealthLevel.EXCELLENT;
        else if (healthScore >= 60) this.healthLevel = HealthLevel.GOOD;
        else if (healthScore >= 40) this.healthLevel = HealthLevel.FAIR;
        else if (healthScore >= 20) this.healthLevel = HealthLevel.POOR;
        else this.healthLevel = HealthLevel.CRITICAL;
    }

    /**
     * Update communication status
     */
    public void updateCommunication(boolean successful) {
        this.lastCommunication = LocalDateTime.now();
        if (!successful) {
            this.communicationFailures++;
        }
        calculateHealthScore();
    }
}