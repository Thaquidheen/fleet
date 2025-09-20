package com.fleetmanagement.bridgeservice.utils;

import com.fleetmanagement.bridgeservice.model.domain.*;
import com.fleetmanagement.bridgeservice.model.events.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class EventBuilder {

    public LocationUpdatedEvent buildLocationEvent(LocationData locationData, String deviceName) {
        return LocationUpdatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("device.location.updated")
                .timestamp(Instant.now())
                .deviceId(locationData.getDeviceId())
                .traccarDeviceId(locationData.getTraccarDeviceId())
                .companyId(locationData.getCompanyId())
                .deviceName(deviceName)
                .locationData(locationData)
                .source("bridge-service")
                .version("1.0")
                .build();
    }

    public SensorReadingEvent buildSensorEvent(SensorData sensorData, String deviceName) {
        return SensorReadingEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("device.sensor.reading")
                .timestamp(Instant.now())
                .deviceId(sensorData.getDeviceId())
                .traccarDeviceId(sensorData.getTraccarDeviceId())
                .companyId(sensorData.getCompanyId())
                .deviceName(deviceName)
                .sensorData(sensorData)
                .source("bridge-service")
                .version("1.0")
                .build();
    }

    public DeviceHeartbeatEvent buildHeartbeatEvent(DeviceHealth deviceHealth, String deviceName) {
        return DeviceHeartbeatEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("device.heartbeat")
                .timestamp(Instant.now())
                .deviceId(deviceHealth.getDeviceId())
                .traccarDeviceId(deviceHealth.getTraccarDeviceId())
                .companyId(deviceHealth.getCompanyId())
                .deviceName(deviceName)
                .deviceHealth(deviceHealth)
                .source("bridge-service")
                .version("1.0")
                .build();
    }
}
