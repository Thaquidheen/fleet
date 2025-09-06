package com.fleetmanagement.userservice.service;

import com.fleetmanagement.userservice.domain.entity.User;
import com.fleetmanagement.userservice.domain.entity.UserSession;
import com.fleetmanagement.userservice.domain.enums.SessionStatus;
import com.fleetmanagement.userservice.repository.UserSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserSessionService {

    private static final Logger logger = LoggerFactory.getLogger(UserSessionService.class);

    private final UserSessionRepository sessionRepository;
    private final CacheService cacheService;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Autowired
    public UserSessionService(UserSessionRepository sessionRepository, CacheService cacheService) {
        this.sessionRepository = sessionRepository;
        this.cacheService = cacheService;
    }

    public UserSession createSession(User user, String ipAddress, String userAgent) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusSeconds(jwtExpiration / 1000);
        LocalDateTime refreshExpiresAt = now.plusSeconds(refreshExpiration / 1000);

        UserSession session = new UserSession();
        session.setUser(user);
        session.setExpiresAt(expiresAt);
        session.setRefreshExpiresAt(refreshExpiresAt);
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        session.setStatus(SessionStatus.ACTIVE);
        session.setLastAccessed(now);

        UserSession savedSession = sessionRepository.save(session);
        logger.info("Session created for user ID: {}", user.getId());

        return savedSession;
    }

    public void updateSessionTokens(UUID sessionId, String accessToken, String refreshToken) {
        UserSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        session.setSessionToken(accessToken);
        session.setRefreshToken(refreshToken);
        sessionRepository.save(session);

        // Cache the session
        cacheService.cacheSession(accessToken, session);
    }

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void cleanupExpiredSessions() {
        logger.info("Starting expired session cleanup");

        LocalDateTime now = LocalDateTime.now();

        // Mark expired sessions
        sessionRepository.expireOldSessions(now);

        // Delete old inactive sessions (older than 7 days)
        LocalDateTime deleteBefore = now.minusDays(7);
        sessionRepository.deleteOldInactiveSessions(deleteBefore);

        logger.info("Expired session cleanup completed");
    }

    public List<UserSession> getActiveSessionsForUser(UUID userId) {
        return sessionRepository.findActiveSessionsByUserId(userId, LocalDateTime.now());
    }

    public void revokeUserSessions(UUID userId) {
        sessionRepository.revokeAllActiveSessionsByUserId(userId);
        logger.info("All sessions revoked for user ID: {}", userId);
    }

    public void revokeSession(UUID sessionId) {
        UserSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        session.revoke();
        sessionRepository.save(session);

        // Remove from cache
        if (session.getSessionToken() != null) {
            cacheService.evictSession(session.getSessionToken());
        }

        logger.info("Session revoked: {}", sessionId);
    }
}