package com.fleetmanagement.deviceservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceTypeResponse {

    /**
     * Unique device type identifier
     */
    private UUID id;

    /**
     * Device type name/identifier
     */
    private String typeName;

    /**
     * Human-readable display name
     */
    private String displayName;

    /**
     * Device type description
     */
    private String description;

    /**
     * Device category (HARDWARE, MOBILE, IOT, SENSOR)
     */
    private String category;

    /**
     * Whether this device type is currently active
     */
    private Boolean isActive;

    /**
     * Supported device brands for this type
     */
    private List<String> supportedBrands;

    /**
     * Supported communication protocols
     */
    private List<String> supportedProtocols;

    /**
     * Default communication protocol
     */
    private String defaultProtocol;

    /**
     * Default TCP port for this device type
     */
    private Integer defaultPort;

    // Capability flags
    private Boolean supportsGps;
    private Boolean supportsSensors;
    private Boolean supportsCommands;
    private Boolean supportsOtaUpdates;
    private Boolean supportsGeofencing;
    private Boolean canAssignToVehicle;
    private Boolean canAssignToUser;

    /**
     * Device limits and restrictions
     */
    private Integer maxDevicesPerCompany;

    // Default configuration values
    private Integer defaultUpdateInterval;
    private Integer defaultMinDistance;
    private Integer defaultMinAngle;
    private Integer defaultTimeout;

    /**
     * Compatible sensor types for this device type
     */
    private List<String> compatibleSensorTypes;

    /**
     * Required capabilities for this device type
     */
    private List<String> requiredCapabilities;

    /**
     * Optional capabilities for this device type
     */
    private List<String> optionalCapabilities;

    /**
     * Device type specific configuration schema
     */
    private Map<String, Object> configurationSchema;

    /**
     * Default configuration values
     */
    private Map<String, Object> defaultConfiguration;

    /**
     * Validation rules as JSON
     */
    private String validationRules;

    // Hardware specifications
    private String iconUrl;
    private String manufacturer;
    private String model;
    private String hardwareVersion;
    private String firmwareVersion;
    private String powerRequirements;
    private String temperatureRange;
    private String ipRating;
    private List<String> connectivityOptions;
    private String antennaRequirements;

    // Documentation
    private String installationNotes;
    private String troubleshootingGuideUrl;
    private String userManualUrl;

    /**
     * Tags for categorization and search
     */
    private List<String> tags;

    /**
     * Additional metadata
     */
    private Map<String, Object> metadata;

    // Usage statistics
    private DeviceTypeStatistics statistics;

    // Audit information
    private UUID createdBy;
    private String createdByName;
    private UUID updatedBy;
    private String updatedByName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Computed properties

    /**
     * Check if device type supports mobile devices
     */
    public boolean isMobileDeviceType() {
        return "MOBILE".equals(category) || Boolean.TRUE.equals(canAssignToUser);
    }

    /**
     * Check if device type supports hardware trackers
     */
    public boolean isHardwareDeviceType() {
        return "HARDWARE".equals(category);
    }

    /**
     * Check if device type supports IoT sensors
     */
    public boolean isIotDeviceType() {
        return "IOT".equals(category) || "SENSOR".equals(category);
    }

    /**
     * Get primary use case based on capabilities
     */
    public String getPrimaryUseCase() {
        if (Boolean.TRUE.equals(canAssignToUser)) {
            return "Driver Tracking";
        } else if (Boolean.TRUE.equals(canAssignToVehicle)) {
            return "Vehicle Tracking";
        } else if (Boolean.TRUE.equals(supportsSensors)) {
            return "Asset Monitoring";
        } else {
            return "Location Tracking";
        }
    }

    /**
     * Get compatibility score (0-100) based on capabilities
     */
    public Integer getCompatibilityScore() {
        int score = 0;
        int maxScore = 0;

        // GPS capability (25 points)
        maxScore += 25;
        if (Boolean.TRUE.equals(supportsGps)) score += 25;

        // Sensor capability (20 points)
        maxScore += 20;
        if (Boolean.TRUE.equals(supportsSensors)) score += 20;

        // Command capability (20 points)
        maxScore += 20;
        if (Boolean.TRUE.equals(supportsCommands)) score += 20;

        // OTA updates (15 points)
        maxScore += 15;
        if (Boolean.TRUE.equals(supportsOtaUpdates)) score += 15;

        // Geofencing (10 points)
        maxScore += 10;
        if (Boolean.TRUE.equals(supportsGeofencing)) score += 10;

        // Assignment flexibility (10 points)
        maxScore += 10;
        if (Boolean.TRUE.equals(canAssignToVehicle) || Boolean.TRUE.equals(canAssignToUser)) {
            score += 10;
        }

        return maxScore > 0 ? (score * 100) / maxScore : 0;
    }

    /**
     * Get feature summary
     */
    public List<String> getFeatureSummary() {
        List<String> features = new java.util.ArrayList<>();

        if (Boolean.TRUE.equals(supportsGps)) features.add("GPS Tracking");
        if (Boolean.TRUE.equals(supportsSensors)) features.add("Sensor Data");
        if (Boolean.TRUE.equals(supportsCommands)) features.add("Remote Commands");
        if (Boolean.TRUE.equals(supportsOtaUpdates)) features.add("OTA Updates");
        if (Boolean.TRUE.equals(supportsGeofencing)) features.add("Geofencing");
        if (Boolean.TRUE.equals(canAssignToVehicle)) features.add("Vehicle Assignment");
        if (Boolean.TRUE.equals(canAssignToUser)) features.add("User Assignment");

        return features;
    }

    /**
     * Device Type Statistics inner class
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceTypeStatistics {

        /**
         * Total number of devices of this type
         */
        private Long totalDevices;

        /**
         * Number of active devices
         */
        private Long activeDevices;

        /**
         * Number of inactive devices
         */
        private Long inactiveDevices;

        /**
         * Number of devices with issues
         */
        private Long devicesWithIssues;

        /**
         * Number of companies using this device type
         */
        private Long companiesUsing;

        /**
         * Average devices per company
         */
        private Double avgDevicesPerCompany;

        /**
         * Most popular brands for this device type
         */
        private List<BrandUsage> popularBrands;

        /**
         * Usage by country/region
         */
        private Map<String, Long> usageByRegion;

        /**
         * Monthly registration trend (last 12 months)
         */
        private List<MonthlyUsage> registrationTrend;

        /**
         * Average uptime percentage
         */
        private Double avgUptimePercentage;

        /**
         * Average battery life (for battery-powered devices)
         */
        private Double avgBatteryLifeDays;

        /**
         * Most common issues/alerts
         */
        private List<IssueStatistics> commonIssues;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime lastUpdated;
    }

    /**
     * Brand Usage Statistics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BrandUsage {
        private String brandName;
        private Long deviceCount;
        private Double percentage;
        private Double avgRating;
    }

    /**
     * Monthly Usage Statistics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyUsage {
        private Integer year;
        private Integer month;
        private Long registrations;
        private Long activations;
        private Long deactivations;
    }

    /**
     * Issue Statistics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IssueStatistics {
        private String issueType;
        private String description;
        private Long occurrences;
        private Double percentage;
        private String severity;
    }

    // Helper methods for creating common responses

    /**
     * Create a basic device type response
     */
    public static DeviceTypeResponse createBasic(UUID id, String typeName, String displayName,
                                                 String category, Boolean isActive) {
        return DeviceTypeResponse.builder()
                .id(id)
                .typeName(typeName)
                .displayName(displayName)
                .category(category)
                .isActive(isActive)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create a summary response (minimal fields)
     */
    public static DeviceTypeResponse createSummary(UUID id, String typeName, String displayName,
                                                   String category, Boolean isActive, Long totalDevices) {
        DeviceTypeStatistics stats = DeviceTypeStatistics.builder()
                .totalDevices(totalDevices)
                .lastUpdated(LocalDateTime.now())
                .build();

        return DeviceTypeResponse.builder()
                .id(id)
                .typeName(typeName)
                .displayName(displayName)
                .category(category)
                .isActive(isActive)
                .statistics(stats)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Check if device type is suitable for a specific use case
     */
    public boolean isSuitableFor(String useCase) {
        return switch (useCase.toLowerCase()) {
            case "vehicle_tracking" -> Boolean.TRUE.equals(canAssignToVehicle) && Boolean.TRUE.equals(supportsGps);
            case "driver_tracking" -> Boolean.TRUE.equals(canAssignToUser) && Boolean.TRUE.equals(supportsGps);
            case "asset_monitoring" -> Boolean.TRUE.equals(supportsSensors);
            case "fleet_management" -> Boolean.TRUE.equals(supportsGps) && Boolean.TRUE.equals(supportsCommands);
            case "cold_chain" -> Boolean.TRUE.equals(supportsSensors) &&
                    compatibleSensorTypes != null && compatibleSensorTypes.contains("TEMPERATURE");
            case "fuel_monitoring" -> Boolean.TRUE.equals(supportsSensors) &&
                    compatibleSensorTypes != null && compatibleSensorTypes.contains("FUEL");
            default -> false;
        };
    }

    /**
     * Get recommended configuration for this device type
     */
    public Map<String, Object> getRecommendedConfiguration() {
        Map<String, Object> config = new java.util.HashMap<>();

        if (defaultUpdateInterval != null) {
            config.put("updateInterval", defaultUpdateInterval);
        }
        if (defaultMinDistance != null) {
            config.put("minDistance", defaultMinDistance);
        }
        if (defaultMinAngle != null) {
            config.put("minAngle", defaultMinAngle);
        }
        if (defaultTimeout != null) {
            config.put("timeout", defaultTimeout);
        }
        if (defaultProtocol != null) {
            config.put("protocol", defaultProtocol);
        }
        if (defaultPort != null) {
            config.put("port", defaultPort);
        }

        // Add capability-based defaults
        config.put("locationEnabled", Boolean.TRUE.equals(supportsGps));
        config.put("sensorsEnabled", Boolean.TRUE.equals(supportsSensors));
        config.put("commandsEnabled", Boolean.TRUE.equals(supportsCommands));
        config.put("geofencingEnabled", Boolean.TRUE.equals(supportsGeofencing));

        return config;
    }
}