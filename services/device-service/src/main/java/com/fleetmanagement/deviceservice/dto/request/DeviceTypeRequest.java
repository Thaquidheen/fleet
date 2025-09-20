package com.fleetmanagement.deviceservice.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Device Type Request DTO
 * Used for creating and updating device types
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceTypeRequest {

    /**
     * Device type name/identifier
     */
    @NotBlank(message = "Device type name is required")
    @Size(max = 50, message = "Device type name must be less than 50 characters")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "Device type name must be uppercase with underscores (e.g., GPS_TRACKER)")
    private String typeName;

    /**
     * Human-readable display name
     */
    @NotBlank(message = "Display name is required")
    @Size(max = 100, message = "Display name must be less than 100 characters")
    private String displayName;

    /**
     * Device type description
     */
    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;

    /**
     * Device category (HARDWARE, MOBILE, IOT, SENSOR)
     */
    @NotBlank(message = "Category is required")
    @Pattern(regexp = "^(HARDWARE|MOBILE|IOT|SENSOR)$", message = "Category must be one of: HARDWARE, MOBILE, IOT, SENSOR")
    private String category;

    /**
     * Whether this device type is currently active
     */
    @Builder.Default
    private Boolean isActive = true;

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
    @Size(max = 20, message = "Default protocol must be less than 20 characters")
    private String defaultProtocol;

    /**
     * Default TCP port for this device type
     */
    @Min(value = 1, message = "Port must be greater than 0")
    @Max(value = 65535, message = "Port must be less than or equal to 65535")
    private Integer defaultPort;

    /**
     * Whether this device type supports GPS tracking
     */
    @Builder.Default
    private Boolean supportsGps = true;

    /**
     * Whether this device type supports sensor data
     */
    @Builder.Default
    private Boolean supportsSensors = false;

    /**
     * Whether this device type supports commands
     */
    @Builder.Default
    private Boolean supportsCommands = true;

    /**
     * Whether this device type supports OTA firmware updates
     */
    @Builder.Default
    private Boolean supportsOtaUpdates = false;

    /**
     * Whether this device type supports geofencing
     */
    @Builder.Default
    private Boolean supportsGeofencing = true;

    /**
     * Whether this device type can be assigned to vehicles
     */
    @Builder.Default
    private Boolean canAssignToVehicle = true;

    /**
     * Whether this device type can be assigned to users (mobile devices)
     */
    @Builder.Default
    private Boolean canAssignToUser = false;

    /**
     * Maximum number of devices of this type per company
     */
    @Min(value = 1, message = "Max devices per company must be at least 1")
    private Integer maxDevicesPerCompany;

    /**
     * Default update interval in seconds
     */
    @Min(value = 1, message = "Update interval must be at least 1 second")
    @Max(value = 86400, message = "Update interval cannot exceed 24 hours")
    @Builder.Default
    private Integer defaultUpdateInterval = 60;

    /**
     * Default minimum distance for location updates (meters)
     */
    @Min(value = 0, message = "Minimum distance must be non-negative")
    private Integer defaultMinDistance;

    /**
     * Default minimum angle for location updates (degrees)
     */
    @Min(value = 0, message = "Minimum angle must be non-negative")
    @Max(value = 360, message = "Minimum angle cannot exceed 360 degrees")
    private Integer defaultMinAngle;

    /**
     * Default timeout in seconds
     */
    @Min(value = 1, message = "Timeout must be at least 1 second")
    @Max(value = 3600, message = "Timeout cannot exceed 1 hour")
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

    /**
     * Device type icon/image URL
     */
    @Size(max = 255, message = "Icon URL must be less than 255 characters")
    private String iconUrl;

    /**
     * Manufacturer information
     */
    @Size(max = 100, message = "Manufacturer must be less than 100 characters")
    private String manufacturer;

    /**
     * Model information
     */
    @Size(max = 100, message = "Model must be less than 100 characters")
    private String model;

    /**
     * Hardware version
     */
    @Size(max = 50, message = "Hardware version must be less than 50 characters")
    private String hardwareVersion;

    /**
     * Firmware version
     */
    @Size(max = 50, message = "Firmware version must be less than 50 characters")
    private String firmwareVersion;

    /**
     * Power requirements (e.g., "12V DC", "Battery", "USB")
     */
    @Size(max = 100, message = "Power requirements must be less than 100 characters")
    private String powerRequirements;

    /**
     * Operating temperature range
     */
    @Size(max = 50, message = "Temperature range must be less than 50 characters")
    private String temperatureRange;

    /**
     * IP rating (e.g., "IP67", "IP68")
     */
    @Size(max = 10, message = "IP rating must be less than 10 characters")
    private String ipRating;

    /**
     * Connectivity options (e.g., "2G/3G/4G", "WiFi", "Bluetooth")
     */
    private List<String> connectivityOptions;

    /**
     * Antenna requirements
     */
    @Size(max = 200, message = "Antenna requirements must be less than 200 characters")
    private String antennaRequirements;

    /**
     * Installation notes
     */
    @Size(max = 1000, message = "Installation notes must be less than 1000 characters")
    private String installationNotes;

    /**
     * Troubleshooting guide URL
     */
    @Size(max = 255, message = "Troubleshooting guide URL must be less than 255 characters")
    private String troubleshootingGuideUrl;

    /**
     * User manual URL
     */
    @Size(max = 255, message = "User manual URL must be less than 255 characters")
    private String userManualUrl;

    /**
     * Tags for categorization and search
     */
    private List<String> tags;

    /**
     * Additional metadata
     */
    private Map<String, Object> metadata;

    /**
     * Created by user ID
     */
    private UUID createdBy;

    // Validation method
    public boolean isValid() {
        return typeName != null && !typeName.trim().isEmpty() &&
                displayName != null && !displayName.trim().isEmpty() &&
                category != null && !category.trim().isEmpty();
    }

    // Helper methods for common device types
    public static DeviceTypeRequest createGpsTrackerType() {
        return DeviceTypeRequest.builder()
                .typeName("GPS_TRACKER")
                .displayName("GPS Tracker")
                .description("Standard GPS tracking device for vehicles")
                .category("HARDWARE")
                .supportedBrands(List.of("TELTONIKA", "QUECLINK", "CONCOX", "MEITRACK"))
                .supportedProtocols(List.of("TCP", "UDP", "HTTP"))
                .defaultProtocol("TCP")
                .defaultPort(8090)
                .supportsGps(true)
                .supportsSensors(true)
                .supportsCommands(true)
                .supportsOtaUpdates(true)
                .supportsGeofencing(true)
                .canAssignToVehicle(true)
                .canAssignToUser(false)
                .defaultUpdateInterval(60)
                .defaultMinDistance(100)
                .defaultMinAngle(30)
                .defaultTimeout(300)
                .compatibleSensorTypes(List.of("TEMPERATURE", "FUEL", "WEIGHT", "DOOR"))
                .requiredCapabilities(List.of("GPS", "CELLULAR"))
                .optionalCapabilities(List.of("ACCELEROMETER", "GYROSCOPE", "CAN_BUS"))
                .powerRequirements("12V DC")
                .temperatureRange("-25°C to +70°C")
                .ipRating("IP67")
                .connectivityOptions(List.of("2G", "3G", "4G"))
                .tags(List.of("gps", "tracker", "vehicle", "hardware"))
                .build();
    }

    public static DeviceTypeRequest createMobileDeviceType() {
        return DeviceTypeRequest.builder()
                .typeName("MOBILE_PHONE")
                .displayName("Mobile Phone")
                .description("Smartphone used for driver tracking")
                .category("MOBILE")
                .supportedBrands(List.of("MOBILE"))
                .supportedProtocols(List.of("HTTP", "WEBSOCKET"))
                .defaultProtocol("HTTP")
                .supportsGps(true)
                .supportsSensors(false)
                .supportsCommands(true)
                .supportsOtaUpdates(false)
                .supportsGeofencing(true)
                .canAssignToVehicle(false)
                .canAssignToUser(true)
                .defaultUpdateInterval(30)
                .defaultMinDistance(10)
                .defaultMinAngle(15)
                .defaultTimeout(180)
                .requiredCapabilities(List.of("GPS", "INTERNET"))
                .optionalCapabilities(List.of("CAMERA", "ACCELEROMETER", "PUSH_NOTIFICATIONS"))
                .powerRequirements("Battery")
                .connectivityOptions(List.of("WiFi", "4G", "5G"))
                .tags(List.of("mobile", "smartphone", "driver", "app"))
                .build();
    }

    public static DeviceTypeRequest createObdTrackerType() {
        return DeviceTypeRequest.builder()
                .typeName("OBD_TRACKER")
                .displayName("OBD-II Tracker")
                .description("OBD-II port connected tracking device")
                .category("HARDWARE")
                .supportedBrands(List.of("TELTONIKA", "QUECLINK", "MEITRACK"))
                .supportedProtocols(List.of("TCP", "HTTP"))
                .defaultProtocol("TCP")
                .defaultPort(8091)
                .supportsGps(true)
                .supportsSensors(true)
                .supportsCommands(true)
                .supportsOtaUpdates(true)
                .supportsGeofencing(true)
                .canAssignToVehicle(true)
                .canAssignToUser(false)
                .defaultUpdateInterval(30)
                .defaultMinDistance(50)
                .defaultMinAngle(20)
                .defaultTimeout(240)
                .compatibleSensorTypes(List.of("ENGINE_HOURS", "RPM", "FUEL", "TEMPERATURE"))
                .requiredCapabilities(List.of("GPS", "CELLULAR", "OBD_II", "CAN_BUS"))
                .optionalCapabilities(List.of("BLUETOOTH", "ACCELEROMETER"))
                .powerRequirements("12V DC (OBD-II Port)")
                .temperatureRange("-25°C to +70°C")
                .ipRating("IP54")
                .connectivityOptions(List.of("2G", "3G", "4G"))
                .installationNotes("Plug into vehicle OBD-II port. Ensure secure connection.")
                .tags(List.of("obd", "tracker", "vehicle", "diagnostic"))
                .build();
    }

    public static DeviceTypeRequest createAssetTrackerType() {
        return DeviceTypeRequest.builder()
                .typeName("ASSET_TRACKER")
                .displayName("Asset Tracker")
                .description("Tracking device for non-vehicle assets")
                .category("HARDWARE")
                .supportedBrands(List.of("TELTONIKA", "QUECLINK", "CONCOX"))
                .supportedProtocols(List.of("TCP", "HTTP"))
                .defaultProtocol("TCP")
                .defaultPort(8092)
                .supportsGps(true)
                .supportsSensors(true)
                .supportsCommands(true)
                .supportsOtaUpdates(false)
                .supportsGeofencing(true)
                .canAssignToVehicle(false)
                .canAssignToUser(false)
                .defaultUpdateInterval(120)
                .defaultMinDistance(200)
                .defaultMinAngle(45)
                .defaultTimeout(600)
                .compatibleSensorTypes(List.of("TEMPERATURE", "HUMIDITY", "DOOR", "WEIGHT"))
                .requiredCapabilities(List.of("GPS", "CELLULAR"))
                .optionalCapabilities(List.of("ACCELEROMETER", "MAGNETIC_SENSOR"))
                .powerRequirements("Battery (5000mAh)")
                .temperatureRange("-10°C to +60°C")
                .ipRating("IP68")
                .connectivityOptions(List.of("2G", "3G", "NB-IoT"))
                .installationNotes("Mount securely on asset. Ensure antenna clearance.")
                .tags(List.of("asset", "tracker", "portable", "battery"))
                .build();
    }
}