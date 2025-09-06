package com.fleetmanagement.userservice.domain.entity;

import com.fleetmanagement.userservice.domain.enums.SessionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_sessions", indexes = {
        @Index(name = "idx_session_token", columnList = "sessionToken"),
        @Index(name = "idx_session_user_id", columnList = "user_id"),
        @Index(name = "idx_session_status", columnList = "status"),
        @Index(name = "idx_session_expires_at", columnList = "expiresAt")
})
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @NotBlank(message = "Session token is required")
    @Column(name = "session_token", unique = true, nullable = false, length = 500)
    private String sessionToken;

    @NotBlank(message = "Refresh token is required")
    @Column(name = "refresh_token", unique = true, nullable = false, length = 500)
    private String refreshToken;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "Session status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SessionStatus status;

    @NotNull(message = "Expires at is required")
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @NotNull(message = "Refresh expires at is required")
    @Column(name = "refresh_expires_at", nullable = false)
    private LocalDateTime refreshExpiresAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "device_info", length = 255)
    private String deviceInfo;

    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public UserSession() {}

    public UserSession(String sessionToken, String refreshToken, User user,
                       LocalDateTime expiresAt, LocalDateTime refreshExpiresAt,
                       String ipAddress, String userAgent) {
        this.sessionToken = sessionToken;
        this.refreshToken = refreshToken;
        this.user = user;
        this.expiresAt = expiresAt;
        this.refreshExpiresAt = refreshExpiresAt;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.status = SessionStatus.ACTIVE;
        this.lastAccessed = LocalDateTime.now();
    }

    // Business Methods
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isRefreshExpired() {
        return refreshExpiresAt != null && refreshExpiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isActive() {
        return status == SessionStatus.ACTIVE && !isExpired();
    }

    public void revoke() {
        this.status = SessionStatus.REVOKED;
    }

    public void expire() {
        this.status = SessionStatus.EXPIRED;
    }

    public void updateLastAccessed() {
        this.lastAccessed = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getRefreshExpiresAt() { return refreshExpiresAt; }
    public void setRefreshExpiresAt(LocalDateTime refreshExpiresAt) {
        this.refreshExpiresAt = refreshExpiresAt;
    }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }

    public LocalDateTime getLastAccessed() { return lastAccessed; }
    public void setLastAccessed(LocalDateTime lastAccessed) { this.lastAccessed = lastAccessed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}