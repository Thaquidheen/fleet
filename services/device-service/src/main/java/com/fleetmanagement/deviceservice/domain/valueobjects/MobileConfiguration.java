package com.fleetmanagement.deviceservice.domain.valueobjects;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;


@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class MobileConfiguration {

    /**
     * Mobile phone number
     */
    @Column(name = "mobile_phone_number", length = 20)
    private String phoneNumber;

    /**
     * Mobile app version
     */
    @Column(name = "mobile_app_version", length = 20)
    private String appVersion;

    /**
     * Operating system (Android/iOS)
     */
    @Column(name = "mobile_os", length = 20)
    private String operatingSystem;

    /**
     * Push notification token
     */
    @Column(name = "mobile_push_token", length = 500)
    private String pushNotificationToken;

    /**
     * Mobile-specific update interval
     */
    @Column(name = "mobile_update_interval")
    @Builder.Default
    private Integer updateInterval = 30;

    /**
     * Background tracking enabled
     */
    @Column(name = "mobile_background_tracking")
    @Builder.Default
    private Boolean backgroundTrackingEnabled = true;

    /**
     * Battery optimization mode
     */
    @Column(name = "mobile_battery_optimization")
    @Builder.Default
    private Boolean batteryOptimization = true;

    /**
     * Tracking accuracy level (HIGH, MEDIUM, LOW)
     */
    @Column(name = "mobile_accuracy_level", length = 20)
    @Builder.Default
    private String accuracyLevel = "MEDIUM";
}