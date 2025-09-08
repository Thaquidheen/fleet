package com.fleetmanagement.userservice.service;

import com.fleetmanagement.userservice.domain.entity.User;
import com.fleetmanagement.userservice.dto.request.ChangePasswordRequest;
import com.fleetmanagement.userservice.dto.request.ResetPasswordRequest;
import com.fleetmanagement.userservice.exception.ResourceNotFoundException;
import com.fleetmanagement.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@Transactional
public class PasswordService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final CacheService cacheService;

    @Value("${app.security.password.min-length:8}")
    private int minPasswordLength;

    @Value("${app.security.password.require-special-chars:true}")
    private boolean requireSpecialChars;

    @Value("${app.security.password.require-numbers:true}")
    private boolean requireNumbers;

    @Value("${app.security.password.require-uppercase:true}")
    private boolean requireUppercase;

    // Password complexity patterns
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

    @Autowired
    public PasswordService(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           EmailService emailService,
                           CacheService cacheService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.cacheService = cacheService;
    }

    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Validate new password
        validatePasswordComplexity(request.getNewPassword());

        // Check password is different from current
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setFailedLoginAttempts(0); // Reset failed attempts

        userRepository.save(user);

        // Clear user cache
        cacheService.evictUser(userId);

        logger.info("Password changed successfully for user: {}", userId);
    }

    public void initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpiry(LocalDateTime.now().plusHours(1)); // 1 hour expiry

        userRepository.save(user);

        // Send reset email
        emailService.sendPasswordResetEmail(user, resetToken);

        logger.info("Password reset initiated for user: {}", user.getId());
    }

    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByPasswordResetToken(request.getToken())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid reset token"));

        // Check token expiry
        if (user.getPasswordResetExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Password reset token has expired");
        }

        // Validate new password
        validatePasswordComplexity(request.getNewPassword());

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setFailedLoginAttempts(0);

        userRepository.save(user);

        // Clear user cache
        cacheService.evictUser(user.getId());

        logger.info("Password reset successfully for user: {}", user.getId());
    }

    public void validatePasswordComplexity(String password) {
        if (password == null || password.length() < minPasswordLength) {
            throw new IllegalArgumentException("Password must be at least " + minPasswordLength + " characters long");
        }

        if (requireUppercase && !UPPERCASE_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }

        if (!LOWERCASE_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        }

        if (requireNumbers && !DIGIT_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("Password must contain at least one number");
        }

        if (requireSpecialChars && !SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("Password must contain at least one special character");
        }
    }

    public boolean isPasswordExpired(User user) {
        if (user.getPasswordChangedAt() == null) {
            return false; // No expiry for passwords that haven't been changed
        }

        // Check if password is older than 90 days
        return user.getPasswordChangedAt().isBefore(LocalDateTime.now().minusDays(90));
    }
}