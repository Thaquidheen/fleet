package com.fleetmanagement.userservice.service;

import com.fleetmanagement.userservice.domain.entity.EmailVerificationToken;
import com.fleetmanagement.userservice.domain.entity.User;
import com.fleetmanagement.userservice.domain.enums.TokenType;
import com.fleetmanagement.userservice.exception.InvalidTokenException;
import com.fleetmanagement.userservice.exception.TokenExpiredException;
import com.fleetmanagement.userservice.exception.UserNotFoundException;
import com.fleetmanagement.userservice.repository.EmailVerificationTokenRepository;
import com.fleetmanagement.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Email Verification Service
 *
 * Service for managing email verification process including:
 * - Token generation and validation
 * - Email verification workflow
 * - Token expiration and cleanup
 * - Resend verification functionality
 */
@Service
@Transactional
public class EmailVerificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailVerificationService.class);

    private static final String VERIFICATION_RATE_LIMIT_PREFIX = "email_verification_rate_limit:";
    private static final int MAX_VERIFICATION_ATTEMPTS_PER_HOUR = 3;
    private static final int TOKEN_LENGTH = 32;

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheService cacheService;

    @Value("${app.email.verification.token-expiry-hours:24}")
    private int tokenExpiryHours;

    @Value("${app.email.verification.base-url:http://localhost:3000}")
    private String baseUrl;

    @Value("${app.email.verification.enabled:true}")
    private boolean emailVerificationEnabled;

    @Autowired
    public EmailVerificationService(UserRepository userRepository,
                                    EmailVerificationTokenRepository tokenRepository,
                                    EmailService emailService,
                                    RedisTemplate<String, Object> redisTemplate,
                                    CacheService cacheService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.redisTemplate = redisTemplate;
        this.cacheService = cacheService;
    }

    /**
     * Generate and send email verification token
     */
    public void sendVerificationEmail(UUID userId) {
        logger.info("Sending verification email for user: {}", userId);

        if (!emailVerificationEnabled) {
            logger.info("Email verification is disabled, skipping for user: {}", userId);
            return;
        }

        // Check rate limiting
        if (isRateLimited(userId)) {
            throw new IllegalStateException("Too many verification emails sent. Please wait before requesting another.");
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("User not found: " + userId);
        }

        User user = userOpt.get();

        if (user.isEmailVerified()) {
            logger.info("User {} email is already verified", userId);
            return;
        }

        // Invalidate any existing tokens for this user
        invalidateExistingTokens(userId);

        // Generate new verification token
        String token = generateSecureToken();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(tokenExpiryHours);

        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .token(token)
                .tokenType(TokenType.EMAIL_VERIFICATION)
                .expiryDate(expiryDate)
                .isUsed(false)
                .createdAt(LocalDateTime.now())
                .build();

        tokenRepository.save(verificationToken);

        // Send verification email
        String verificationUrl = buildVerificationUrl(token);
        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), verificationUrl);

        // Update rate limiting
        updateRateLimit(userId);

        logger.info("Verification email sent successfully for user: {}", userId);
    }

    /**
     * Verify email using token
     */
    public void verifyEmail(String token) {
        logger.info("Verifying email with token: {}", maskToken(token));

        Optional<EmailVerificationToken> tokenOpt = tokenRepository.findByTokenAndTokenType(
                token, TokenType.EMAIL_VERIFICATION);

        if (tokenOpt.isEmpty()) {
            throw new InvalidTokenException("Invalid verification token");
        }

        EmailVerificationToken verificationToken = tokenOpt.get();

        if (verificationToken.isUsed()) {
            throw new InvalidTokenException("Verification token has already been used");
        }

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Verification token has expired");
        }

        Optional<User> userOpt = userRepository.findById(verificationToken.getUserId());
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("User not found for verification token");
        }

        User user = userOpt.get();

        // Mark user as verified
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Mark token as used
        verificationToken.setUsed(true);
        verificationToken.setUsedAt(LocalDateTime.now());
        tokenRepository.save(verificationToken);

        // Clear rate limiting for this user
        clearRateLimit(verificationToken.getUserId());

        logger.info("Email verification completed successfully for user: {}", user.getId());
    }

    /**
     * Resend verification email
     */
    public void resendVerificationEmail(UUID userId) {
        logger.info("Resending verification email for user: {}", userId);

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("User not found: " + userId);
        }

        User user = userOpt.get();

        if (user.isEmailVerified()) {
            throw new IllegalStateException("Email is already verified");
        }

        // Use the same logic as sending initial verification email
        sendVerificationEmail(userId);
    }

    /**
     * Check if email verification is required for user
     */
    @Transactional(readOnly = true)
    public boolean isVerificationRequired(UUID userId) {
        if (!emailVerificationEnabled) {
            return false;
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }

        return !userOpt.get().isEmailVerified();
    }

    /**
     * Get verification status for user
     */
    @Transactional(readOnly = true)
    public EmailVerificationStatus getVerificationStatus(UUID userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("User not found: " + userId);
        }

        User user = userOpt.get();

        if (user.isEmailVerified()) {
            return EmailVerificationStatus.builder()
                    .isVerified(true)
                    .verifiedAt(user.getEmailVerifiedAt())
                    .email(user.getEmail())
                    .canResend(false)
                    .message("Email is verified")
                    .build();
        }

        // Check if there's a pending verification token
        Optional<EmailVerificationToken> activeToken = tokenRepository
                .findActiveTokenByUserIdAndType(userId, TokenType.EMAIL_VERIFICATION);

        boolean canResend = !isRateLimited(userId);
        LocalDateTime tokenExpiryDate = activeToken.map(EmailVerificationToken::getExpiryDate).orElse(null);

        return EmailVerificationStatus.builder()
                .isVerified(false)
                .verifiedAt(null)
                .email(user.getEmail())
                .canResend(canResend)
                .hasPendingToken(activeToken.isPresent())
                .tokenExpiryDate(tokenExpiryDate)
                .message(canResend ? "Verification email can be sent" : "Rate limited - please wait before requesting another email")
                .build();
    }

    /**
     * Clean up expired tokens (scheduled task)
     */
    @Transactional
    public int cleanupExpiredTokens() {
        logger.info("Cleaning up expired email verification tokens");

        LocalDateTime cutoffDate = LocalDateTime.now().minusHours(tokenExpiryHours * 2);
        int deletedCount = tokenRepository.deleteExpiredTokens(cutoffDate);

        logger.info("Cleaned up {} expired email verification tokens", deletedCount);
        return deletedCount;
    }

    // Private helper methods

    private boolean isRateLimited(UUID userId) {
        String rateLimitKey = VERIFICATION_RATE_LIMIT_PREFIX + userId;
        Integer attempts = (Integer) redisTemplate.opsForValue().get(rateLimitKey);
        return attempts != null && attempts >= MAX_VERIFICATION_ATTEMPTS_PER_HOUR;
    }

    private void updateRateLimit(UUID userId) {
        String rateLimitKey = VERIFICATION_RATE_LIMIT_PREFIX + userId;
        Integer currentAttempts = (Integer) redisTemplate.opsForValue().get(rateLimitKey);

        if (currentAttempts == null) {
            redisTemplate.opsForValue().set(rateLimitKey, 1, 1, TimeUnit.HOURS);
        } else {
            redisTemplate.opsForValue().increment(rateLimitKey);
        }
    }

    private void clearRateLimit(UUID userId) {
        String rateLimitKey = VERIFICATION_RATE_LIMIT_PREFIX + userId;
        redisTemplate.delete(rateLimitKey);
    }

    private void invalidateExistingTokens(UUID userId) {
        List<EmailVerificationToken> existingTokens = tokenRepository
                .findActiveTokensByUserIdAndType(userId, TokenType.EMAIL_VERIFICATION);

        for (EmailVerificationToken token : existingTokens) {
            token.setUsed(true);
            token.setUsedAt(LocalDateTime.now());
        }

        if (!existingTokens.isEmpty()) {
            tokenRepository.saveAll(existingTokens);
            logger.debug("Invalidated {} existing tokens for user: {}", existingTokens.size(), userId);
        }
    }

    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[TOKEN_LENGTH];
        random.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private String buildVerificationUrl(String token) {
        return String.format("%s/verify-email?token=%s", baseUrl, token);
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 8) {
            return "***";
        }
        return token.substring(0, 4) + "***" + token.substring(token.length() - 4);
    }

    // Response DTO
    @lombok.Data
    @lombok.Builder
    public static class EmailVerificationStatus {
        private boolean isVerified;
        private LocalDateTime verifiedAt;
        private String email;
        private boolean canResend;
        private boolean hasPendingToken;
        private LocalDateTime tokenExpiryDate;
        private String message;
    }
}