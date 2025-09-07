package com.fleetmanagement.userservice.domain.entity;

import com.fleetmanagement.userservice.domain.enums.UserRole;
import com.fleetmanagement.userservice.domain.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_username", columnList = "username"),
        @Index(name = "idx_user_company_id", columnList = "companyId"),
        @Index(name = "idx_user_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @NotBlank(message = "Password is required")
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @NotNull(message = "User role is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @NotNull(message = "User status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status;

    @Column(name = "company_id")
    private UUID companyId;

    @Size(max = 50, message = "Employee ID must not exceed 50 characters")
    @Column(name = "employee_id", length = 50)
    private String employeeId;

    @Size(max = 100, message = "Department must not exceed 100 characters")
    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "phone_verified", nullable = false)
    @Builder.Default
    private Boolean phoneVerified = false;

    @Column(name = "two_factor_enabled", nullable = false)
    @Builder.Default
    private Boolean twoFactorEnabled = false;

    @Column(name = "email_verification_token")
    private String emailVerificationToken;

    @Column(name = "email_verification_expiry")
    private LocalDateTime emailVerificationExpiry;

    @Column(name = "password_reset_token")
    private String passwordResetToken;

    @Column(name = "password_reset_expiry")
    private LocalDateTime passwordResetExpiry;

    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    @Column(name = "last_password_change")
    private LocalDateTime lastPasswordChange;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Column(name = "force_password_change", nullable = false)
    @Builder.Default
    private Boolean forcePasswordChange = false;

    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;

    @Size(max = 10, message = "Timezone must not exceed 10 characters")
    @Column(name = "timezone", length = 10)
    @Builder.Default
    private String timezone = "UTC";

    @Size(max = 10, message = "Language must not exceed 10 characters")
    @Column(name = "language", length = 10)
    @Builder.Default
    private String language = "en";

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Version
    @Column(name = "version")
    private Long version;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<UserSession> sessions = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<UserPermission> permissions = new HashSet<>();

    // Business Methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean hasRole(UserRole requiredRole) {
        return this.role == requiredRole;
    }

    public boolean hasAnyRole(UserRole... roles) {
        for (UserRole role : roles) {
            if (this.role == role) {
                return true;
            }
        }
        return false;
    }

    public boolean isAccountLocked() {
        return accountLockedUntil != null && accountLockedUntil.isAfter(LocalDateTime.now());
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE && !isAccountLocked();
    }

    public void lockAccount(long lockoutDurationMillis) {
        this.accountLockedUntil = LocalDateTime.now().plusNanos(lockoutDurationMillis * 1_000_000);
        this.status = UserStatus.LOCKED;
    }

    public void lockAccount(LocalDateTime lockUntil) {
        this.accountLockedUntil = lockUntil;
        this.status = UserStatus.LOCKED;
    }

    public void unlockAccount() {
        this.accountLockedUntil = null;
        this.failedLoginAttempts = 0;
        if (this.status == UserStatus.LOCKED) {
            this.status = UserStatus.ACTIVE;
        }
    }

    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
    }

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
    }

    public void updateLastLogin(String ipAddress) {
        this.lastLoginAt = LocalDateTime.now();
        this.lastLogin = LocalDateTime.now();
        this.lastLoginIp = ipAddress;
        resetFailedLoginAttempts();
    }

    public boolean isEmailVerificationExpired() {
        return emailVerificationExpiry != null && emailVerificationExpiry.isBefore(LocalDateTime.now());
    }

    public boolean isPasswordResetExpired() {
        return passwordResetExpiry != null && passwordResetExpiry.isBefore(LocalDateTime.now());
    }

    // Additional getters for backward compatibility
    public String getProfileImageUrl() {
        // Return profileImageUrl if set, otherwise return profilePictureUrl
        return profileImageUrl != null ? profileImageUrl : profilePictureUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
        // Also set profilePictureUrl for backward compatibility
        this.profilePictureUrl = profileImageUrl;
    }

    public LocalDateTime getLastLogin() {
        // Return lastLogin if set, otherwise return lastLoginAt
        return lastLogin != null ? lastLogin : lastLoginAt;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
        // Also set lastLoginAt for backward compatibility
        this.lastLoginAt = lastLogin;
    }

    public LocalDateTime getLastPasswordChange() {
        // Return lastPasswordChange if set, otherwise return passwordChangedAt
        return lastPasswordChange != null ? lastPasswordChange : passwordChangedAt;
    }

    public void setLastPasswordChange(LocalDateTime lastPasswordChange) {
        this.lastPasswordChange = lastPasswordChange;
        // Also set passwordChangedAt for backward compatibility
        this.passwordChangedAt = lastPasswordChange;
    }
}