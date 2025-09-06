// UserService.java
package com.fleetmanagement.userservice.service;

import com.fleetmanagement.userservice.domain.entity.User;
import com.fleetmanagement.userservice.domain.enums.UserRole;
import com.fleetmanagement.userservice.domain.enums.UserStatus;
import com.fleetmanagement.userservice.dto.response.UserResponse;
import com.fleetmanagement.userservice.exception.ResourceNotFoundException;
import com.fleetmanagement.userservice.exception.UserAlreadyExistsException;
import com.fleetmanagement.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final CacheService cacheService;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService,
                       CacheService cacheService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.cacheService = cacheService;
    }

    /**
     * Create a new user
     */
    public UserResponse createUser(CreateUserRequest request, UUID createdBy) {
        logger.info("Creating new user with username: {}", request.getUsername());

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
        user.setRole(request.getRole());
        user.setCompanyId(request.getCompanyId());
        user.setEmployeeId(request.getEmployeeId());
        user.setDepartment(request.getDepartment());
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.setEmailVerified(false);
        user.setCreatedBy(createdBy);
        user.setUpdatedBy(createdBy);
        user.setTimezone(request.getTimezone() != null ? request.getTimezone() : "UTC");
        user.setLanguage(request.getLanguage() != null ? request.getLanguage() : "en");

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

        logger.info("User created successfully with ID: {}", savedUser.getId());
        return convertToUserResponse(savedUser);
    }

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        User user = findUserById(userId);
        return convertToUserResponse(user);
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
    public UserResponse updateUser(UUID userId, UpdateUserRequest request, UUID updatedBy) {
        logger.info("Updating user with ID: {}", userId);

        User user = findUserById(userId);

        // Validate uniqueness if username or email changed
        if (!user.getUsername().equals(request.getUsername()) ||
                !user.getEmail().equals(request.getEmail())) {
            validateUserUniqueness(request.getUsername(), request.getEmail(), userId);
        }

        // Update fields
        user.setUsername(request.getUsername());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmployeeId(request.getEmployeeId());
        user.setDepartment(request.getDepartment());
        user.setUpdatedBy(updatedBy);

        // Handle email change
        if (!user.getEmail().equals(request.getEmail())) {
            user.setEmail(request.getEmail());
            user.setEmailVerified(false);
            user.setStatus(UserStatus.PENDING_VERIFICATION);

            // Generate new verification token
            String verificationToken = UUID.randomUUID().toString();
            user.setEmailVerificationToken(verificationToken);
            user.setEmailVerificationExpiry(LocalDateTime.now().plusHours(24));

            // Send verification email
            try {
                emailService.sendEmailVerification(user.getEmail(),
                        user.getFullName(),
                        verificationToken);
            } catch (Exception e) {
                logger.error("Failed to send verification email to {}: {}",
                        user.getEmail(), e.getMessage());
            }
        }

        // Update optional fields
        if (StringUtils.hasText(request.getTimezone())) {
            user.setTimezone(request.getTimezone());
        }
        if (StringUtils.hasText(request.getLanguage())) {
            user.setLanguage(request.getLanguage());
        }
        if (StringUtils.hasText(request.getProfileImageUrl())) {
            user.setProfileImageUrl(request.getProfileImageUrl());
        }
        if (StringUtils.hasText(request.getNotes())) {
            user.setNotes(request.getNotes());
        }

        User savedUser = userRepository.save(user);

        // Clear cache
        cacheService.evictUser(userId);

        logger.info("User updated successfully with ID: {}", savedUser.getId());
        return convertToUserResponse(savedUser);
    }

    /**
     * Update user role
     */
    public UserResponse updateUserRole(UUID userId, UserRole newRole, UUID updatedBy) {
        logger.info("Updating role for user ID: {} to {}", userId, newRole);

        User user = findUserById(userId);
        user.setRole(newRole);
        user.setUpdatedBy(updatedBy);

        User savedUser = userRepository.save(user);

        // Clear cache
        cacheService.evictUser(userId);

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
            stats.put("pendingUsers", userRepository.countByCompanyIdAndStatus(companyId, UserStatus.PENDING_VERIFICATION));

            // Role statistics
            for (UserRole role : UserRole.values()) {
                stats.put(role.name().toLowerCase() + "Count",
                        userRepository.countByCompanyIdAndRole(companyId, role));
            }
        } else {
            stats.put("totalUsers", userRepository.count());
        }

        return stats;
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