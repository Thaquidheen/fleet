package com.fleetmanagement.bridgeservice.service;

ackage com.fleetmanagement.bridgeservice.service;

import com.fleetmanagement.bridgeservice.client.DeviceServiceClient;
import com.fleetmanagement.bridgeservice.exception.DeviceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DeviceValidationService {

    private final DeviceServiceClient deviceServiceClient;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public DeviceValidationService(DeviceServiceClient deviceServiceClient,
                                   RedisTemplate<String, Object> redisTemplate) {
        this.deviceServiceClient = deviceServiceClient;
        this.redisTemplate = redisTemplate;
    }

    public boolean isDeviceActive(Long traccarDeviceId) {
        try {
            // Check cache first
            String cacheKey = "device:active:" + traccarDeviceId;
            Boolean cached = (Boolean) redisTemplate.opsForValue().get(cacheKey);

            if (cached != null) {
                return cached;
            }

            // Get device UUID and check if active
            UUID deviceId = deviceServiceClient.getDeviceByTraccarId(traccarDeviceId);
            boolean isActive = deviceServiceClient.isDeviceActive(deviceId);

            // Cache result for 5 minutes
            redisTemplate.opsForValue().set(cacheKey, isActive, 5, TimeUnit.MINUTES);

            return isActive;

        } catch (Exception e) {
            log.warn("Could not validate device {}: {}", traccarDeviceId, e.getMessage());
            return false; // Default to inactive if validation fails
        }
    }

    public UUID getDeviceId(Long traccarDeviceId) {
        try {
            String cacheKey = "device:uuid:" + traccarDeviceId;
            String cached = (String) redisTemplate.opsForValue().get(cacheKey);

            if (cached != null) {
                return UUID.fromString(cached);
            }

            UUID deviceId = deviceServiceClient.getDeviceByTraccarId(traccarDeviceId);

            // Cache for 1 hour
            redisTemplate.opsForValue().set(cacheKey, deviceId.toString(), 1, TimeUnit.HOURS);

            return deviceId;

        } catch (Exception e) {
            log.error("Device not found for Traccar ID: {}", traccarDeviceId);
            throw new DeviceNotFoundException("Device not found for Traccar ID: " + traccarDeviceId);
        }
    }

    public String getDeviceName(Long traccarDeviceId) {
        try {
            String cacheKey = "device:name:" + traccarDeviceId;
            String cached = (String) redisTemplate.opsForValue().get(cacheKey);

            if (cached != null) {
                return cached;
            }

            String deviceName = deviceServiceClient.getDeviceNameByTraccarId(traccarDeviceId);

            // Cache for 30 minutes
            redisTemplate.opsForValue().set(cacheKey, deviceName, 30, TimeUnit.MINUTES);

            return deviceName;

        } catch (Exception e) {
            log.warn("Could not get device name for Traccar ID: {}", traccarDeviceId);
            return "Unknown Device";
        }
    }
}