package com.fleetmanagement.userservice.service;

import com.fleetmanagement.userservice.client.CompanyServiceClient;
import com.fleetmanagement.userservice.dto.response.CompanyValidationResponse;
import com.fleetmanagement.userservice.exception.SubscriptionLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class SubscriptionValidationService {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionValidationService.class);

    private final CompanyServiceClient companyServiceClient;
    private final CacheService cacheService;

    @Value("${app.company.validation.cache-duration:300}")
    private int cacheDataDuration;

    @Autowired
    public SubscriptionValidationService(CompanyServiceClient companyServiceClient,
                                         CacheService cacheService) {
        this.companyServiceClient = companyServiceClient;
        this.cacheService = cacheService;
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void validateUserCreation(UUID companyId) throws SubscriptionLimitExceededException {
        logger.info("Validating user creation for company: {}", companyId);

        try {
            // Check cache first
            String cacheKey = "company:validation:" + companyId;
            CompanyValidationResponse cached = (CompanyValidationResponse) cacheService.get(cacheKey);

            CompanyValidationResponse validation;
            if (cached != null) {
                validation = cached;
                logger.debug("Using cached validation for company: {}", companyId);
            } else {
                validation = companyServiceClient.validateUserLimit(companyId).getBody();
                // Cache for configured duration
                cacheService.set(cacheKey, validation, Duration.ofSeconds(cacheDataDuration));
                logger.debug("Cached new validation for company: {}", companyId);
            }

            if (validation == null || !validation.isCanAddUser()) {
                String message = validation != null ? validation.getMessage() :
                        "Company has reached maximum user limit";
                throw new SubscriptionLimitExceededException(message);
            }

            logger.info("User creation validated for company: {} (Current: {}, Max: {})",
                    companyId, validation.getCurrentUsers(), validation.getMaxUsers());

        } catch (SubscriptionLimitExceededException e) {
            throw e; // Re-throw subscription limit exceptions
        } catch (Exception e) {
            logger.error("Failed to validate user creation for company: {}", companyId, e);
            // In case of service failure, we could either:
            // 1. Fail fast (current approach)
            // 2. Allow creation with a warning
            throw new SubscriptionLimitExceededException("Unable to validate company subscription limits");
        }
    }

    public void incrementUserCount(UUID companyId) {
        try {
            companyServiceClient.incrementUserCount(companyId);
            // Invalidate cache
            cacheService.delete("company:validation:" + companyId);
            logger.info("User count incremented for company: {}", companyId);
        } catch (Exception e) {
            logger.error("Failed to increment user count for company: {}", companyId, e);
            // Note: This is logged but doesn't prevent user creation
            // Consider implementing eventual consistency mechanism
        }
    }

    public void decrementUserCount(UUID companyId) {
        try {
            companyServiceClient.decrementUserCount(companyId);
            // Invalidate cache
            cacheService.delete("company:validation:" + companyId);
            logger.info("User count decremented for company: {}", companyId);
        } catch (Exception e) {
            logger.error("Failed to decrement user count for company: {}", companyId, e);
        }
    }
}