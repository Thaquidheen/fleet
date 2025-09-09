package com.fleetmanagement.companyservice.service;

import com.fleetmanagement.companyservice.client.UserServiceClient;
import com.fleetmanagement.companyservice.domain.entity.Company;
import com.fleetmanagement.companyservice.domain.enums.SubscriptionPlan;
import com.fleetmanagement.companyservice.dto.request.BulkUserCreateRequest;
import com.fleetmanagement.companyservice.dto.request.BulkUserUpdateRequest;
import com.fleetmanagement.companyservice.dto.response.*;
import com.fleetmanagement.companyservice.exception.SubscriptionLimitExceededException;
import com.fleetmanagement.companyservice.exception.CompanyNotFoundException;
import com.fleetmanagement.companyservice.repository.CompanyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Company User Management Service
 *
 * Service for managing user-company relationships including:
 * - User count tracking and validation
 * - Bulk user operations
 * - User limit enforcement
 * - Company-user synchronization
 */
@Service
@Transactional
public class CompanyUserManagementService {

    private static final Logger logger = LoggerFactory.getLogger(CompanyUserManagementService.class);

    private static final String USER_COUNT_CACHE_PREFIX = "company:user_count:";
    private static final String DRIVER_COUNT_CACHE_PREFIX = "company:driver_count:";
    private static final String USER_SYNC_LOCK_PREFIX = "company:user_sync_lock:";
    private static final int CACHE_TTL_MINUTES = 5;
    private static final int SYNC_LOCK_TIMEOUT_MINUTES = 2;

    private final CompanyRepository companyRepository;
    private final UserServiceClient userServiceClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CompanySubscriptionService subscriptionService;
    private final EventPublishingService eventPublishingService;

    @Autowired
    public CompanyUserManagementService(CompanyRepository companyRepository,
                                        UserServiceClient userServiceClient,
                                        RedisTemplate<String, Object> redisTemplate,
                                        CompanySubscriptionService subscriptionService,
                                        EventPublishingService eventPublishingService) {
        this.companyRepository = companyRepository;
        this.userServiceClient = userServiceClient;
        this.redisTemplate = redisTemplate;
        this.subscriptionService = subscriptionService;
        this.eventPublishingService = eventPublishingService;
    }

    /**
     * Validate if company can add users
     */
    @Transactional(readOnly = true)
    public CompanyValidationResponse validateUserLimit(UUID companyId) {
        logger.debug("Validating user limit for company: {}", companyId);

        // Check cache first
        String cacheKey = USER_COUNT_CACHE_PREFIX + companyId;
        CompanyValidationResponse cached = (CompanyValidationResponse) redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            logger.debug("Returning cached validation for company: {}", companyId);
            return cached;
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found: " + companyId));

        // Get current user count from User Service
        UserCountResponse userCount = getUserCountFromService(companyId);
        int currentUsers = userCount.getCount();

        // Get subscription limits
        int maxUsers = getMaxUsersForSubscription(company.getSubscriptionPlan());

        // Check if can add user
        boolean canAddUser = (maxUsers == -1) || (currentUsers < maxUsers); // -1 means unlimited

        CompanyValidationResponse validation = CompanyValidationResponse.builder()
                .canAddUser(canAddUser)
                .currentUsers(currentUsers)
                .maxUsers(maxUsers)
                .availableSlots(maxUsers == -1 ? Integer.MAX_VALUE : Math.max(0, maxUsers - currentUsers))
                .subscriptionPlan(company.getSubscriptionPlan().name())
                .message(buildValidationMessage(canAddUser, currentUsers, maxUsers))
                .companyId(companyId)
                .build();

        // Cache the result
        redisTemplate.opsForValue().set(cacheKey, validation, CACHE_TTL_MINUTES, TimeUnit.MINUTES);

        logger.debug("User limit validation for company {}: canAdd={}, current={}, max={}",
                companyId, canAddUser, currentUsers, maxUsers);

        return validation;
    }

    /**
     * Increment user count for company
     */
    public void incrementUserCount(UUID companyId) {
        logger.info("Incrementing user count for company: {}", companyId);

        try {
            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new CompanyNotFoundException("Company not found: " + companyId));

            // Increment the count
            company.setCurrentUserCount(company.getCurrentUserCount() + 1);
            company.setLastUserSyncAt(LocalDateTime.now());
            companyRepository.save(company);

            // Invalidate cache
            invalidateUserCountCache(companyId);

            // Publish event
            eventPublishingService.publishUserCountChangedEvent(companyId, company.getCurrentUserCount(), "INCREMENT");

            logger.info("User count incremented for company: {} to {}", companyId, company.getCurrentUserCount());

        } catch (Exception e) {
            logger.error("Failed to increment user count for company: {}", companyId, e);
            // Don't throw exception to avoid blocking user creation
        }
    }

    /**
     * Decrement user count for company
     */
    public void decrementUserCount(UUID companyId) {
        logger.info("Decrementing user count for company: {}", companyId);

        try {
            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new CompanyNotFoundException("Company not found: " + companyId));

            // Decrement the count (ensure it doesn't go below 0)
            int newCount = Math.max(0, company.getCurrentUserCount() - 1);
            company.setCurrentUserCount(newCount);
            company.setLastUserSyncAt(LocalDateTime.now());
            companyRepository.save(company);

            // Invalidate cache
            invalidateUserCountCache(companyId);

            // Publish event
            eventPublishingService.publishUserCountChangedEvent(companyId, company.getCurrentUserCount(), "DECREMENT");

            logger.info("User count decremented for company: {} to {}", companyId, company.getCurrentUserCount());

        } catch (Exception e) {
            logger.error("Failed to decrement user count for company: {}", companyId, e);
            // Don't throw exception to avoid blocking user deletion
        }
    }

    /**
     * Get all users for company
     */
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getCompanyUsers(UUID companyId, int page, int size, String sortBy, String sortDirection) {
        logger.debug("Getting company users for company: {} (page: {}, size: {})", companyId, page, size);

        // Validate company exists
        if (!companyRepository.existsById(companyId)) {
            throw new CompanyNotFoundException("Company not found: " + companyId);
        }

        // Get users from User Service
        Page<UserResponse> usersPage = userServiceClient.getCompanyUsers(companyId, page, size, sortBy, sortDirection).getBody();

        return PagedResponse.<UserResponse>builder()
                .content(usersPage.getContent())
                .page(usersPage.getNumber())
                .size(usersPage.getSize())
                .totalElements(usersPage.getTotalElements())
                .totalPages(usersPage.getTotalPages())
                .first(usersPage.isFirst())
                .last(usersPage.isLast())
                .empty(usersPage.isEmpty())
                .build();
    }

    /**
     * Perform bulk user operations
     */
    public BulkOperationResponse performBulkUserOperations(UUID companyId, BulkUserOperationRequest request) {
        logger.info("Performing bulk user operations for company: {} (operation: {})", companyId, request.getOperation());

        // Validate company exists
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found: " + companyId));

        BulkOperationResponse response;

        switch (request.getOperation()) {
            case CREATE:
                response = performBulkUserCreation(companyId, request.getCreateRequest());
                break;
            case UPDATE:
                response = performBulkUserUpdate(companyId, request.getUpdateRequest());
                break;
            case DELETE:
                response = performBulkUserDeletion(companyId, request.getUserIds());
                break;
            default:
                throw new IllegalArgumentException("Unsupported bulk operation: " + request.getOperation());
        }

        // Trigger user count synchronization
        synchronizeUserCount(companyId);

        return response;
    }

    /**
     * Synchronize user count with User Service
     */
    public void synchronizeUserCount(UUID companyId) {
        logger.debug("Synchronizing user count for company: {}", companyId);

        String lockKey = USER_SYNC_LOCK_PREFIX + companyId;

        // Try to acquire lock
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", SYNC_LOCK_TIMEOUT_MINUTES, TimeUnit.MINUTES);

        if (!lockAcquired) {
            logger.debug("Sync already in progress for company: {}", companyId);
            return;
        }

        try {
            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new CompanyNotFoundException("Company not found: " + companyId));

            // Get actual count from User Service
            UserCountResponse userCount = getUserCountFromService(companyId);
            UserCountResponse driverCount = getDriverCountFromService(companyId);

            // Update company counts
            int previousUserCount = company.getCurrentUserCount();
            company.setCurrentUserCount(userCount.getCount());
            company.setCurrentDriverCount(driverCount.getCount());
            company.setLastUserSyncAt(LocalDateTime.now());

            companyRepository.save(company);

            // Invalidate caches
            invalidateUserCountCache(companyId);
            invalidateDriverCountCache(companyId);

            // Publish event if count changed
            if (previousUserCount != userCount.getCount()) {
                eventPublishingService.publishUserCountSynchronizedEvent(companyId, previousUserCount, userCount.getCount());
            }

            logger.info("User count synchronized for company: {} (users: {}, drivers: {})",
                    companyId, userCount.getCount(), driverCount.getCount());

        } catch (Exception e) {
            logger.error("Failed to synchronize user count for company: {}", companyId, e);
        } finally {
            // Release lock
            redisTemplate.delete(lockKey);
        }
    }

    /**
     * Get user statistics for company
     */
    @Transactional(readOnly = true)
    public UserStatisticsResponse getUserStatistics(UUID companyId) {
        logger.debug("Getting user statistics for company: {}", companyId);

        // Validate company exists
        if (!companyRepository.existsById(companyId)) {
            throw new CompanyNotFoundException("Company not found: " + companyId);
        }

        // Get statistics from User Service
        UserServiceClient.UserStatisticsResponse stats = userServiceClient.getUserStatistics(companyId).getBody();

        return UserStatisticsResponse.builder()
                .companyId(companyId)
                .totalUsers(stats.getTotalUsers())
                .activeUsers(stats.getActiveUsers())
                .inactiveUsers(stats.getInactiveUsers())
                .driverCount(stats.getDriverCount())
                .adminCount(stats.getAdminCount())
                .managerCount(stats.getManagerCount())
                .viewerCount(stats.getViewerCount())
                .lastSyncAt(LocalDateTime.now())
                .build();
    }

    // Private helper methods

    private BulkOperationResponse performBulkUserCreation(UUID companyId, BulkUserCreateRequest createRequest) {
        // Validate subscription limits
        UserServiceClient.BulkValidationResponse validation = userServiceClient
                .validateBulkUserCreation(companyId, createRequest.getUsers().size()).getBody();

        if (!validation.isCanCreate()) {
            throw new SubscriptionLimitExceededException(
                    "Cannot create " + createRequest.getUsers().size() + " users. " + validation.getMessage());
        }

        // Perform bulk creation
        return userServiceClient.createBulkUsers(createRequest).getBody();
    }

    private BulkOperationResponse performBulkUserUpdate(UUID companyId, BulkUserUpdateRequest updateRequest) {
        return userServiceClient.bulkUpdateUsers(updateRequest).getBody();
    }

    private BulkOperationResponse performBulkUserDeletion(UUID companyId, List<UUID> userIds) {
        return userServiceClient.bulkDeleteUsers(companyId, userIds).getBody();
    }

    private UserCountResponse getUserCountFromService(UUID companyId) {
        try {
            return userServiceClient.getUserCount(companyId).getBody();
        } catch (Exception e) {
            logger.warn("Failed to get user count from User Service for company: {}", companyId, e);
            return UserCountResponse.builder().count(0).message("Service unavailable").build();
        }
    }

    private UserCountResponse getDriverCountFromService(UUID companyId) {
        try {
            return userServiceClient.getDriverCount(companyId).getBody();
        } catch (Exception e) {
            logger.warn("Failed to get driver count from User Service for company: {}", companyId, e);
            return UserCountResponse.builder().count(0).message("Service unavailable").build();
        }
    }

    private int getMaxUsersForSubscription(SubscriptionPlan plan) {
        return switch (plan) {
            case BASIC -> 5;
            case PREMIUM -> 50;
            case ENTERPRISE -> 1000;
            case OWNER -> -1; // Unlimited
            default -> 5; // Default to basic
        };
    }

    private String buildValidationMessage(boolean canAddUser, int currentUsers, int maxUsers) {
        if (maxUsers == -1) {
            return "Unlimited users allowed";
        }
        if (canAddUser) {
            return String.format("Can add user (%d/%d used)", currentUsers, maxUsers);
        } else {
            return String.format("User limit exceeded (%d/%d used)", currentUsers, maxUsers);
        }
    }

    private void invalidateUserCountCache(UUID companyId) {
        redisTemplate.delete(USER_COUNT_CACHE_PREFIX + companyId);
    }

    private void invalidateDriverCountCache(UUID companyId) {
        redisTemplate.delete(DRIVER_COUNT_CACHE_PREFIX + companyId);
    }
}