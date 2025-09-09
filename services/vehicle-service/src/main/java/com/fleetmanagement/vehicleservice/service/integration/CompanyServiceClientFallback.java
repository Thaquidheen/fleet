package com.fleetmanagement.vehicleservice.service.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Company Service Client Fallback
 *
 * Provides fallback implementations when the Company Service is unavailable.
 * Implements circuit breaker pattern for resilience.
 */
@Component
public class CompanyServiceClientFallback implements CompanyServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(CompanyServiceClientFallback.class);

    @Override
    public CompanyResponse getCompany(UUID companyId) {
        logger.warn("Company Service unavailable - using fallback for getCompany: {}", companyId);

        CompanyResponse fallbackResponse = new CompanyResponse();
        fallbackResponse.setId(companyId);
        fallbackResponse.setName("Company (Service Unavailable)");
        fallbackResponse.setStatus("UNKNOWN");
        fallbackResponse.setSubscriptionPlan("BASIC");
        fallbackResponse.setMaxVehicles(10); // Default fallback limit
        fallbackResponse.setCurrentVehicleCount(0);
        fallbackResponse.setActive(true);

        return fallbackResponse;
    }

    @Override
    public VehicleLimitValidationResponse validateVehicleLimit(UUID companyId) {
        logger.warn("Company Service unavailable - using fallback for validateVehicleLimit: {}", companyId);

        VehicleLimitValidationResponse fallbackResponse = new VehicleLimitValidationResponse();
        fallbackResponse.setValid(true); // Allow operation when service is down
        fallbackResponse.setCanAddVehicle(true);
        fallbackResponse.setCurrentCount(0);
        fallbackResponse.setMaxAllowed(10); // Default fallback limit
        fallbackResponse.setMessage("Company service unavailable - using default limits");

        return fallbackResponse;
    }

    @Override
    public CanAddVehicleResponse canAddVehicle(UUID companyId) {
        logger.warn("Company Service unavailable - using fallback for canAddVehicle: {}", companyId);

        CanAddVehicleResponse fallbackResponse = new CanAddVehicleResponse();
        fallbackResponse.setCanAdd(true); // Allow operation when service is down
        fallbackResponse.setReason("Company service unavailable - allowing operation");
        fallbackResponse.setRemainingSlots(10); // Default fallback

        return fallbackResponse;
    }

    @Override
    public void incrementVehicleCount(UUID companyId) {
        logger.warn("Company Service unavailable - skipping incrementVehicleCount for: {}", companyId);
        // No-op in fallback - we'll let the company service sync later
    }

    @Override
    public void decrementVehicleCount(UUID companyId) {
        logger.warn("Company Service unavailable - skipping decrementVehicleCount for: {}", companyId);
        // No-op in fallback - we'll let the company service sync later
    }

    @Override
    public CompanySubscriptionResponse getCompanySubscription(UUID companyId) {
        logger.warn("Company Service unavailable - using fallback for getCompanySubscription: {}", companyId);

        CompanySubscriptionResponse fallbackResponse = new CompanySubscriptionResponse();
        fallbackResponse.setCompanyId(companyId);
        fallbackResponse.setSubscriptionPlan("BASIC");
        fallbackResponse.setBillingModel("STANDARD");
        fallbackResponse.setMaxVehicles(10); // Default fallback limit
        fallbackResponse.setMaxUsers(5); // Default fallback limit
        fallbackResponse.setCurrentVehicleCount(0);
        fallbackResponse.setCurrentUserCount(0);
        fallbackResponse.setActive(true);
        fallbackResponse.setSubscriptionStartDate(java.time.LocalDate.now());
        fallbackResponse.setSubscriptionEndDate(java.time.LocalDate.now().plusMonths(1));

        return fallbackResponse;
    }
}