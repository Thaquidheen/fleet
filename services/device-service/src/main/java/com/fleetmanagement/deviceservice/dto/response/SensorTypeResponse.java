package com.fleetmanagement.deviceservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Sensor Type Response DTO
 * Response containing sensor type information and pricing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SensorTypeResponse {

    /**
     * Unique sensor type identifier
     */
    private UUID id;

    /**
     * Sensor type name/identifier
     */
    private String sensorTypeName;

    /**
     * Human-readable display name
     */
    private String displayName;

    /**
     * Sensor type description
     */
    private String description;

    /**
     * Sensor category (ENVIRONMENTAL, MECHANICAL, ELECTRICAL, BIOMETRIC)
     */
    private String category;

    /**
     * Measurement unit (e.g., "°C", "L", "kg", "%", "psi")
     */
    private String unit;

    /**
     * Unit display name (e.g., "Celsius", "Liters", "Kilograms")
     */
    private String unitDisplayName;

    /**
     * Whether this sensor type is currently active
     */
    private Boolean isActive;

    /**
     * Whether this sensor type is available for subscription
     */
    private Boolean isAvailableForSubscription;

    // Pricing information
    private Double baseMonthlyPrice;
    private Double maxMonthlyPrice;
    private String currency;
    private String pricingModel; // FLAT, TIERED, USAGE_BASED

    /**
     * Pricing tiers for different subscription levels
     */
    private List<PricingTier> pricingTiers;

    // Sensor capabilities and specifications
    private String dataType; // NUMERIC, BOOLEAN, STRING, JSON
    private Double minValue;
    private Double maxValue;
    private Integer precision; // Decimal places
    private Double accuracy; // Percentage accuracy
    private String readingFrequency; // How often sensor can be read
    private String responseTime; // Sensor response time

    /**
     * Compatible device types for this sensor
     */
    private List<String> compatibleDeviceTypes;

    /**
     * Compatible device brands
     */
    private List<String> compatibleDeviceBrands;

    /**
     * Required hardware capabilities
     */
    private List<String> requiredCapabilities;

    /**
     * Installation requirements
     */
    private String installationRequirements;

    /**
     * Calibration requirements
     */
    private String calibrationRequirements;

    // Configuration and thresholds
    private Map<String, Object> defaultConfiguration;
    private Map<String, Object> configurationSchema;
    private AlertThresholds defaultAlertThresholds;

    // Business rules
    private Integer maxSensorsPerDevice;
    private Integer maxSensorsPerCompany;
    private Boolean requiresCalibration;
    private Boolean supportsAlerts;
    private Boolean supportsHistoricalData;
    private Boolean supportsRealTimeData;

    // Documentation and support
    private String iconUrl;
    private String documentationUrl;
    private String installationGuideUrl;
    private String troubleshootingGuideUrl;
    private String calibrationGuideUrl;

    /**
     * Use cases and applications
     */
    private List<String> useCases;

    /**
     * Industry applications
     */
    private List<String> industries;

    /**
     * Compliance standards
     */
    private List<String> complianceStandards;

    /**
     * Tags for categorization and search
     */
    private List<String> tags;

    /**
     * Additional metadata
     */
    private Map<String, Object> metadata;

    // Statistics and usage
    private SensorTypeStatistics statistics;

    // Audit information
    private UUID createdBy;
    private String createdByName;
    private UUID updatedBy;
    private String updatedByName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Inner classes

    /**
     * Pricing Tier Information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricingTier {
        private String tierName;
        private String tierDescription;
        private Double monthlyPrice;
        private Integer minDevices;
        private Integer maxDevices;
        private List<String> includedFeatures;
        private Map<String, Object> tierConfiguration;
        private Double discountPercentage;
    }

    /**
     * Alert Thresholds Configuration
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertThresholds {
        private Double warningMin;
        private Double warningMax;
        private Double criticalMin;
        private Double criticalMax;
        private String alertCondition; // ABOVE, BELOW, OUTSIDE_RANGE, INSIDE_RANGE
        private Integer alertDelay; // Seconds before triggering alert
        private Boolean enableEmailAlerts;
        private Boolean enableSmsAlerts;
        private Boolean enablePushAlerts;
    }

    /**
     * Sensor Type Statistics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SensorTypeStatistics {

        /**
         * Total number of sensor subscriptions
         */
        private Long totalSubscriptions;

        /**
         * Number of active subscriptions
         */
        private Long activeSubscriptions;

        /**
         * Number of companies using this sensor type
         */
        private Long companiesUsing;

        /**
         * Average subscriptions per company
         */
        private Double avgSubscriptionsPerCompany;

        /**
         * Total monthly revenue from this sensor type
         */
        private Double totalMonthlyRevenue;

        /**
         * Average monthly revenue per subscription
         */
        private Double avgRevenuePerSubscription;

        /**
         * Growth rate (month over month)
         */
        private Double growthRate;

        /**
         * Most popular pricing tier
         */
        private String popularPricingTier;

        /**
         * Usage by industry
         */
        private Map<String, Long> usageByIndustry;

        /**
         * Device type compatibility usage
         */
        private Map<String, Long> usageByDeviceType;

        /**
         * Monthly subscription trend (last 12 months)
         */
        private List<MonthlySubscription> subscriptionTrend;

        /**
         * Alert frequency statistics
         */
        private AlertStatistics alertStatistics;

        /**
         * Data quality metrics
         */
        private DataQualityMetrics dataQuality;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime lastUpdated;
    }

    /**
     * Monthly Subscription Statistics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlySubscription {
        private Integer year;
        private Integer month;
        private Long newSubscriptions;
        private Long cancelledSubscriptions;
        private Long netSubscriptions;
        private Double revenue;
    }

    /**
     * Alert Statistics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertStatistics {
        private Long totalAlerts;
        private Long warningAlerts;
        private Long criticalAlerts;
        private Double avgAlertsPerDay;
        private Double falsePositiveRate;
        private String mostCommonAlertType;
    }

    /**
     * Data Quality Metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataQualityMetrics {
        private Double dataAccuracy;
        private Double dataCompleteness;
        private Double dataConsistency;
        private Double uptimePercentage;
        private Long avgReadingsPerDay;
        private Double calibrationDriftRate;
    }

    // Computed properties and business methods

    /**
     * Get effective monthly price based on tier or base price
     */
    public Double getEffectiveMonthlyPrice() {
        if (pricingTiers != null && !pricingTiers.isEmpty()) {
            return pricingTiers.get(0).getMonthlyPrice(); // Return base tier price
        }
        return baseMonthlyPrice;
    }

    /**
     * Get price for specific device count
     */
    public Double getPriceForDeviceCount(Integer deviceCount) {
        if (pricingTiers == null || pricingTiers.isEmpty()) {
            return baseMonthlyPrice;
        }

        // Find appropriate tier based on device count
        for (PricingTier tier : pricingTiers) {
            if (deviceCount >= tier.getMinDevices() &&
                    (tier.getMaxDevices() == null || deviceCount <= tier.getMaxDevices())) {
                return tier.getMonthlyPrice();
            }
        }

        return maxMonthlyPrice; // Return max price if no tier matches
    }

    /**
     * Check if sensor is suitable for specific use case
     */
    public boolean isSuitableForUseCase(String useCase) {
        return useCases != null && useCases.contains(useCase);
    }

    /**
     * Check if sensor is compatible with device type
     */
    public boolean isCompatibleWithDeviceType(String deviceType) {
        return compatibleDeviceTypes != null && compatibleDeviceTypes.contains(deviceType);
    }

    /**
     * Check if sensor supports real-time monitoring
     */
    public boolean supportsRealTimeMonitoring() {
        return Boolean.TRUE.equals(supportsRealTimeData);
    }

    /**
     * Get sensor value range as string
     */
    public String getValueRange() {
        if (minValue != null && maxValue != null) {
            return String.format("%.2f - %.2f %s", minValue, maxValue, unit != null ? unit : "");
        } else if (minValue != null) {
            return String.format("≥ %.2f %s", minValue, unit != null ? unit : "");
        } else if (maxValue != null) {
            return String.format("≤ %.2f %s", maxValue, unit != null ? unit : "");
        }
        return "No range specified";
    }

    /**
     * Get ROI calculation based on use case
     */
    public String getEstimatedROI(String useCase) {
        return switch (useCase.toLowerCase()) {
            case "fuel_monitoring" -> "15-25% fuel cost savings";
            case "temperature_monitoring" -> "90% reduction in spoilage costs";
            case "weight_monitoring" -> "95% reduction in overload fines";
            case "door_monitoring" -> "80% reduction in theft incidents";
            case "engine_monitoring" -> "20% reduction in maintenance costs";
            default -> "Varies by implementation";
        };
    }

    /**
     * Get compliance benefits
     */
    public List<String> getComplianceBenefits() {
        List<String> benefits = new java.util.ArrayList<>();

        if (complianceStandards != null) {
            for (String standard : complianceStandards) {
                switch (standard) {
                    case "HACCP" -> benefits.add("Food safety compliance");
                    case "GDP" -> benefits.add("Pharmaceutical distribution compliance");
                    case "DOT" -> benefits.add("Transportation regulation compliance");
                    case "ISO9001" -> benefits.add("Quality management compliance");
                    case "FMCSA" -> benefits.add("Federal motor carrier safety compliance");
                    default -> benefits.add(standard + " compliance");
                }
            }
        }

        return benefits;
    }

    // Helper factory methods

    /**
     * Create a basic sensor type response
     */
    public static SensorTypeResponse createBasic(UUID id, String sensorTypeName, String displayName,
                                                 String unit, Double basePrice, Boolean isActive) {
        return SensorTypeResponse.builder()
                .id(id)
                .sensorTypeName(sensorTypeName)
                .displayName(displayName)
                .unit(unit)
                .baseMonthlyPrice(basePrice)
                .isActive(isActive)
                .isAvailableForSubscription(isActive)
                .currency("USD")
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create a temperature sensor response
     */
    public static SensorTypeResponse createTemperatureSensor() {
        return SensorTypeResponse.builder()
                .sensorTypeName("TEMPERATURE")
                .displayName("Temperature Sensor")
                .description("Monitor temperature for cold chain compliance and equipment monitoring")
                .category("ENVIRONMENTAL")
                .unit("°C")
                .unitDisplayName("Celsius")
                .isActive(true)
                .isAvailableForSubscription(true)
                .baseMonthlyPrice(8.00)
                .maxMonthlyPrice(15.00)
                .currency("USD")
                .pricingModel("TIERED")
                .dataType("NUMERIC")
                .minValue(-50.0)
                .maxValue(150.0)
                .precision(1)
                .accuracy(95.0)
                .readingFrequency("Every 30 seconds")
                .responseTime("< 5 seconds")
                .compatibleDeviceTypes(List.of("GPS_TRACKER", "ASSET_TRACKER", "OBD_TRACKER"))
                .useCases(List.of("Cold Chain Monitoring", "Engine Temperature", "Cabin Climate"))
                .industries(List.of("Food & Beverage", "Pharmaceuticals", "Logistics", "HVAC"))
                .complianceStandards(List.of("HACCP", "GDP", "FDA"))
                .requiresCalibration(true)
                .supportsAlerts(true)
                .supportsHistoricalData(true)
                .supportsRealTimeData(true)
                .tags(List.of("temperature", "cold-chain", "environmental", "compliance"))
                .build();
    }

    /**
     * Create a fuel sensor response
     */
    public static SensorTypeResponse createFuelSensor() {
        return SensorTypeResponse.builder()
                .sensorTypeName("FUEL")
                .displayName("Fuel Level Sensor")
                .description("Monitor fuel consumption and detect theft or leakage")
                .category("MECHANICAL")
                .unit("L")
                .unitDisplayName("Liters")
                .isActive(true)
                .isAvailableForSubscription(true)
                .baseMonthlyPrice(10.00)
                .maxMonthlyPrice(18.00)
                .currency("USD")
                .pricingModel("TIERED")
                .dataType("NUMERIC")
                .minValue(0.0)
                .maxValue(1000.0)
                .precision(1)
                .accuracy(98.0)
                .readingFrequency("Every minute")
                .responseTime("< 3 seconds")
                .compatibleDeviceTypes(List.of("GPS_TRACKER", "OBD_TRACKER"))
                .useCases(List.of("Fuel Theft Detection", "Consumption Monitoring", "Fleet Optimization"))
                .industries(List.of("Transportation", "Logistics", "Construction", "Agriculture"))
                .complianceStandards(List.of("DOT", "FMCSA"))
                .requiresCalibration(true)
                .supportsAlerts(true)
                .supportsHistoricalData(true)
                .supportsRealTimeData(true)
                .tags(List.of("fuel", "theft-detection", "consumption", "fleet"))
                .build();
    }
}