package com.fleetmanagement.bridgeservice.service;

import com.fleetmanagement.bridgeservice.model.traccar.TraccarPosition;
import com.fleetmanagement.bridgeservice.utils.ErrorAnalyzer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ErrorRecoveryService {

    private final ErrorAnalyzer errorAnalyzer;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public ErrorRecoveryService(ErrorAnalyzer errorAnalyzer,
                                RedisTemplate<String, Object> redisTemplate) {
        this.errorAnalyzer = errorAnalyzer;
        this.redisTemplate = redisTemplate;
    }

    public void handleSyncError(String syncType, Exception error) {
        try {
            log.error("Sync error for type: {}", syncType, error);

            // Record error
            errorAnalyzer.recordError(syncType, error);

            // Store in Redis for monitoring
            String errorKey = "error:" + syncType + ":" + System.currentTimeMillis();
            redisTemplate.opsForValue().set(errorKey, error.getMessage(), 24, TimeUnit.HOURS);

            // Check if we need to alert
            if (shouldAlert(syncType)) {
                sendAlert(syncType, error);
            }

        } catch (Exception e) {
            log.error("Error handling sync error", e);
        }
    }

    public void handlePositionError(TraccarPosition position, Exception error) {
        try {
            log.error("Position processing error for device: {}", position.getDeviceId(), error);

            // Record error
            errorAnalyzer.recordError("position", error);

            // Store failed position for retry
            String retryKey = "retry:position:" + position.getDeviceId() + ":" + System.currentTimeMillis();
            redisTemplate.opsForValue().set(retryKey, position, 1, TimeUnit.HOURS);

        } catch (Exception e) {
            log.error("Error handling position error", e);
        }
    }

    public void handleBatchError(List<TraccarPosition> batch, Exception error) {
        try {
            log.error("Batch processing error for {} positions", batch.size(), error);

            // Record error
            errorAnalyzer.recordError("batch", error);

            // Store failed batch for retry
            String batchKey = "retry:batch:" + System.currentTimeMillis();
            redisTemplate.opsForValue().set(batchKey, batch, 1, TimeUnit.HOURS);

        } catch (Exception e) {
            log.error("Error handling batch error", e);
        }
    }

    public void cleanupOldErrors() {
        try {
            // Clean up old error records (older than 7 days)
            String pattern = "error:*";
            redisTemplate.delete(redisTemplate.keys(pattern));

            // Reset error analyzer
            errorAnalyzer.resetErrors();

            log.info("Old error records cleaned up");

        } catch (Exception e) {
            log.error("Error cleaning up old errors", e);
        }
    }

    private boolean shouldAlert(String syncType) {
        // Check if there have been too many recent errors
        return errorAnalyzer.hasRecentErrors(Duration.ofMinutes(5)) &&
                errorAnalyzer.getErrorCount(syncType) > 10;
    }

    private void sendAlert(String syncType, Exception error) {
        try {
            // In a real implementation, this would send notifications
            // via email, Slack, PagerDuty, etc.
            log.warn("ALERT: Multiple {} sync errors detected. Last error: {}",
                    syncType, error.getMessage());

            // Store alert record
            String alertKey = "alert:" + syncType + ":" + System.currentTimeMillis();
            redisTemplate.opsForValue().set(alertKey, error.getMessage(), 24, TimeUnit.HOURS);

        } catch (Exception e) {
            log.error("Error sending alert", e);
        }
    }
}
