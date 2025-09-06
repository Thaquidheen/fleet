
// UserSessionRepository.java
package com.fleetmanagement.userservice.repository;

import com.fleetmanagement.userservice.domain.entity.UserSession;
import com.fleetmanagement.userservice.domain.enums.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    // Basic find methods
    Optional<UserSession> findBySessionToken(String sessionToken);

    Optional<UserSession> findByRefreshToken(String refreshToken);

    // User-specific queries
    List<UserSession> findByUserId(UUID userId);

    Page<UserSession> findByUserId(UUID userId, Pageable pageable);

    List<UserSession> findByUserIdAndStatus(UUID userId, SessionStatus status);

    Page<UserSession> findByUserIdAndStatus(UUID userId, SessionStatus status, Pageable pageable);

    // Active session queries
    List<UserSession> findByUserIdAndStatusAndExpiresAtAfter(UUID userId,
                                                             SessionStatus status,
                                                             LocalDateTime now);

    @Query("SELECT s FROM UserSession s WHERE s.user.id = :userId AND s.status = 'ACTIVE' " +
            "AND s.expiresAt > :now ORDER BY s.lastAccessed DESC")
    List<UserSession> findActiveSessionsByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    // Session management
    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.user.id = :userId AND s.status = 'ACTIVE' " +
            "AND s.expiresAt > :now")
    long countActiveSessionsByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    // Expired session queries
    List<UserSession> findByExpiresAtBefore(LocalDateTime dateTime);

    List<UserSession> findByRefreshExpiresAtBefore(LocalDateTime dateTime);

    List<UserSession> findByStatusAndExpiresAtBefore(SessionStatus status, LocalDateTime dateTime);

    // Update operations
    @Modifying
    @Query("UPDATE UserSession s SET s.lastAccessed = :lastAccessed WHERE s.sessionToken = :sessionToken")
    void updateLastAccessed(@Param("sessionToken") String sessionToken,
                            @Param("lastAccessed") LocalDateTime lastAccessed);

    @Modifying
    @Query("UPDATE UserSession s SET s.status = :status WHERE s.sessionToken = :sessionToken")
    void updateSessionStatus(@Param("sessionToken") String sessionToken, @Param("status") SessionStatus status);

    @Modifying
    @Query("UPDATE UserSession s SET s.status = 'REVOKED' WHERE s.user.id = :userId AND s.status = 'ACTIVE'")
    void revokeAllActiveSessionsByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE UserSession s SET s.status = 'REVOKED' WHERE s.user.id = :userId AND s.id != :excludeSessionId " +
            "AND s.status = 'ACTIVE'")
    void revokeOtherActiveSessionsByUserId(@Param("userId") UUID userId, @Param("excludeSessionId") UUID excludeSessionId);

    @Modifying
    @Query("UPDATE UserSession s SET s.status = 'EXPIRED' WHERE s.expiresAt < :now AND s.status = 'ACTIVE'")
    void expireOldSessions(@Param("now") LocalDateTime now);

    // Cleanup operations
    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.status IN ('EXPIRED', 'REVOKED') AND s.updatedAt < :deleteBefore")
    void deleteOldInactiveSessions(@Param("deleteBefore") LocalDateTime deleteBefore);

    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.expiresAt < :expiredBefore AND s.refreshExpiresAt < :expiredBefore")
    void deleteExpiredSessions(@Param("expiredBefore") LocalDateTime expiredBefore);

    // Statistics
    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.status = 'ACTIVE' AND s.expiresAt > :now")
    long countActiveSessions(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(DISTINCT s.user.id) FROM UserSession s WHERE s.status = 'ACTIVE' " +
            "AND s.expiresAt > :now AND s.lastAccessed > :since")
    long countActiveUsersSince(@Param("now") LocalDateTime now, @Param("since") LocalDateTime since);
}

