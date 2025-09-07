// UserService.java - Complete Version with All Dependencies
package com.fleetmanagement.userservice.service;

import com.fleetmanagement.userservice.domain.entity.User;
import com.fleetmanagement.userservice.domain.enums.UserRole;
import com.fleetmanagement.userservice.domain.enums.UserStatus;
import com.fleetmanagement.userservice.dto.request.CreateUserRequest;
import com.fleetmanagement.userservice.dto.request.UpdateUserRequest;
import com.fleetmanagement.userservice.dto.request.UserSearchRequest;
import com.fleetmanagement.userservice.dto.response.UserResponse;
import com.fleetmanagement.userservice.exception.ResourceNotFoundException;
import com.fleetmanagement.userservice.exception.UserAlreadyExistsException;
import com.fleetmanagement.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final CacheService cacheService;
    private final PasswordService passwordService;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService,
                       CacheService cacheService,
                       PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.cacheService = cacheService;
        this.passwordService = passwordService;
    }

    /**
     * Create a new user
     */
    public UserResponse createUser(CreateUserRequest request, UUID createdBy) {
        logger.info("Creating new user with username: {}", request.getUsername());

        // Validate password strength
        if (!passwordService.isValidPassword(request.getPassword())) {
            throw new IllegalArgumentException("Password does not meet requirements: " +
                    passwordService.getPasswordRequirements());
        }

        // Validate uniqueness
        validateUserUniqueness(request.getUsername(), request.getEmail(), null);

        // Create user entity
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(request.getRole() != null ? request.getRole() : UserRole.DRIVER);
        user.setCompanyId(request.getCompanyId());
        user.setEmployeeId(request.getEmployeeId());
        user.setDepartment(request.getDepartment());
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(false);
        user.setCreatedBy(createdBy);
        user.setUpdatedBy(createdBy);
        user.setTimezone(request.getTimezone() != null ? request.getTimezone() : "UTC");
        user.setLanguage(request.getLanguage() != null ? request.getLanguage() : "en");
        user.setLastPasswordChange(LocalDateTime.now());

        // Generate email verification token
        String verificationToken = UUID.randomUUID().toString();
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationExpiry(LocalDateTime.now().plusHours(24));

        // Save user
        User savedUser = userRepository.save(user);

        // Send verification email
        try {
            emailService.sendEmailVerification(savedUser.getEmail(),
                    savedUser.getFullName(),
                    verificationToken);
        } catch (Exception e) {
            logger.error("Failed to send verification email to {}: {}",
                    savedUser.getEmail(), e.getMessage());
        }

        // Cache the user
        UserResponse response = convertToUserResponse(savedUser);
        cacheService.cacheUser(savedUser.getId(), response);

        logger.info("User created successfully with ID: {}", savedUser.getId());
        return response;
    }

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        // Check cache first
        Object cachedUser = cacheService.getCachedUser(userId);
        if (cachedUser instanceof UserResponse) {
            return (UserResponse) cachedUser;
        }

        User user = findUserById(userId);
        UserResponse response = convertToUserResponse(user);

        // Cache the result
        cacheService.cacheUser(userId, response);

        return response;
    }

    /**
     * Get user by username
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return convertToUserResponse(user);
    }

    /**
     * Get user by email
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return convertToUserResponse(user);
    }

    /**
     * Update user
     */
    public UserResponse updateUserProfile(UUID userId, UpdateUserRequest request) {
        logger.info("Updating user profile for user ID: {}", userId);

        User user = findUserById(userId);

        // Validate uniqueness if username or email changed
        if (request.getUsername() != null && !user.getUsername().equals(request.getUsername())) {
            validateUserUniqueness(request.getUsername(), user.getEmail(), userId);
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !user.getEmail().equals(request.getEmail())) {
            validateUserUniqueness(user.getUsername(), request.getEmail(), userId);
            user.setEmail(request.getEmail());
            // Note: In a complete implementation, you might want to require email verification
            // user.setEmailVerified(false);
        }

        // Update allowed profile fields
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getEmployeeId() != null) {
            user.setEmployeeId(request.getEmployeeId());
        }
        if (request.getDepartment() != null) {
            user.setDepartment(request.getDepartment());
        }
        if (request.getTimezone() != null) {
            user.setTimezone(request.getTimezone());
        }
        if (request.getLanguage() != null) {
            user.setLanguage(request.getLanguage());
        }
        if (request.getProfileImageUrl() != null) {
            user.setProfileImageUrl(request.getProfileImageUrl());
        }
        if (request.getNotes() != null) {
            user.setNotes(request.getNotes());
        }

        // Save user
        User savedUser = userRepository.save(user);

        // Update cache
        UserResponse response = convertToUserResponse(savedUser);
        cacheService.cacheUser(savedUser.getId(), response);

        logger.info("User profile updated successfully for user ID: {}", userId);
        return response;
    }

    /**
     * Get user by ID (already exists but ensure it's public)
     */
//    @Transactional(readOnly = true)
//    public UserResponse getUserById(UUID userId) {
//        // Check cache first
//        Object cachedUser = cacheService.getCachedUser(userId);
//        if (cachedUser instanceof UserResponse) {
//            return (UserResponse) cachedUser;
//        }
//
//        User user = findUserById(userId);
//        UserResponse response = convertToUserResponse(user);
//
//        // Cache the result
//        cacheService.cacheUser(userId, response);
//
//        return response;
//    }
//    /**
    public UserResponse updateUserRole(UUID userId, UserRole newRole, UUID updatedBy) {
        logger.info("Updating role for user ID: {} to {}", userId, newRole);

        User user = findUserById(userId);
        user.setRole(newRole);
        user.setUpdatedBy(updatedBy);

        User savedUser = userRepository.save(user);

        // Clear cache
        cacheService.evictUser(userId);
        cacheService.evictUserPermissions(userId);

        logger.info("User role updated successfully for ID: {}", savedUser.getId());
        return convertToUserResponse(savedUser);
    }

    /**
     * Update user status
     */
    public UserResponse updateUserStatus(UUID userId, UserStatus newStatus, UUID updatedBy) {
        logger.info("Updating status for user ID: {} to {}", userId, newStatus);

        User user = findUserById(userId);
        user.setStatus(newStatus);
        user.setUpdatedBy(updatedBy);

        // If activating user, reset failed login attempts
        if (newStatus == UserStatus.ACTIVE) {
            user.resetFailedLoginAttempts();
        }

        User savedUser = userRepository.save(user);

        // Clear cache
        cacheService.evictUser(userId);

        logger.info("User status updated successfully for ID: {}", savedUser.getId());
        return convertToUserResponse(savedUser);
    }

    /**
     * Delete user (soft delete by changing status)
     */
    public void deleteUser(UUID userId, UUID deletedBy) {
        logger.info("Deleting user with ID: {}", userId);

        User user = findUserById(userId);
        user.setStatus(UserStatus.INACTIVE);
        user.setUpdatedBy(deletedBy);

        userRepository.save(user);

        // Clear cache
        cacheService.evictUser(userId);
        cacheService.evictUserPermissions(userId);

        logger.info("User deleted successfully with ID: {}", userId);
    }

    /**
     * Get users by company
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsersByCompany(UUID companyId, Pageable pageable) {
        Page<User> users = userRepository.findByCompanyId(companyId, pageable);
        return users.map(this::convertToUserResponse);
    }

    /**
     * Search users
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(UserSearchRequest request, Pageable pageable) {
        Specification<User> spec = buildUserSearchSpecification(request);
        Page<User> users = userRepository.findAll(spec, pageable);
        return users.map(this::convertToUserResponse);
    }

    /**
     * Get users by role
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsersByRole(UserRole role, Pageable pageable) {
        Page<User> users = userRepository.findByRole(role, pageable);
        return users.map(this::convertToUserResponse);
    }

    /**
     * Get company users by role
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getCompanyUsersByRole(UUID companyId, UserRole role, Pageable pageable) {
        Page<User> users = userRepository.findByCompanyIdAndRole(companyId, role, pageable);
        return users.map(this::convertToUserResponse);
    }

    /**
     * Verify email
     */
    public UserResponse verifyEmail(String verificationToken) {
        logger.info("Verifying email with token: {}", verificationToken);

        User user = userRepository.findByEmailVerificationToken(verificationToken)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid verification token"));

        if (user.isEmailVerificationExpired()) {
            throw new IllegalArgumentException("Email verification token has expired");
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationExpiry(null);
        user.setStatus(UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);

        // Clear cache
        cacheService.evictUser(savedUser.getId());

        logger.info("Email verified successfully for user ID: {}", savedUser.getId());
        return convertToUserResponse(savedUser);
    }

    /**
     * Resend email verification
     */
    public void resendEmailVerification(String email) {
        logger.info("Resending email verification for: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (user.getEmailVerified()) {
            throw new IllegalArgumentException("Email is already verified");
        }

        // Generate new verification token
        String verificationToken = UUID.randomUUID().toString();
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationExpiry(LocalDateTime.now().plusHours(24));

        userRepository.save(user);

        // Send verification email
        try {
            emailService.sendEmailVerification(user.getEmail(),
                    user.getFullName(),
                    verificationToken);
            logger.info("Email verification resent successfully to: {}", email);
        } catch (Exception e) {
            logger.error("Failed to resend verification email to {}: {}", email, e.getMessage());
            throw new RuntimeException("Failed to send verification email", e);
        }

        // Clear cache
        cacheService.evictUser(user.getId());
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

        // Send reset email
        try {
            emailService.sendPasswordReset(user.getEmail(), user.getFullName(), resetToken);
            logger.info("Password reset email sent to: {}", email);
        } catch (Exception e) {
            logger.error("Failed to send password reset email to {}: {}", email, e.getMessage());
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    /**
     * Reset password with token
     */
    public void resetPassword(String resetToken, String newPassword) {
        logger.info("Password reset attempt with token");

        // Validate password strength
        if (!passwordService.isValidPassword(newPassword)) {
            throw new IllegalArgumentException("Password does not meet requirements: " +
                    passwordService.getPasswordRequirements());
        }

        User user = userRepository.findByPasswordResetToken(resetToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid reset token"));

        if (user.isPasswordResetExpired()) {
            throw new IllegalArgumentException("Reset token has expired");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setLastPasswordChange(LocalDateTime.now());
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);
        user.setForcePasswordChange(false);

        userRepository.save(user);

        // Clear cache
        cacheService.evictUser(user.getId());

        logger.info("Password reset successfully for user ID: {}", user.getId());
    }

    /**
     * Get user statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserStatistics(UUID companyId) {
        Map<String, Object> stats = new HashMap<>();

        if (companyId != null) {
            stats.put("totalUsers", userRepository.countByCompanyId(companyId));
            stats.put("activeUsers", userRepository.countByCompanyIdAndStatus(companyId, UserStatus.ACTIVE));
            stats.put("inactiveUsers", userRepository.countByCompanyIdAndStatus(companyId, UserStatus.INACTIVE));
            stats.put("lockedUsers", userRepository.countByCompanyIdAndStatus(companyId, UserStatus.LOCKED));
            stats.put("suspendedUsers", userRepository.countByCompanyIdAndStatus(companyId, UserStatus.SUSPENDED));

            // Role statistics
            for (UserRole role : UserRole.values()) {
                stats.put(role.name().toLowerCase() + "Count",
                        userRepository.countByCompanyIdAndRole(companyId, role));
            }

            // Activity statistics
            LocalDateTime lastWeek = LocalDateTime.now().minusDays(7);
            stats.put("activeUsersLastWeek", userRepository.countActiveUsersByCompanySince(companyId, lastWeek));
        } else {
            stats.put("totalUsers", userRepository.count());
            LocalDateTime lastWeek = LocalDateTime.now().minusDays(7);
            stats.put("activeUsersLastWeek", userRepository.countActiveUsersSince(lastWeek));
        }

        return stats;
    }

    /**
     * Check if user can access another user's data
     */
    public boolean canAccessUser(String currentUserIdStr, UUID targetUserId) {
        try {
            UUID currentUserId = UUID.fromString(currentUserIdStr);

            // Users can always access their own data
            if (currentUserId.equals(targetUserId)) {
                return true;
            }

            User currentUser = findUserById(currentUserId);

            // Super admins can access any user
            if (currentUser.getRole() == UserRole.SUPER_ADMIN) {
                return true;
            }

            // Company admins can access users in their company
            if (currentUser.getRole() == UserRole.COMPANY_ADMIN) {
                User targetUser = findUserById(targetUserId);
                return currentUser.getCompanyId().equals(targetUser.getCompanyId());
            }

            return false;
        } catch (Exception e) {
            logger.error("Error checking user access: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if user can modify another user
     */
    public boolean canModifyUser(String currentUserIdStr, UUID targetUserId) {
        try {
            UUID currentUserId = UUID.fromString(currentUserIdStr);

            // Users can modify their own data (with restrictions)
            if (currentUserId.equals(targetUserId)) {
                return true;
            }

            User currentUser = findUserById(currentUserId);

            // Super admins can modify any user
            if (currentUser.getRole() == UserRole.SUPER_ADMIN) {
                return true;
            }

            // Company admins can modify users in their company (except other admins)
            if (currentUser.getRole() == UserRole.COMPANY_ADMIN) {
                User targetUser = findUserById(targetUserId);
                return currentUser.getCompanyId().equals(targetUser.getCompanyId()) &&
                        targetUser.getRole() != UserRole.SUPER_ADMIN &&
                        targetUser.getRole() != UserRole.COMPANY_ADMIN;
            }

            return false;
        } catch (Exception e) {
            logger.error("Error checking user modification rights: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if user belongs to company
     */
    public boolean belongsToCompany(String userIdStr, UUID companyId) {
        try {
            UUID userId = UUID.fromString(userIdStr);
            User user = findUserById(userId);
            return user.getCompanyId().equals(companyId);
        } catch (Exception e) {
            logger.error("Error checking company membership: {}", e.getMessage());
            return false;
        }
    }

    // Helper methods

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    private void validateUserUniqueness(String username, String email, UUID excludeUserId) {
        if (excludeUserId != null) {
            if (userRepository.existsByUsernameAndIdNot(username, excludeUserId)) {
                throw new UserAlreadyExistsException("Username already exists: " + username);
            }
            if (userRepository.existsByEmailAndIdNot(email, excludeUserId)) {
                throw new UserAlreadyExistsException("Email already exists: " + email);
            }
        } else {
            if (userRepository.existsByUsername(username)) {
                throw new UserAlreadyExistsException("Username already exists: " + username);
            }
            if (userRepository.existsByEmail(email)) {
                throw new UserAlreadyExistsException("Email already exists: " + email);
            }
        }
    }

    private Specification<User> buildUserSearchSpecification(UserSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.getSearchTerm())) {
                String searchPattern = "%" + request.getSearchTerm().toLowerCase() + "%";
                Predicate searchPredicate = criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("employeeId")), searchPattern)
                );
                predicates.add(searchPredicate);
            }

            if (request.getCompanyId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("companyId"), request.getCompanyId()));
            }

            if (request.getRole() != null) {
                predicates.add(criteriaBuilder.equal(root.get("role"), request.getRole()));
            }

            if (request.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), request.getStatus()));
            }

            if (request.getEmailVerified() != null) {
                predicates.add(criteriaBuilder.equal(root.get("emailVerified"), request.getEmailVerified()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setFullName(user.getFullName());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setCompanyId(user.getCompanyId());
        response.setEmployeeId(user.getEmployeeId());
        response.setDepartment(user.getDepartment());
        response.setEmailVerified(user.getEmailVerified());
        response.setLastLogin(user.getLastLogin());
        response.setProfileImageUrl(user.getProfileImageUrl());
        response.setTimezone(user.getTimezone());
        response.setLanguage(user.getLanguage());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}