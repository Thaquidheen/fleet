package com.fleetmanagement.deviceservice.domain.valueobjects;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Device Configuration Value Object
 * Embeddable configuration for different device types
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceConfiguration {

    /**
     * Update interval in seconds
     */
    @Column(name = "update_interval")
    @Builder.Default
    private Integer updateInterval = 60;

    /**
     * Minimum distance for location updates (meters)
     */
    @Column(name = "min_distance")
    @Builder.Default
    private Integer minDistance = 100;

    /**
     * Minimum angle change for updates (degrees)
     */
    @Column(name = "min_angle")
    @Builder.Default
    private Integer minAngle = 30;

    /**
     * Device timeout in seconds
     */
    @Column(name = "timeout")
    @Builder.Default
    private Integer timeout = 300;

    /**
     * Enable/disable location tracking
     */
    @Column(name = "location_enabled")
    @Builder.Default
    private Boolean locationEnabled = true;

    /**
     * Enable/disable sensor data collection
     */
    @Column(name = "sensors_enabled")
    @Builder.Default
    private Boolean sensorsEnabled = true;

    /**
     * Enable/disable commands
     */
    @Column(name = "commands_enabled")
    @Builder.Default
    private Boolean commandsEnabled = true;

    /**
     * Mobile-specific configuration
     */
    @Embedded
    private MobileConfiguration mobileConfiguration;

    /**
     * Traccar-specific attributes as JSON
     */
    @Column(name = "traccar_attributes", columnDefinition = "TEXT")
    private String traccarAttributes;

    /**
     * Custom device attributes as JSON
     */
    @Column(name = "custom_attributes", columnDefinition = "TEXT")
    private String customAttributes;

    // Helper methods for mobile devices
    public boolean isMobileDevice() {
        return mobileConfiguration != null;
    }

    public Integer getMobileUpdateInterval() {
        return mobileConfiguration != null ? mobileConfiguration.getUpdateInterval() : updateInterval;
    }
}