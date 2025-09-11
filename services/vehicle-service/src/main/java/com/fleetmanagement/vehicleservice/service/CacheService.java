package com.fleetmanagement.vehicleservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Cache Service
 *
 * Manages Redis caching operations for the Vehicle Service.
 * Provides centralized cache management with proper eviction strategies.
 */
@Service
public class CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

    // Cache Names
    public static final String VEHICLE_DETAILS_CACHE = "vehicle-details";
    public static final String VEHICLE_LISTS_CACHE = "vehicle-lists";
    public static final String ASSIGNMENTS_CACHE = "assignments";
    public static final String COMPANY_VEHICLES_CACHE = "company-vehicles";
    public static final String DRIVER_ASSIGNMENTS_CACHE = "driver-assignments";
    public static final String FLEET_ANALYTICS_CACHE = "fleet-analytics";

    // Cache Key Patterns
    private static final String VEHICLE_DETAILS_KEY = "vehicle:details:%s";
    private static final String VEHICLE_LIST_KEY = "company:%s:vehicles:page:%d:size:%d";
    private static final String DRIVER_ASSIGNMENT_KEY = "driver:%s:current-vehicle";
    private static final String FLEET_STATS_KEY = "company:%s:fleet-stats";
    private static final String VEHICLE_ASSIGNMENT_KEY = "vehicle:%s:assignment";

    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public CacheService(CacheManager cacheManager, RedisTemplate<String, Object> redisTemplate) {
        this.cacheManager = cacheManager;
        this.redisTemplate = redisTemplate;
    }
    public void clearVehicleGroupCache(UUID companyId) {
        // Implementation for clearing vehicle group cache
    }

    public void clearVehicleCache(UUID vehicleId) {
        // Implementation for clearing vehicle cache
    }
    /**
     * Evict all vehicle-related caches for a company
     */
    public void evictVehicleCaches(UUID companyId) {
        logger.debug("Evicting vehicle caches for company: {}", companyId);

        try {
            // Evict company-specific vehicle lists
            evictCompanyVehicleListCaches(companyId);

            // Evict fleet analytics cache
            evictFleetAnalyticsCache(companyId);

            // Evict all vehicle details for this company (requires pattern matching)
            evictVehicleDetailsCacheByCompany(companyId);

            logger.debug("Successfully evicted vehicle caches for company: {}", companyId);
        } catch (Exception e) {
            logger.error("Error evicting vehicle caches for company: {}", companyId, e);
        }
    }

    /**
     * Evict specific vehicle cache
     */
    public void evictVehicleCache(UUID vehicleId) {
        logger.debug("Evicting cache for vehicle: {}", vehicleId);

        try {
            String key = String.format(VEHICLE_DETAILS_KEY, vehicleId);
            redisTemplate.delete(key);

            logger.debug("Successfully evicted cache for vehicle: {}", vehicleId);
        } catch (Exception e) {
            logger.error("Error evicting cache for vehicle: {}", vehicleId, e);
        }
    }

    /**
     * Evict assignment cache for a driver
     */
    public void evictDriverAssignmentCache(UUID driverId) {
        logger.debug("Evicting assignment cache for driver: {}", driverId);

        try {
            String key = String.format(DRIVER_ASSIGNMENT_KEY, driverId);
            redisTemplate.delete(key);

            // Also evict from Spring Cache Manager
            if (cacheManager.getCache(DRIVER_ASSIGNMENTS_CACHE) != null) {
                cacheManager.getCache(DRIVER_ASSIGNMENTS_CACHE).evict(driverId);
            }

            logger.debug("Successfully evicted assignment cache for driver: {}", driverId);
        } catch (Exception e) {
            logger.error("Error evicting assignment cache for driver: {}", driverId, e);
        }
    }

    /**
     * Evict assignment cache for a vehicle
     */
    public void evictVehicleAssignmentCache(UUID vehicleId) {
        logger.debug("Evicting assignment cache for vehicle: {}", vehicleId);

        try {
            String key = String.format(VEHICLE_ASSIGNMENT_KEY, vehicleId);
            redisTemplate.delete(key);

            // Also evict from Spring Cache Manager
            if (cacheManager.getCache(ASSIGNMENTS_CACHE) != null) {
                cacheManager.getCache(ASSIGNMENTS_CACHE).evict(vehicleId);
            }

            logger.debug("Successfully evicted assignment cache for vehicle: {}", vehicleId);
        } catch (Exception e) {
            logger.error("Error evicting assignment cache for vehicle: {}", vehicleId, e);
        }
    }

    /**
     * Cache driver's current vehicle assignment
     */
    public void cacheDriverAssignment(UUID driverId, UUID vehicleId, long ttlMinutes) {
        logger.debug("Caching driver assignment: driver={}, vehicle={}", driverId, vehicleId);

        try {
            String key = String.format(DRIVER_ASSIGNMENT_KEY, driverId);
            redisTemplate.opsForValue().set(key, vehicleId, ttlMinutes, TimeUnit.MINUTES);

            logger.debug("Successfully cached driver assignment");
        } catch (Exception e) {
            logger.error("Error caching driver assignment", e);
        }
    }

    /**
     * Get driver's current vehicle assignment from cache
     */
    public UUID getDriverCurrentVehicle(UUID driverId) {
        try {
            String key = String.format(DRIVER_ASSIGNMENT_KEY, driverId);
            Object result = redisTemplate.opsForValue().get(key);
            return result != null ? (UUID) result : null;
        } catch (Exception e) {
            logger.error("Error getting driver assignment from cache", e);
            return null;
        }
    }

    /**
     * Cache fleet statistics
     */
    public void cacheFleetStats(UUID companyId, Object stats, long ttlHours) {
        logger.debug("Caching fleet stats for company: {}", companyId);

        try {
            String key = String.format(FLEET_STATS_KEY, companyId);
            redisTemplate.opsForValue().set(key, stats, ttlHours, TimeUnit.HOURS);

            logger.debug("Successfully cached fleet stats");
        } catch (Exception e) {
            logger.error("Error caching fleet stats", e);
        }
    }

    /**
     * Get fleet statistics from cache
     */
    public Object getFleetStats(UUID companyId) {
        try {
            String key = String.format(FLEET_STATS_KEY, companyId);
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logger.error("Error getting fleet stats from cache", e);
            return null;
        }
    }

    /**
     * Evict all caches (use with caution)
     */
    public void evictAllCaches() {
        logger.warn("Evicting ALL vehicle service caches");

        try {
            // Clear Spring Cache Manager caches
            cacheManager.getCacheNames().forEach(cacheName -> {
                if (cacheManager.getCache(cacheName) != null) {
                    cacheManager.getCache(cacheName).clear();
                }
            });

            // Clear Redis keys with specific patterns
            clearRedisPattern("vehicle:*");
            clearRedisPattern("company:*:vehicles:*");
            clearRedisPattern("driver:*:current-vehicle");
            clearRedisPattern("company:*:fleet-stats");

            logger.warn("Successfully evicted all caches");
        } catch (Exception e) {
            logger.error("Error evicting all caches", e);
        }
    }

    /**
     * Get cache statistics
     */
    public CacheStatistics getCacheStatistics() {
        CacheStatistics stats = new CacheStatistics();

        try {
            // Get cache names and sizes from Spring Cache Manager
            for (String cacheName : cacheManager.getCacheNames()) {
                if (cacheManager.getCache(cacheName) != null) {
                    // Note: Spring Cache doesn't provide direct size info
                    // This would need to be enhanced based on your cache implementation
                    stats.addCacheInfo(cacheName, "Available", 0);
                }
            }

            // Get Redis key count for our patterns
            long vehicleKeys = countKeysWithPattern("vehicle:*");
            long companyKeys = countKeysWithPattern("company:*:vehicles:*");
            long driverKeys = countKeysWithPattern("driver:*:current-vehicle");

            stats.addCacheInfo("Redis Vehicle Keys", "Active", vehicleKeys);
            stats.addCacheInfo("Redis Company Keys", "Active", companyKeys);
            stats.addCacheInfo("Redis Driver Keys", "Active", driverKeys);

        } catch (Exception e) {
            logger.error("Error getting cache statistics", e);
        }

        return stats;
    }

    // Private helper methods

    private void evictCompanyVehicleListCaches(UUID companyId) {
        // Evict Spring Cache
        if (cacheManager.getCache(VEHICLE_LISTS_CACHE) != null) {
            cacheManager.getCache(VEHICLE_LISTS_CACHE).clear(); // Clear all for simplicity
        }

        if (cacheManager.getCache(COMPANY_VEHICLES_CACHE) != null) {
            cacheManager.getCache(COMPANY_VEHICLES_CACHE).clear();
        }

        // Evict Redis patterns
        String pattern = String.format("company:%s:vehicles:*", companyId);
        clearRedisPattern(pattern);
    }

    private void evictFleetAnalyticsCache(UUID companyId) {
        // Evict Spring Cache
        if (cacheManager.getCache(FLEET_ANALYTICS_CACHE) != null) {
            cacheManager.getCache(FLEET_ANALYTICS_CACHE).evict(companyId);
        }

        // Evict Redis
        String key = String.format(FLEET_STATS_KEY, companyId);
        redisTemplate.delete(key);
    }

    private void evictVehicleDetailsCacheByCompany(UUID companyId) {
        // This is more complex as we need to find all vehicle IDs for the company
        // For now, we'll clear the entire vehicle details cache
        if (cacheManager.getCache(VEHICLE_DETAILS_CACHE) != null) {
            cacheManager.getCache(VEHICLE_DETAILS_CACHE).clear();
        }

        // Clear Redis vehicle details pattern
        clearRedisPattern("vehicle:details:*");
    }

    private void clearRedisPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                logger.debug("Cleared {} Redis keys matching pattern: {}", keys.size(), pattern);
            }
        } catch (Exception e) {
            logger.error("Error clearing Redis pattern: {}", pattern, e);
        }
    }

    private long countKeysWithPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            return keys != null ? keys.size() : 0;
        } catch (Exception e) {
            logger.error("Error counting keys with pattern: {}", pattern, e);
            return 0;
        }
    }

    /**
     * Cache Statistics DTO
     */
    public static class CacheStatistics {
        private final java.util.Map<String, CacheInfo> caches = new java.util.HashMap<>();

        public void addCacheInfo(String cacheName, String status, long size) {
            caches.put(cacheName, new CacheInfo(status, size));
        }

        public java.util.Map<String, CacheInfo> getCaches() {
            return caches;
        }

        public static class CacheInfo {
            private final String status;
            private final long size;

            public CacheInfo(String status, long size) {
                this.status = status;
                this.size = size;
            }

            public String getStatus() { return status; }
            public long getSize() { return size; }
        }
    }
}