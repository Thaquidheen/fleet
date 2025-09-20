// ===== TraccarBridgeService.java =====
package com.fleetmanagement.bridgeservice.service;

import com.fleetmanagement.bridgeservice.exception.SyncException;
import com.fleetmanagement.bridgeservice.model.domain.*;
import com.fleetmanagement.bridgeservice.model.events.*;
import com.fleetmanagement.bridgeservice.model.traccar.*;
import com.fleetmanagement.bridgeservice.utils.BatchProcessor;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TraccarBridgeService {

    private final TraccarApiClient traccarApiClient;
    private final DataTransformationService dataTransformationService;
    private final EventPublishingService eventPublishingService;
    private final DeviceValidationService deviceValidationService;
    private final ErrorRecoveryService errorRecoveryService;
    private final CacheManagementService cacheManagementService;
    private final BatchProcessor batchProcessor;
    private final RedisTemplate<String, Object> redisTemplate;

    // Metrics
    private final Counter syncSuccessCounter;
    private final Counter syncErrorCounter;
    private final Timer syncDurationTimer;
    private final Counter positionsProcessedCounter;
    private final Counter eventsProcessedCounter;

    @Autowired
    public TraccarBridgeService(TraccarApiClient traccarApiClient,
                                DataTransformationService dataTransformationService,
                                EventPublishingService eventPublishingService,
                                DeviceValidationService deviceValidationService,
                                ErrorRecoveryService errorRecoveryService,
                                CacheManagementService cacheManagementService,
                                BatchProcessor batchProcessor,
                                RedisTemplate<String, Object> redisTemplate,
                                MeterRegistry meterRegistry) {
        this.traccarApiClient = traccarApiClient;
        this.dataTransformationService = dataTransformationService;
        this.eventPublishingService = eventPublishingService;
        this.deviceValidationService = deviceValidationService;
        this.errorRecoveryService = errorRecoveryService;
        this.cacheManagementService = cacheManagementService;
        this.batchProcessor = batchProcessor;
        this.redisTemplate = redisTemplate;

        // Initialize metrics
        this.syncSuccessCounter = Counter.builder("bridge.sync.success")
                .description("Number of successful sync operations")
                .register(meterRegistry);
        this.syncErrorCounter = Counter.builder("bridge.sync.error")
                .description("Number of failed sync operations")
                .register(meterRegistry);
        this.syncDurationTimer = Timer.builder("bridge.sync.duration")
                .description("Duration of sync operations")
                .register(meterRegistry);
        this.positionsProcessedCounter = Counter.builder("bridge.positions.processed")
                .description("Number of positions processed")
                .register(meterRegistry);
        this.eventsProcessedCounter = Counter.builder("bridge.events.processed")
                .description("Number of events processed")
                .register(meterRegistry);
    }

    /**
     * Main position synchronization job - runs every 5 seconds
     */
    @Scheduled(fixedRate = 5000, initialDelay = 10000)
    public void syncPositionData() {
        Timer.Sample sample = Timer.start();

        try {
            log.debug("Starting position data synchronization");

            // Get latest positions from Traccar
            List<TraccarPosition> positions = traccarApiClient.getLatestPositions();

            if (positions.isEmpty()) {
                log.debug("No new positions to process");
                return;
            }

            log.info("Processing {} positions from Traccar", positions.size());

            // Process positions in parallel batches
            List<List<TraccarPosition>> batches = batchProcessor.createBatches(positions, 50);

            batches.parallelStream().forEach(batch -> {
                try {
                    processBatch(batch);
                } catch (Exception e) {
                    log.error("Error processing position batch", e);
                    errorRecoveryService.handleBatchError(batch, e);
                }
            });

            // Update metrics and cache
            updateSyncMetrics(positions.size());
            cacheManagementService.updateLastSyncTime("positions");

            syncSuccessCounter.increment();
            log.info("Successfully processed {} positions", positions.size());

        } catch (Exception e) {
            log.error("Error during position synchronization", e);
            syncErrorCounter.increment();
            errorRecoveryService.handleSyncError("positions", e);
            throw new SyncException("Position sync failed", e);
        } finally {
            sample.stop(syncDurationTimer);
        }
    }

    /**
     * Device health monitoring job - runs every 30 seconds
     */
    @Scheduled(fixedRate = 30000, initialDelay = 15000)
    public void syncDeviceHealth() {
        try {
            log.debug("Starting device health synchronization");

            // Get all devices from Traccar
            List<TraccarDevice> devices = traccarApiClient.getAllDevices();

            if (devices.isEmpty()) {
                log.debug("No devices to check");
                return;
            }

            log.info("Checking health for {} devices", devices.size());

            // Process device health
            for (TraccarDevice device : devices) {
                try {
                    processDeviceHealth(device);
                } catch (Exception e) {
                    log.error("Error processing health for device: {}", device.getId(), e);
                }
            }

            cacheManagementService.updateLastSyncTime("health");
            log.info("Successfully processed health for {} devices", devices.size());

        } catch (Exception e) {
            log.error("Error during device health synchronization", e);
            errorRecoveryService.handleSyncError("health", e);
        }
    }

    /**
     * Event synchronization job - runs every 10 seconds
     */
    @Scheduled(fixedRate = 10000, initialDelay = 20000)
    public void syncEvents() {
        try {
            log.debug("Starting event synchronization");

            // Get last sync time
            Instant lastSync = cacheManagementService.getLastSyncTime("events");
            Instant now = Instant.now();

            // Get events from Traccar
            List<TraccarEvent> events = traccarApiClient.getEvents(lastSync, now);

            if (events.isEmpty()) {
                log.debug("No new events to process");
                return;
            }

            log.info("Processing {} events from Traccar", events.size());

            // Process events
            for (TraccarEvent event : events) {
                try {
                    processEvent(event);
                    eventsProcessedCounter.increment();
                } catch (Exception e) {
                    log.error("Error processing event: {}", event.getId(), e);
                }
            }

            cacheManagementService.updateLastSyncTime("events");
            log.info("Successfully processed {} events", events.size());

        } catch (Exception e) {
            log.error("Error during event synchronization", e);
            errorRecoveryService.handleSyncError("events", e);
        }
    }

    /**
     * Cleanup job - runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void performDailyCleanup() {
        try {
            log.info("Starting daily cleanup");

            // Clean old cache entries
            cacheManagementService.cleanupExpiredEntries();

            // Clean old error logs
            errorRecoveryService.cleanupOldErrors();

            // Reset daily metrics
            resetDailyMetrics();

            log.info("Daily cleanup completed");

        } catch (Exception e) {
            log.error("Error during daily cleanup", e);
        }
    }

    // Processing methods

    @Async("bridgeAsyncExecutor")
    private void processBatch(List<TraccarPosition> positions) {
        for (TraccarPosition position : positions) {
            try {
                processPosition(position);
                positionsProcessedCounter.increment();
            } catch (Exception e) {
                log.error("Error processing position for device: {}", position.getDeviceId(), e);
                errorRecoveryService.handlePositionError(position, e);
            }
        }
    }

    private void processPosition(TraccarPosition position) {
        try {
            // Validate device exists and is active
            if (!deviceValidationService.isDeviceActive(position.getDeviceId())) {
                log.debug("Skipping position for inactive device: {}", position.getDeviceId());
                return;
            }

            // Check if position is duplicate
            if (cacheManagementService.isDuplicatePosition(position)) {
                log.debug("Skipping duplicate position for device: {}", position.getDeviceId());
                return;
            }

            // Get device information
            TraccarDevice device = getDeviceInfo(position.getDeviceId());

            // Transform to domain model
            LocationData locationData = dataTransformationService.convertToLocationData(position, device);

            // Publish location event
            LocationUpdatedEvent locationEvent = LocationUpdatedEvent.from(locationData);
            eventPublishingService.publishLocationUpdate(locationEvent);

            // Process sensor data if available
            SensorData sensorData = dataTransformationService.convertToSensorData(position, device);
            if (sensorData != null) {
                SensorReadingEvent sensorEvent = SensorReadingEvent.from(sensorData);
                eventPublishingService.publishSensorReading(sensorEvent);
            }

            // Cache processed position
            cacheManagementService.cacheProcessedPosition(position);

            log.debug("Successfully processed position for device: {}", position.getDeviceId());

        } catch (Exception e) {
            log.error("Error processing position for device: {}", position.getDeviceId(), e);
            throw e;
        }
    }

    private void processDeviceHealth(TraccarDevice device) {
        try {
            // Validate device
            if (!deviceValidationService.isDeviceActive(device.getId())) {
                log.debug("Skipping health check for inactive device: {}", device.getId());
                return;
            }

            // Transform to domain model
            DeviceHealth deviceHealth = dataTransformationService.convertToDeviceHealth(device);

            // Publish heartbeat event
            DeviceHeartbeatEvent heartbeatEvent = DeviceHeartbeatEvent.from(deviceHealth);
            eventPublishingService.publishDeviceHeartbeat(heartbeatEvent);

            // Cache device health
            cacheManagementService.cacheDeviceHealth(device);

            log.debug("Successfully processed health for device: {}", device.getId());

        } catch (Exception e) {
            log.error("Error processing health for device: {}", device.getId(), e);
            throw e;
        }
    }

    private void processEvent(TraccarEvent event) {
        try {
            // Validate device
            if (!deviceValidationService.isDeviceActive(event.getDeviceId())) {
                log.debug("Skipping event for inactive device: {}", event.getDeviceId());
                return;
            }

            // Process different event types
            switch (event.getType().toLowerCase()) {
                case "geofence":
                    processGeofenceEvent(event);
                    break;
                case "overspeed":
                    processOverspeedEvent(event);
                    break;
                case "maintenance":
                    processMaintenanceEvent(event);
                    break;
                case "ignition":
                    processIgnitionEvent(event);
                    break;
                default:
                    log.debug("Unknown event type: {}", event.getType());
            }

            log.debug("Successfully processed event: {} for device: {}", event.getType(), event.getDeviceId());

        } catch (Exception e) {
            log.error("Error processing event: {} for device: {}", event.getType(), event.getDeviceId(), e);
            throw e;
        }
    }

    // Helper methods

    private TraccarDevice getDeviceInfo(Long deviceId) {
        // Try to get from cache first
        TraccarDevice cached = cacheManagementService.getCachedDevice(deviceId);
        if (cached != null) {
            return cached;
        }

        // Get from Traccar API
        List<TraccarDevice> devices = traccarApiClient.getAllDevices();
        return devices.stream()
                .filter(d -> d.getId().equals(deviceId))
                .findFirst()
                .orElse(null);
    }

    private void processGeofenceEvent(TraccarEvent event) {
        // Process geofence entry/exit events
        log.info("Processing geofence event for device: {}", event.getDeviceId());
        // Implementation for geofence events
    }

    private void processOverspeedEvent(TraccarEvent event) {
        // Process overspeed events
        log.info("Processing overspeed event for device: {}", event.getDeviceId());
        // Implementation for overspeed events
    }

    private void processMaintenanceEvent(TraccarEvent event) {
        // Process maintenance events
        log.info("Processing maintenance event for device: {}", event.getDeviceId());
        // Implementation for maintenance events
    }

    private void processIgnitionEvent(TraccarEvent event) {
        // Process ignition on/off events
        log.info("Processing ignition event for device: {}", event.getDeviceId());
        // Implementation for ignition events
    }

    private void updateSyncMetrics(int positionsCount) {
        // Update Redis with sync metrics
        String key = "bridge:metrics:positions:count";
        redisTemplate.opsForValue().increment(key, positionsCount);
        redisTemplate.expire(key, 24, TimeUnit.HOURS);

        String timestampKey = "bridge:metrics:last_sync";
        redisTemplate.opsForValue().set(timestampKey, Instant.now().toString());
        redisTemplate.expire(timestampKey, 7, TimeUnit.DAYS);
    }

    private void resetDailyMetrics() {
        // Reset daily counters
        String pattern = "bridge:metrics:daily:*";
        redisTemplate.delete(redisTemplate.keys(pattern));
        log.info("Daily metrics reset");
    }

    /**
     * Manual sync trigger for testing/admin purposes
     */
    public void triggerManualSync() {
        log.info("Manual sync triggered");

        try {
            syncPositionData();
            syncDeviceHealth();
            syncEvents();
            log.info("Manual sync completed successfully");
        } catch (Exception e) {
            log.error("Manual sync failed", e);
            throw new SyncException("Manual sync failed", e);
        }
    }

    /**
     * Get sync statistics
     */
    public SyncStatistics getSyncStatistics() {
        try {
            String positionsKey = "bridge:metrics:positions:count";
            String lastSyncKey = "bridge:metrics:last_sync";

            Long positionsCount = (Long) redisTemplate.opsForValue().get(positionsKey);
            String lastSync = (String) redisTemplate.opsForValue().get(lastSyncKey);

            return SyncStatistics.builder()
                    .positionsProcessedToday(positionsCount != null ? positionsCount : 0)
                    .lastSyncTime(lastSync != null ? Instant.parse(lastSync) : null)
                    .syncSuccessCount(syncSuccessCounter.count())
                    .syncErrorCount(syncErrorCounter.count())
                    .averageSyncDuration(syncDurationTimer.mean(TimeUnit.MILLISECONDS))
                    .build();

        } catch (Exception e) {
            log.error("Error getting sync statistics", e);
            return SyncStatistics.builder().build();
        }
    }

    // Inner class for statistics
    @lombok.Builder
    @lombok.Data
    public static class SyncStatistics {
        private Long positionsProcessedToday;
        private Instant lastSyncTime;
        private double syncSuccessCount;
        private double syncErrorCount;
        private double averageSyncDuration;
    }
}