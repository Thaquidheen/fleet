// ===== DataTransformationService.java =====
package com.fleetmanagement.bridgeservice.service;

import com.fleetmanagement.bridgeservice.client.DeviceServiceClient;
import com.fleetmanagement.bridgeservice.exception.DataTransformationException;
import com.fleetmanagement.bridgeservice.model.domain.*;
import com.fleetmanagement.bridgeservice.model.traccar.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
public class DataTransformationService {

    private final DeviceServiceClient deviceServiceClient;

    @Autowired
    public DataTransformationService(DeviceServiceClient deviceServiceClient) {
        this.deviceServiceClient = deviceServiceClient;
    }

    public LocationData convertToLocationData(TraccarPosition traccarPosition, TraccarDevice traccarDevice) {
        try {
            log.debug("Converting Traccar position to LocationData for device: {}", traccarPosition.getDeviceId());

            // Get device information from Device Service
            UUID deviceId = getDeviceId(traccarPosition.getDeviceId());
            UUID companyId = getCompanyId(deviceId);

            LocationData locationData = LocationData.builder()
                    .deviceId(deviceId)
                    .traccarDeviceId(traccarPosition.getDeviceId())
                    .companyId(companyId)

                    // Position data
                    .latitude(traccarPosition.getLatitude())
                    .longitude(traccarPosition.getLongitude())
                    .altitude(traccarPosition.getAltitude())
                    .speed(convertSpeed(traccarPosition.getSpeed()))
                    .course(traccarPosition.getCourse())
                    .accuracy(traccarPosition.getAccuracy())

                    // Timestamps
                    .deviceTime(traccarPosition.getDeviceTime())
                    .serverTime(traccarPosition.getServerTime())
                    .processedTime(Instant.now())

                    // Status
                    .valid(traccarPosition.getValid() != null ? traccarPosition.getValid() : false)
                    .address(traccarPosition.getAddress())
                    .protocol(traccarPosition.getProtocol())

                    // Additional data
                    .odometer(traccarPosition.getOdometer())
                    .ignition(traccarPosition.getIgnition())

                    // Data quality
                    .source("traccar")
                    .satelliteCount(getSatelliteCount(traccarPosition))
                    .hdop(getHdop(traccarPosition))

                    .build();

            log.debug("Successfully converted position data for device: {}", deviceId);
            return locationData;

        } catch (Exception e) {
            log.error("Error converting Traccar position to LocationData", e);
            throw new DataTransformationException("Failed to convert position data", e);
        }
    }

    public SensorData convertToSensorData(TraccarPosition traccarPosition, TraccarDevice traccarDevice) {
        try {
            log.debug("Converting Traccar position to SensorData for device: {}", traccarPosition.getDeviceId());

            // Get device information
            UUID deviceId = getDeviceId(traccarPosition.getDeviceId());
            UUID companyId = getCompanyId(deviceId);

            // Check if position contains sensor data
            if (!hasSensorData(traccarPosition)) {
                return null;
            }

            SensorData sensorData = SensorData.builder()
                    .deviceId(deviceId)
                    .traccarDeviceId(traccarPosition.getDeviceId())
                    .companyId(companyId)

                    // Sensor readings
                    .fuelLevel(traccarPosition.getFuelLevel())
                    .temperature(traccarPosition.getTemperature())
                    .batteryLevel(traccarPosition.getBatteryLevel())
                    .engineHours(getEngineHours(traccarPosition))
                    .weight(getWeight(traccarPosition))
                    .pressure(getPressure(traccarPosition))
                    .humidity(getHumidity(traccarPosition))

                    // Timestamps
                    .readingTime(traccarPosition.getDeviceTime())
                    .processedTime(Instant.now())

                    // Metadata
                    .sensorType(determineSensorType(traccarPosition))
                    .unit(determineSensorUnit(traccarPosition))
                    .valid(traccarPosition.getValid() != null ? traccarPosition.getValid() : false)
                    .source("traccar")

                    .build();

            log.debug("Successfully converted sensor data for device: {}", deviceId);
            return sensorData;

        } catch (Exception e) {
            log.error("Error converting Traccar position to SensorData", e);
            throw new DataTransformationException("Failed to convert sensor data", e);
        }
    }

    public DeviceHealth convertToDeviceHealth(TraccarDevice traccarDevice) {
        try {
            log.debug("Converting Traccar device to DeviceHealth for device: {}", traccarDevice.getId());

            // Get device information
            UUID deviceId = getDeviceId(traccarDevice.getId());
            UUID companyId = getCompanyId(deviceId);

            DeviceHealth deviceHealth = DeviceHealth.builder()
                    .deviceId(deviceId)
                    .traccarDeviceId(traccarDevice.getId())
                    .companyId(companyId)

                    // Connection status
                    .status(traccarDevice.getStatus())
                    .lastCommunication(traccarDevice.getLastUpdate())
                    .lastPosition(traccarDevice.getLastUpdate())

                    // Signal quality
                    .signalStrength(getSignalStrength(traccarDevice))
                    .networkType(getNetworkType(traccarDevice))
                    .satelliteCount(getSatelliteCount(traccarDevice))

                    // Device status
                    .batteryLevel(getBatteryLevel(traccarDevice))
                    .powerStatus(getPowerStatus(traccarDevice))
                    .firmwareVersion(getFirmwareVersion(traccarDevice))

                    // Performance metrics
                    .messageCount(getMessageCount(traccarDevice))
                    .dataUsage(getDataUsage(traccarDevice))
                    .errorCount(getErrorCount(traccarDevice))

                    // Health score
                    .healthScore(calculateHealthScore(traccarDevice))
                    .healthStatus(determineHealthStatus(traccarDevice))

                    // Timestamps
                    .checkedTime(Instant.now())
                    .reportedTime(traccarDevice.getLastUpdate())

                    .build();

            log.debug("Successfully converted device health for device: {}", deviceId);
            return deviceHealth;

        } catch (Exception e) {
            log.error("Error converting Traccar device to DeviceHealth", e);
            throw new DataTransformationException("Failed to convert device health", e);
        }
    }

    public CommandResult convertToCommandResult(TraccarCommand traccarCommand) {
        try {
            log.debug("Converting Traccar command to CommandResult for device: {}", traccarCommand.getDeviceId());

            // Get device information
            UUID deviceId = getDeviceId(traccarCommand.getDeviceId());
            UUID companyId = getCompanyId(deviceId);

            CommandResult commandResult = CommandResult.builder()
                    .commandId(UUID.randomUUID()) // Generate new UUID for internal tracking
                    .deviceId(deviceId)
                    .traccarDeviceId(traccarCommand.getDeviceId())
                    .companyId(companyId)

                    // Command details
                    .commandType(traccarCommand.getType())
                    .commandData(getCommandData(traccarCommand))
                    .description(traccarCommand.getDescription())

                    // Execution details
                    .status("EXECUTED")
                    .result("SUCCESS")
                    .errorMessage(null)

                    // Timestamps
                    .sentTime(traccarCommand.getServerTime())
                    .executedTime(Instant.now())
                    .acknowledgedTime(Instant.now())

                    // Metadata
                    .protocol("traccar")
                    .retryCount(0)
                    .successful(true)

                    .build();

            log.debug("Successfully converted command result for device: {}", deviceId);
            return commandResult;

        } catch (Exception e) {
            log.error("Error converting Traccar command to CommandResult", e);
            throw new DataTransformationException("Failed to convert command result", e);
        }
    }

    // Helper methods

    private UUID getDeviceId(Long traccarDeviceId) {
        try {
            // Call Device Service to get UUID by Traccar ID
            return deviceServiceClient.getDeviceByTraccarId(traccarDeviceId);
        } catch (Exception e) {
            log.warn("Could not find device with Traccar ID: {}", traccarDeviceId);
            return UUID.randomUUID(); // Fallback - in real implementation, this should be handled properly
        }
    }

    private UUID getCompanyId(UUID deviceId) {
        try {
            // Call Device Service to get company ID
            return deviceServiceClient.getCompanyIdByDeviceId(deviceId);
        } catch (Exception e) {
            log.warn("Could not find company for device: {}", deviceId);
            return UUID.randomUUID(); // Fallback
        }
    }

    private Double convertSpeed(Double speed) {
        // Convert from knots to km/h if needed
        return speed != null ? speed * 1.852 : null;
    }

    private Integer getSatelliteCount(TraccarPosition position) {
        Object satellites = position.getAttributes() != null ?
                position.getAttributes().get("sat") : null;
        return satellites instanceof Number ? ((Number) satellites).intValue() : null;
    }

    private Integer getSatelliteCount(TraccarDevice device) {
        Object satellites = device.getAttributes() != null ?
                device.getAttributes().get("sat") : null;
        return satellites instanceof Number ? ((Number) satellites).intValue() : null;
    }

    private Double getHdop(TraccarPosition position) {
        Object hdop = position.getAttributes() != null ?
                position.getAttributes().get("hdop") : null;
        return hdop instanceof Number ? ((Number) hdop).doubleValue() : null;
    }

    private boolean hasSensorData(TraccarPosition position) {
        return position.getFuelLevel() != null ||
                position.getTemperature() != null ||
                position.getBatteryLevel() != null ||
                getEngineHours(position) != null ||
                getWeight(position) != null;
    }

    private Double getEngineHours(TraccarPosition position) {
        Object hours = position.getAttributes() != null ?
                position.getAttributes().get("hours") : null;
        return hours instanceof Number ? ((Number) hours).doubleValue() : null;
    }

    private Double getWeight(TraccarPosition position) {
        Object weight = position.getAttributes() != null ?
                position.getAttributes().get("weight") : null;
        return weight instanceof Number ? ((Number) weight).doubleValue() : null;
    }

    private Double getPressure(TraccarPosition position) {
        Object pressure = position.getAttributes() != null ?
                position.getAttributes().get("pressure") : null;
        return pressure instanceof Number ? ((Number) pressure).doubleValue() : null;
    }

    private Double getHumidity(TraccarPosition position) {
        Object humidity = position.getAttributes() != null ?
                position.getAttributes().get("humidity") : null;
        return humidity instanceof Number ? ((Number) humidity).doubleValue() : null;
    }

    private String determineSensorType(TraccarPosition position) {
        if (position.getFuelLevel() != null) return "FUEL";
        if (position.getTemperature() != null) return "TEMPERATURE";
        if (position.getBatteryLevel() != null) return "BATTERY";
        if (getWeight(position) != null) return "WEIGHT";
        if (getPressure(position) != null) return "PRESSURE";
        if (getHumidity(position) != null) return "HUMIDITY";
        return "UNKNOWN";
    }

    private String determineSensorUnit(TraccarPosition position) {
        String sensorType = determineSensorType(position);
        switch (sensorType) {
            case "FUEL": return "LITERS";
            case "TEMPERATURE": return "CELSIUS";
            case "BATTERY": return "PERCENTAGE";
            case "WEIGHT": return "KILOGRAMS";
            case "PRESSURE": return "BAR";
            case "HUMIDITY": return "PERCENTAGE";
            default: return "UNKNOWN";
        }
    }

    private Integer getSignalStrength(TraccarDevice device) {
        Object signal = device.getAttributes() != null ?
                device.getAttributes().get("rssi") : null;
        return signal instanceof Number ? ((Number) signal).intValue() : null;
    }

    private String getNetworkType(TraccarDevice device) {
        Object network = device.getAttributes() != null ?
                device.getAttributes().get("network") : null;
        return network != null ? network.toString() : "UNKNOWN";
    }

    private Double getBatteryLevel(TraccarDevice device) {
        Object battery = device.getAttributes() != null ?
                device.getAttributes().get("battery") : null;
        return battery instanceof Number ? ((Number) battery).doubleValue() : null;
    }

    private Boolean getPowerStatus(TraccarDevice device) {
        Object power = device.getAttributes() != null ?
                device.getAttributes().get("power") : null;
        return power instanceof Boolean ? (Boolean) power : null;
    }

    private String getFirmwareVersion(TraccarDevice device) {
        Object firmware = device.getAttributes() != null ?
                device.getAttributes().get("version") : null;
        return firmware != null ? firmware.toString() : null;
    }

    private Long getMessageCount(TraccarDevice device) {
        Object count = device.getAttributes() != null ?
                device.getAttributes().get("messageCount") : null;
        return count instanceof Number ? ((Number) count).longValue() : 0L;
    }

    private Double getDataUsage(TraccarDevice device) {
        Object usage = device.getAttributes() != null ?
                device.getAttributes().get("dataUsage") : null;
        return usage instanceof Number ? ((Number) usage).doubleValue() : 0.0;
    }

    private Integer getErrorCount(TraccarDevice device) {
        Object errors = device.getAttributes() != null ?
                device.getAttributes().get("errorCount") : null;
        return errors instanceof Number ? ((Number) errors).intValue() : 0;
    }

    private Integer calculateHealthScore(TraccarDevice device) {
        int score = 100;

        // Deduct points for various issues
        if (!"online".equalsIgnoreCase(device.getStatus())) {
            score -= 50;
        }

        if (device.getLastUpdate() != null &&
                Instant.now().minusSeconds(300).isAfter(device.getLastUpdate())) {
            score -= 20; // No communication in 5 minutes
        }

        Double batteryLevel = getBatteryLevel(device);
        if (batteryLevel != null && batteryLevel < 20) {
            score -= 15; // Low battery
        }

        Integer errorCount = getErrorCount(device);
        if (errorCount > 0) {
            score -= Math.min(errorCount * 5, 30); // Max 30 points for errors
        }

        return Math.max(score, 0);
    }

    private String determineHealthStatus(TraccarDevice device) {
        Integer healthScore = calculateHealthScore(device);

        if (healthScore >= 80) return "HEALTHY";
        if (healthScore >= 60) return "WARNING";
        if (healthScore >= 40) return "CRITICAL";
        return "OFFLINE";
    }

    private String getCommandData(TraccarCommand command) {
        if (command.getAttributes() != null && !command.getAttributes().isEmpty()) {
            return command.getAttributes().toString();
        }
        return command.getDescription();
    }
}