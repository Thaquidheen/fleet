// CacheManagementService.java
package com.fleetmanagement.bridgeservice.service;

import com.fleetmanagement.bridgeservice.model.traccar.TraccarDevice;
import com.fleetmanagement.bridgeservice.model.traccar.TraccarPosition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CacheManagementService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public CacheManagementService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void updateLastSyncTime(String syncType) {
        String key = "sync:last:" + syncType;
        redisTemplate.opsForValue().set(key, Instant.now().toString(), 24, TimeUnit.HOURS);
    }

    public Instant getLastSyncTime(String syncType) {
        String key = "sync:last:" + syncType;
        String value = (String) redisTemplate.opsForValue().get(key);

        if (value != null) {
            try {
                return Instant.parse(value);
            } catch (Exception e) {
                log.warn("Invalid timestamp in cache for {}: {}", syncType, value);
            }
        }

        // Default to 5 minutes ago for first sync
        return Instant.now().minusSeconds(300);
    }

    public boolean isDuplicatePosition(TraccarPosition position) {
        String key = "position:last:" + position.getDeviceId();
        String lastPositionHash = generatePositionHash(position);

        String cachedHash = (String) redisTemplate.opsForValue().get(key);

        if (lastPositionHash.equals(cachedHash)) {
            return true;
        }

        // Cache new position hash
        redisTemplate.opsForValue().set(key, lastPositionHash, 30, TimeUnit.SECONDS);
        return false;
    }

    public void cacheProcessedPosition(TraccarPosition position) {
        String key = "processed:position:" + position.getDeviceId();
        redisTemplate.opsForValue().set(key, position, 1, TimeUnit.MINUTES);
    }

    public TraccarDevice getCachedDevice(Long deviceId) {
        String key = "device:info:" + deviceId;
        return (TraccarDevice) redisTemplate.opsForValue().get(key);
    }

    public void cacheDeviceHealth(TraccarDevice device) {
        String key = "device:health:" + device.getId();
        redisTemplate.opsForValue().set(key, device, 2, TimeUnit.MINUTES);
    }

    public void cleanupExpiredEntries() {
        try {
            // Clean up old cache entries
            Set<String> expiredKeys = redisTemplate.keys("*:expired:*");
            if (!expiredKeys.isEmpty()) {
                redisTemplate.delete(expiredKeys);
                log.info("Cleaned up {} expired cache entries", expiredKeys.size());
            }

        } catch (Exception e) {
            log.error("Error cleaning up expired entries", e);
        }
    }

    private String generatePositionHash(TraccarPosition position) {
        // Generate hash based on position data to detect duplicates
        return String.format("%d_%f_%f_%s",
                position.getDeviceId(),
                position.getLatitude(),
                position.getLongitude(),
                position.getDeviceTime());
    }
}