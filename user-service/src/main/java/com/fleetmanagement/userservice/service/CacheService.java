package com.fleetmanagement.userservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String USER_CACHE_PREFIX = "user:";
    private static final String SESSION_CACHE_PREFIX = "session:";
    private static final String PERMISSION_CACHE_PREFIX = "permission:";

    @Autowired
    public CacheService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    // User caching
    public void cacheUser(UUID userId, Object user) {
        try {
            String key = USER_CACHE_PREFIX + userId.toString();
            redisTemplate.opsForValue().set(key, user, Duration.ofMinutes(30));
            logger.debug("User cached: {}", userId);
        } catch (Exception e) {
            logger.error("Error caching user {}: {}", userId, e.getMessage());
        }
    }

    public Object getCachedUser(UUID userId) {
        try {
            String key = USER_CACHE_PREFIX + userId.toString();
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logger.error("Error retrieving cached user {}: {}", userId, e.getMessage());
            return null;
        }
    }

    public void evictUser(UUID userId) {
        try {
            String key = USER_CACHE_PREFIX + userId.toString();
            redisTemplate.delete(key);
            logger.debug("User cache evicted: {}", userId);
        } catch (Exception e) {
            logger.error("Error evicting user cache {}: {}", userId, e.getMessage());
        }
    }

    // Session caching
    public void cacheSession(String sessionToken, Object session) {
        try {
            String key = SESSION_CACHE_PREFIX + sessionToken;
            redisTemplate.opsForValue().set(key, session, Duration.ofHours(24));
            logger.debug("Session cached: {}", sessionToken);
        } catch (Exception e) {
            logger.error("Error caching session {}: {}", sessionToken, e.getMessage());
        }
    }

    public Object getCachedSession(String sessionToken) {
        try {
            String key = SESSION_CACHE_PREFIX + sessionToken;
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logger.error("Error retrieving cached session {}: {}", sessionToken, e.getMessage());
            return null;
        }
    }

    public void evictSession(String sessionToken) {
        try {
            String key = SESSION_CACHE_PREFIX + sessionToken;
            redisTemplate.delete(key);
            logger.debug("Session cache evicted: {}", sessionToken);
        } catch (Exception e) {
            logger.error("Error evicting session cache {}: {}", sessionToken, e.getMessage());
        }
    }

    // Permission caching
    public void cacheUserPermissions(UUID userId, Object permissions) {
        try {
            String key = PERMISSION_CACHE_PREFIX + userId.toString();
            redisTemplate.opsForValue().set(key, permissions, Duration.ofMinutes(15));
            logger.debug("Permissions cached for user: {}", userId);
        } catch (Exception e) {
            logger.error("Error caching permissions for user {}: {}", userId, e.getMessage());
        }
    }

    public Object getCachedUserPermissions(UUID userId) {
        try {
            String key = PERMISSION_CACHE_PREFIX + userId.toString();
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logger.error("Error retrieving cached permissions for user {}: {}", userId, e.getMessage());
            return null;
        }
    }

    public void evictUserPermissions(UUID userId) {
        try {
            String key = PERMISSION_CACHE_PREFIX + userId.toString();
            redisTemplate.delete(key);
            logger.debug("Permissions cache evicted for user: {}", userId);
        } catch (Exception e) {
            logger.error("Error evicting permissions cache for user {}: {}", userId, e.getMessage());
        }
    }

    // Generic cache operations
    public void set(String key, Object value, Duration duration) {
        try {
            redisTemplate.opsForValue().set(key, value, duration);
        } catch (Exception e) {
            logger.error("Error setting cache key {}: {}", key, e.getMessage());
        }
    }

    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logger.error("Error getting cache key {}: {}", key, e.getMessage());
            return null;
        }
    }

    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            logger.error("Error deleting cache key {}: {}", key, e.getMessage());
        }
    }
}