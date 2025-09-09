package com.fleetmanagement.userservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetmanagement.userservice.domain.entity.User;
import com.fleetmanagement.userservice.dto.response.UserSessionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis Session Service
 *
 * Service for managing user sessions in Redis including:
 * - Session creation and storage
 * - Multi-device session management
 * - Session invalidation and cleanup
 * - Active session tracking
 */
@Service
public class RedisSessionService {

    private static final Logger logger = LoggerFactory.getLogger(RedisSessionService.class);

    private static final String USER_SESSION_PREFIX = "user:session:";
    private static final String USER_SESSIONS_SET_PREFIX = "user:sessions:";
    private static final String ACTIVE_SESSIONS_PREFIX = "active:sessions:";
    private static final String SESSION_ACTIVITY_PREFIX = "session:activity:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.session.max-concurrent-sessions:5}")
    private int maxConcurrentSessions;

    @Value("${app.session.timeout-hours:24}")
    private long sessionTimeoutHours;

    @Value("${app.session.activity-timeout-minutes:30}")
    private long activityTimeoutMinutes;

    @Value("${app.session.cleanup-enabled:true}")
    private boolean cleanupEnabled;

    @Autowired
    public RedisSessionService(RedisTemplate<String, Object> redisTemplate,
                               ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Create a new session for user
     */
    public UserSessionInfo createSession(User user, String deviceInfo, String ipAddress, String jwtToken) {
        logger.info("Creating session for user: {} from device: {}", user.getId(), deviceInfo);

        String sessionId = generateSessionId();
        LocalDateTime now = LocalDateTime.now();

        UserSessionInfo sessionInfo = UserSessionInfo.builder()
                .sessionId(sessionId)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .companyId(user.getCompanyId())
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .jwtToken(jwtToken)
                .createdAt(now)
                .lastActivityAt(now)
                .isActive(true)
                .build();

        // Store session data
        storeSession(sessionInfo);

        // Add to user's session set
        addToUserSessions(user.getId(), sessionId);

        // Enforce concurrent session limits
        enforceConcurrentSessionLimit(user.getId());

        // Track in active sessions
        trackActiveSession(sessionId, user.getId());

        logger.info("Session created successfully: {} for user: {}", sessionId, user.getId());
        return sessionInfo;
    }

    /**
     * Get session information
     */
    public Optional<UserSessionInfo> getSession(String sessionId) {
        logger.debug("Retrieving session: {}", sessionId);

        String sessionKey = USER_SESSION_PREFIX + sessionId;
        String sessionData = (String) redisTemplate.opsForValue().get(sessionKey);

        if (sessionData == null) {
            logger.debug("Session not found: {}", sessionId);
            return Optional.empty();
        }

        try {
            UserSessionInfo sessionInfo = objectMapper.readValue(sessionData, UserSessionInfo.class);
            return Optional.of(sessionInfo);
        } catch (JsonProcessingException e) {
            logger.error("Error deserializing session data for session: {}", sessionId, e);
            return Optional.empty();
        }
    }

    /**
     * Update session activity
     */
    public void updateSessionActivity(String sessionId) {
        logger.debug("Updating activity for session: {}", sessionId);

        Optional<UserSessionInfo> sessionOpt = getSession(sessionId);
        if (sessionOpt.isEmpty()) {
            logger.warn("Cannot update activity for non-existent session: {}", sessionId);
            return;
        }

        UserSessionInfo sessionInfo = sessionOpt.get();
        sessionInfo.setLastActivityAt(LocalDateTime.now());

        storeSession(sessionInfo);
        updateSessionActivityTimestamp(sessionId);
    }

    /**
     * Invalidate a specific session
     */
    public void invalidateSession(String sessionId) {
        logger.info("Invalidating session: {}", sessionId);

        Optional<UserSessionInfo> sessionOpt = getSession(sessionId);
        if (sessionOpt.isEmpty()) {
            logger.warn("Cannot invalidate non-existent session: {}", sessionId);
            return;
        }

        UserSessionInfo sessionInfo = sessionOpt.get();
        UUID userId = sessionInfo.getUserId();

        // Remove session data
        String sessionKey = USER_SESSION_PREFIX + sessionId;
        redisTemplate.delete(sessionKey);

        // Remove from user's session set
        removeFromUserSessions(userId, sessionId);

        // Remove from active sessions
        removeFromActiveSessions(sessionId);

        // Remove activity tracking
        removeSessionActivity(sessionId);

        logger.info("Session invalidated successfully: {}", sessionId);
    }

    /**
     * Invalidate all sessions for a user
     */
    public void invalidateAllUserSessions(UUID userId) {
        logger.info("Invalidating all sessions for user: {}", userId);

        Set<String> userSessions = getUserSessions(userId);

        for (String sessionId : userSessions) {
            invalidateSession(sessionId);
        }

        logger.info("Invalidated {} sessions for user: {}", userSessions.size(), userId);
    }

    /**
     * Get all active sessions for a user
     */
    public List<UserSessionInfo> getUserActiveSessions(UUID userId) {
        logger.debug("Retrieving active sessions for user: {}", userId);

        Set<String> sessionIds = getUserSessions(userId);

        return sessionIds.stream()
                .map(this::getSession)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(session -> session.isActive())
                .sorted((s1, s2) -> s2.getLastActivityAt().compareTo(s1.getLastActivityAt()))
                .collect(Collectors.toList());
    }

    /**
     * Check if session is valid and active
     */
    public boolean isSessionValid(String sessionId) {
        Optional<UserSessionInfo> sessionOpt = getSession(sessionId);

        if (sessionOpt.isEmpty()) {
            return false;
        }

        UserSessionInfo sessionInfo = sessionOpt.get();

        // Check if session is active
        if (!sessionInfo.isActive()) {
            return false;
        }

        // Check if session has expired due to inactivity
        LocalDateTime lastActivity = sessionInfo.getLastActivityAt();
        LocalDateTime activityThreshold = LocalDateTime.now().minusMinutes(activityTimeoutMinutes);

        if (lastActivity.isBefore(activityThreshold)) {
            logger.debug("Session {} expired due to inactivity", sessionId);
            invalidateSession(sessionId);
            return false;
        }

        return true;
    }

    /**
     * Force logout from specific device
     */
    public void forceLogoutFromDevice(UUID userId, String deviceInfo) {
        logger.info("Force logout for user: {} from device: {}", userId, deviceInfo);

        List<UserSessionInfo> userSessions = getUserActiveSessions(userId);

        userSessions.stream()
                .filter(session -> deviceInfo.equals(session.getDeviceInfo()))
                .forEach(session -> invalidateSession(session.getSessionId()));
    }

    /**
     * Get session statistics for user
     */
    public SessionStatistics getSessionStatistics(UUID userId) {
        List<UserSessionInfo> activeSessions = getUserActiveSessions(userId);

        long totalSessions = activeSessions.size();

        Map<String, Long> deviceCounts = activeSessions.stream()
                .collect(Collectors.groupingBy(
                        UserSessionInfo::getDeviceInfo,
                        Collectors.counting()
                ));

        Optional<LocalDateTime> lastActivity = activeSessions.stream()
                .map(UserSessionInfo::getLastActivityAt)
                .max(LocalDateTime::compareTo);

        return SessionStatistics.builder()
                .userId(userId)
                .totalActiveSessions(totalSessions)
                .deviceCounts(deviceCounts)
                .lastActivity(lastActivity.orElse(null))
                .maxAllowedSessions(maxConcurrentSessions)
                .build();
    }

    /**
     * Clean up expired sessions (scheduled task)
     */
    public int cleanupExpiredSessions() {
        if (!cleanupEnabled) {
            return 0;
        }

        logger.info("Starting cleanup of expired sessions");

        Set<String> activeSessionKeys = redisTemplate.keys(ACTIVE_SESSIONS_PREFIX + "*");
        int cleanedUp = 0;

        for (String activeSessionKey : activeSessionKeys) {
            String sessionId = activeSessionKey.substring(ACTIVE_SESSIONS_PREFIX.length());

            if (!isSessionValid(sessionId)) {
                cleanedUp++;
            }
        }

        logger.info("Cleaned up {} expired sessions", cleanedUp);
        return cleanedUp;
    }

    // Private helper methods

    private void storeSession(UserSessionInfo sessionInfo) {
        try {
            String sessionKey = USER_SESSION_PREFIX + sessionInfo.getSessionId();
            String sessionData = objectMapper.writeValueAsString(sessionInfo);

            redisTemplate.opsForValue().set(sessionKey, sessionData, sessionTimeoutHours, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing session data for session: {}", sessionInfo.getSessionId(), e);
            throw new RuntimeException("Failed to store session data", e);
        }
    }

    private Set<String> getUserSessions(UUID userId) {
        String userSessionsKey = USER_SESSIONS_SET_PREFIX + userId;
        Set<Object> sessionObjects = redisTemplate.opsForSet().members(userSessionsKey);

        return sessionObjects != null ?
                sessionObjects.stream()
                        .map(Object::toString)
                        .collect(Collectors.toSet()) :
                new HashSet<>();
    }

    private void addToUserSessions(UUID userId, String sessionId) {
        String userSessionsKey = USER_SESSIONS_SET_PREFIX + userId;
        redisTemplate.opsForSet().add(userSessionsKey, sessionId);
        redisTemplate.expire(userSessionsKey, sessionTimeoutHours, TimeUnit.HOURS);
    }

    private void removeFromUserSessions(UUID userId, String sessionId) {
        String userSessionsKey = USER_SESSIONS_SET_PREFIX + userId;
        redisTemplate.opsForSet().remove(userSessionsKey, sessionId);
    }

    private void enforceConcurrentSessionLimit(UUID userId) {
        Set<String> userSessions = getUserSessions(userId);

        if (userSessions.size() > maxConcurrentSessions) {
            // Get sessions sorted by last activity (oldest first)
            List<UserSessionInfo> sessions = userSessions.stream()
                    .map(this::getSession)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .sorted(Comparator.comparing(UserSessionInfo::getLastActivityAt))
                    .collect(Collectors.toList());

            // Remove oldest sessions to enforce limit
            int sessionsToRemove = sessions.size() - maxConcurrentSessions;
            for (int i = 0; i < sessionsToRemove; i++) {
                String sessionToRemove = sessions.get(i).getSessionId();
                logger.info("Removing oldest session {} for user {} due to concurrent session limit",
                        sessionToRemove, userId);
                invalidateSession(sessionToRemove);
            }
        }
    }

    private void trackActiveSession(String sessionId, UUID userId) {
        String activeSessionKey = ACTIVE_SESSIONS_PREFIX + sessionId;
        redisTemplate.opsForValue().set(activeSessionKey, userId.toString(), sessionTimeoutHours, TimeUnit.HOURS);
    }

    private void removeFromActiveSessions(String sessionId) {
        String activeSessionKey = ACTIVE_SESSIONS_PREFIX + sessionId;
        redisTemplate.delete(activeSessionKey);
    }

    private void updateSessionActivityTimestamp(String sessionId) {
        String activityKey = SESSION_ACTIVITY_PREFIX + sessionId;
        long timestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        redisTemplate.opsForValue().set(activityKey, timestamp, activityTimeoutMinutes, TimeUnit.MINUTES);
    }

    private void removeSessionActivity(String sessionId) {
        String activityKey = SESSION_ACTIVITY_PREFIX + sessionId;
        redisTemplate.delete(activityKey);
    }

    private String generateSessionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    // Supporting DTOs
    @lombok.Data
    @lombok.Builder
    public static class SessionStatistics {
        private UUID userId;
        private long totalActiveSessions;
        private Map<String, Long> deviceCounts;
        private LocalDateTime lastActivity;
        private int maxAllowedSessions;
    }
}