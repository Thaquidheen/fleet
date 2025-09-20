package com.fleetmanagement.bridgeservice.controller;

import com.fleetmanagement.bridgeservice.service.TraccarApiClient;
import com.fleetmanagement.bridgeservice.service.TraccarBridgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
@Slf4j
public class HealthController {

    private final TraccarApiClient traccarApiClient;
    private final TraccarBridgeService bridgeService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public HealthController(TraccarApiClient traccarApiClient,
                            TraccarBridgeService bridgeService,
                            RedisTemplate<String, Object> redisTemplate) {
        this.traccarApiClient = traccarApiClient;
        this.bridgeService = bridgeService;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/traccar")
    public ResponseEntity<Map<String, Object>> checkTraccarHealth() {
        Map<String, Object> health = new HashMap<>();

        try {
            boolean isHealthy = traccarApiClient.isServerHealthy();
            health.put("status", isHealthy ? "UP" : "DOWN");
            health.put("timestamp", Instant.now());
            health.put("service", "traccar");

            if (isHealthy) {
                health.put("message", "Traccar server is responding");
                return ResponseEntity.ok(health);
            } else {
                health.put("message", "Traccar server is not responding");
                return ResponseEntity.status(503).body(health);
            }

        } catch (Exception e) {
            log.error("Error checking Traccar health", e);
            health.put("status", "DOWN");
            health.put("timestamp", Instant.now());
            health.put("service", "traccar");
            health.put("error", e.getMessage());
            return ResponseEntity.status(503).body(health);
        }
    }

    @GetMapping("/redis")
    public ResponseEntity<Map<String, Object>> checkRedisHealth() {
        Map<String, Object> health = new HashMap<>();

        try {
            // Test Redis connection
            String testKey = "health:test:" + System.currentTimeMillis();
            redisTemplate.opsForValue().set(testKey, "test");
            String result = (String) redisTemplate.opsForValue().get(testKey);
            redisTemplate.delete(testKey);

            boolean isHealthy = "test".equals(result);
            health.put("status", isHealthy ? "UP" : "DOWN");
            health.put("timestamp", Instant.now());
            health.put("service", "redis");
            health.put("message", isHealthy ? "Redis is responding" : "Redis test failed");

            return isHealthy ? ResponseEntity.ok(health) : ResponseEntity.status(503).body(health);

        } catch (Exception e) {
            log.error("Error checking Redis health", e);
            health.put("status", "DOWN");
            health.put("timestamp", Instant.now());
            health.put("service", "redis");
            health.put("error", e.getMessage());
            return ResponseEntity.status(503).body(health);
        }
    }

    @GetMapping("/sync")
    public ResponseEntity<Map<String, Object>> checkSyncHealth() {
        Map<String, Object> health = new HashMap<>();

        try {
            TraccarBridgeService.SyncStatistics stats = bridgeService.getSyncStatistics();

            boolean isHealthy = stats.getLastSyncTime() != null &&
                    Instant.now().minusSeconds(300).isBefore(stats.getLastSyncTime());

            health.put("status", isHealthy ? "UP" : "DOWN");
            health.put("timestamp", Instant.now());
            health.put("service", "sync");
            health.put("lastSync", stats.getLastSyncTime());
            health.put("successCount", stats.getSyncSuccessCount());
            health.put("errorCount", stats.getSyncErrorCount());
            health.put("avgDuration", stats.getAverageSyncDuration());

            return isHealthy ? ResponseEntity.ok(health) : ResponseEntity.status(503).body(health);

        } catch (Exception e) {
            log.error("Error checking sync health", e);
            health.put("status", "DOWN");
            health.put("timestamp", Instant.now());
            health.put("service", "sync");
            health.put("error", e.getMessage());
            return ResponseEntity.status(503).body(health);
        }
    }
}