package com.fleetmanagement.userservice.service;

import com.fleetmanagement.userservice.domain.enums.UserRole;
import com.fleetmanagement.userservice.dto.request.CreateUserRequest;
import com.fleetmanagement.userservice.domain.entity.User;
import com.fleetmanagement.userservice.domain.entity.UserSession;
import com.fleetmanagement.userservice.domain.enums.SessionStatus;
import com.fleetmanagement.userservice.domain.enums.UserStatus;
import com.fleetmanagement.userservice.dto.request.LoginRequest;
import com.fleetmanagement.userservice.dto.request.RefreshTokenRequest;
import com.fleetmanagement.userservice.dto.response.AuthenticationResponse;
import com.fleetmanagement.userservice.exception.AuthenticationFailedException;
import com.fleetmanagement.userservice.exception.ResourceNotFoundException;
import com.fleetmanagement.userservice.repository.UserRepository;
import com.fleetmanagement.userservice.repository.UserSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final UserSessionService sessionService;
    private final CacheService cacheService;

    @Value("${app.security.account-lockout.max-attempts:5}")
    private int maxFailedAttempts;

    @Value("${app.security.account-lockout.lockout-duration:900000}") // 15 minutes
    private long lockoutDuration;

    @Value("${app.security.session.max-concurrent-sessions:3}")
    private int maxConcurrentSessions;

    @Value("${jwt.expiration:86400000}")  // 24 hours default
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration:604800000}")  // 7 days default
    private long refreshExpiration;

    @Autowired
    public AuthenticationService(UserRepository userRepository,
                                 UserSessionRepository sessionRepository,
                                 PasswordEncoder passwordEncoder,
                                 JwtTokenService jwtTokenService,
                                 UserSessionService sessionService, CacheService cacheService) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.sessionService = sessionService;
        this.cacheService = cacheService;
    }

    public AuthenticationResponse register(CreateUserRequest request, String ipAddress, String userAgent) {
        logger.info("Registration attempt for email: {}", request.getEmail());

        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new AuthenticationFailedException("User already exists with this email");
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new AuthenticationFailedException("Username is already taken");
        }

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(request.getRole() != null ? request.getRole() : UserRole.DRIVER)                  // ADD: Missing role
                .companyId(request.getCompanyId())          // ADD: Missing companyId
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .failedLoginAttempts(0)
                .lastPasswordChange(LocalDateTime.now())
                .build();                                   // ADD: Missing .build()

        // Save user
        User savedUser = userRepository.save(user);
        logger.info("User created successfully with ID: {}", savedUser.getId());

        // Optional: Send email verification
        // emailService.sendEmailVerification(savedUser.getEmail(), savedUser.getFullName(), verificationToken);

        // Return success response WITHOUT tokens
        return AuthenticationResponse.builder()
                .success(true)
                .message("User registered successfully. Please log in to continue.")
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .companyId(savedUser.getCompanyId())
                .emailVerified(savedUser.getEmailVerified())
                .requiresPasswordChange(false)
                // NO session, tokens, or session-related fields
                .build();
    }
    /**
     * Authenticate user and create session
     */
    public AuthenticationResponse authenticate(LoginRequest request, String ipAddress, String userAgent) {
        logger.info("Authentication attempt for username/email: {}", request.getUsernameOrEmail());

        // Find user by username or email
        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new AuthenticationFailedException("Invalid credentials"));

        // Check account status
        validateAccountStatus(user);

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleFailedLogin(user);
            throw new AuthenticationFailedException("Invalid credentials");
        }

        // Check if password change is required
        if (user.getForcePasswordChange()) {
            return AuthenticationResponse.builder()
                    .requiresPasswordChange(true)
                    .userId(user.getId())
                    .message("Password change required")
                    .build();
        }

        // Reset failed login attempts on successful authentication
        if (user.getFailedLoginAttempts() > 0) {
            user.resetFailedLoginAttempts();
            userRepository.save(user);
        }

        // Manage concurrent sessions
        manageConcurrentSessions(user);

        // Create session WITHOUT saving it first
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

        String tempSessionId = UUID.randomUUID().toString();
        String accessToken = jwtTokenService.generateAccessToken(user, tempSessionId);
        String refreshToken = jwtTokenService.generateRefreshToken(user, tempSessionId);

        session.setSessionToken(accessToken);
        session.setRefreshToken(refreshToken);
        UserSession savedSession = sessionRepository.save(session);

// Cache the session
        cacheService.cacheSession(accessToken, savedSession);

        if (!tempSessionId.equals(savedSession.getId().toString())) {
            accessToken = jwtTokenService.generateAccessToken(user, savedSession.getId().toString());
            refreshToken = jwtTokenService.generateRefreshToken(user, savedSession.getId().toString());

            savedSession.setSessionToken(accessToken);
            savedSession.setRefreshToken(refreshToken);
            sessionRepository.save(savedSession);

            // Cache the updated session
            cacheService.cacheSession(accessToken, savedSession);
        }

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Create token info
        Map<String, Object> tokenInfo = jwtTokenService.createTokenInfo(accessToken, refreshToken);

        logger.info("Authentication successful for user ID: {}", user.getId());

        return AuthenticationResponse.builder()
                .success(true)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .companyId(user.getCompanyId())
                .emailVerified(user.getEmailVerified())
                .sessionId(savedSession.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn((Long) tokenInfo.get("expiresIn"))
                .refreshExpiresIn((Long) tokenInfo.get("refreshExpiresIn"))
                .requiresPasswordChange(false)
                .message("Authentication successful")
                .build();
    }
    /**
     * Refresh access token
     */
    public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
        logger.info("Token refresh attempt");

        // Validate refresh token
        if (!jwtTokenService.validateRefreshToken(request.getRefreshToken())) {
            throw new AuthenticationFailedException("Invalid refresh token");
        }

        // Extract session ID from token
        String sessionId = jwtTokenService.getSessionIdFromToken(request.getRefreshToken());
        UUID sessionUuid = UUID.fromString(sessionId);

        // Find session
        UserSession session = sessionRepository.findById(sessionUuid)
                .orElseThrow(() -> new AuthenticationFailedException("Session not found"));

        // Validate session
        if (!session.isActive() || !session.getRefreshToken().equals(request.getRefreshToken())) {
            throw new AuthenticationFailedException("Invalid or expired session");
        }

        // Get user
        User user = session.getUser();

        // Check account status
        validateAccountStatus(user);

        // Generate new access token
        String newAccessToken = jwtTokenService.generateAccessToken(user, sessionId);

        // Update session
        session.setSessionToken(newAccessToken);
        session.updateLastAccessed();
        sessionRepository.save(session);

        // Create token info
        Map<String, Object> tokenInfo = jwtTokenService.createTokenInfo(newAccessToken, request.getRefreshToken());

        logger.info("Token refreshed successfully for user ID: {}", user.getId());

        return AuthenticationResponse.builder()
                .success(true)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .companyId(user.getCompanyId())
                .emailVerified(user.getEmailVerified())
                .sessionId(session.getId())
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .tokenType("Bearer")
                .expiresIn((Long) tokenInfo.get("expiresIn"))
                .refreshExpiresIn((Long) tokenInfo.get("refreshExpiresIn"))
                .requiresPasswordChange(false)
                .message("Token refreshed successfully")
                .build();
    }

    /**
     * Logout user and invalidate session
     */
    public void logout(String sessionToken, boolean logoutAllSessions) {
        logger.info("Logout attempt for session");

        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Session token is required");
        }

        try {
            // Find session by token
            UserSession session = sessionRepository.findBySessionToken(sessionToken)
                    .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

            if (logoutAllSessions) {
                // Get all active sessions for this user
                List<UserSession> userSessions = sessionRepository
                        .findActiveSessionsByUserId(session.getUser().getId(), LocalDateTime.now());

                // Revoke all active sessions for the user
                sessionRepository.revokeAllActiveSessionsByUserId(session.getUser().getId());

                // Remove all sessions from cache
                for (UserSession userSession : userSessions) {
                    if (userSession.getSessionToken() != null) {
                        cacheService.evictSession(userSession.getSessionToken());
                    }
                }

                logger.info("All sessions revoked for user ID: {}", session.getUser().getId());
            } else {
                // Revoke only current session
                session.revoke();
                sessionRepository.save(session);

                // Remove from cache
                cacheService.evictSession(sessionToken);

                logger.info("Session revoked: {}", session.getId());
            }

            // Optional: Clear user cache if logging out from all sessions
            if (logoutAllSessions) {
                cacheService.evictUser(session.getUser().getId());
            }

        } catch (ResourceNotFoundException e) {
            logger.warn("Logout attempted with invalid session token");
            throw e;
        } catch (Exception e) {
            logger.error("Error during logout: {}", e.getMessage());
            throw new RuntimeException("Logout failed", e);
        }
    }
    /**
     * Validate session token
     */
    @Transactional(readOnly = true)
    public boolean validateSession(String sessionToken) {
        if (sessionToken == null) {
            return false;
        }

        try {
            // Validate JWT token
            if (!jwtTokenService.validateAccessToken(sessionToken)) {
                return false;
            }

            // Find session in database
            UserSession session = sessionRepository.findBySessionToken(sessionToken)
                    .orElse(null);

            if (session == null || !session.isActive()) {
                return false;
            }

            // Update last accessed time
            session.updateLastAccessed();
            sessionRepository.save(session);

            return true;
        } catch (Exception e) {
            logger.error("Error validating session: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get user from session token
     */
    @Transactional(readOnly = true)
    public User getUserFromToken(String sessionToken) {
        if (sessionToken == null) {
            throw new IllegalArgumentException("Session token is required");
        }

        // Validate token
        if (!jwtTokenService.validateAccessToken(sessionToken)) {
            throw new AuthenticationFailedException("Invalid session token");
        }

        // Find session
        UserSession session = sessionRepository.findBySessionToken(sessionToken)
                .orElseThrow(() -> new AuthenticationFailedException("Session not found"));

        if (!session.isActive()) {
            throw new AuthenticationFailedException("Session is not active");
        }

        return session.getUser();
    }

    /**
     * Change password
     */
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        logger.info("Password change attempt for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new AuthenticationFailedException("Current password is incorrect");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setLastPasswordChange(LocalDateTime.now());
        user.setForcePasswordChange(false);

        userRepository.save(user);

        // Revoke all other sessions to force re-login
        sessionRepository.revokeAllActiveSessionsByUserId(userId);

        logger.info("Password changed successfully for user ID: {}", userId);
    }

    /**
     * Force password change
     */
    public void forcePasswordChange(UUID userId) {
        logger.info("Forcing password change for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setForcePasswordChange(true);
        userRepository.save(user);

        // Revoke all active sessions
        sessionRepository.revokeAllActiveSessionsByUserId(userId);

        logger.info("Password change forced for user ID: {}", userId);
    }

    /**
     * Reset password with token
     */
    public void resetPassword(String resetToken, String newPassword) {
        logger.info("Password reset attempt with token");

        User user = userRepository.findByPasswordResetToken(resetToken)
                .orElseThrow(() -> new AuthenticationFailedException("Invalid reset token"));

        if (user.isPasswordResetExpired()) {
            throw new AuthenticationFailedException("Reset token has expired");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setLastPasswordChange(LocalDateTime.now());
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);
        user.setForcePasswordChange(false);

        userRepository.save(user);

        // Revoke all active sessions
        sessionRepository.revokeAllActiveSessionsByUserId(user.getId());

        logger.info("Password reset successfully for user ID: {}", user.getId());
    }

    /**
     * Request password reset
     */
    public void requestPasswordReset(String email) {
        logger.info("Password reset request for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpiry(LocalDateTime.now().plusHours(1)); // 1 hour expiry

        userRepository.save(user);

        // Send reset email (you'll need to implement EmailService)
        // emailService.sendPasswordReset(user.getEmail(), user.getFullName(), resetToken);

        logger.info("Password reset token generated for user ID: {}", user.getId());
    }

    // Helper methods

    private void validateAccountStatus(User user) {
        if (user.isAccountLocked()) {
            throw new AuthenticationFailedException("Account is temporarily locked. Try again later.");
        }

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new AuthenticationFailedException("Account is inactive");
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new AuthenticationFailedException("Account is suspended");
        }

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new AuthenticationFailedException("Account has expired");
        }

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AuthenticationFailedException("Account is locked. Contact administrator.");
        }
    }

    private void handleFailedLogin(User user) {
        user.incrementFailedLoginAttempts();

        if (user.getFailedLoginAttempts() >= maxFailedAttempts) {
            user.lockAccount(lockoutDuration);  // Pass the long duration directly
            logger.warn("Account locked for user ID: {} due to {} failed login attempts",
                    user.getId(), user.getFailedLoginAttempts());
        }

        userRepository.save(user);

        logger.warn("Failed login attempt for user ID: {}. Attempts: {}",
                user.getId(), user.getFailedLoginAttempts());
    }

    private void manageConcurrentSessions(User user) {
        long activeSessionCount = sessionRepository.countActiveSessionsByUserId(user.getId(), LocalDateTime.now());

        if (activeSessionCount >= maxConcurrentSessions) {
            // Find oldest session and revoke it
            sessionRepository.findActiveSessionsByUserId(user.getId(), LocalDateTime.now())
                    .stream()
                    .findFirst()
                    .ifPresent(oldSession -> {
                        oldSession.revoke();
                        sessionRepository.save(oldSession);
                        logger.info("Revoked oldest session for user ID: {} due to concurrent session limit",
                                user.getId());
                    });
        }
    }

    /**
     * Get active sessions for user
     */
    @Transactional(readOnly = true)
    public long getActiveSessionCount(UUID userId) {
        return sessionRepository.countActiveSessionsByUserId(userId, LocalDateTime.now());
    }

    /**
     * Revoke specific session
     */
    public void revokeSession(UUID sessionId) {
        UserSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        session.revoke();
        sessionRepository.save(session);

        logger.info("Session revoked: {}", sessionId);
    }

    /**
     * Check if user needs to verify email
     */
    public boolean requiresEmailVerification(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return !user.getEmailVerified();
    }
}
