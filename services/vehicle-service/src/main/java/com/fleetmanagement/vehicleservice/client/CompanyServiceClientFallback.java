package com.fleetmanagement.vehicleservice.client;

import com.fleetmanagement.vehicleservice.client.CompanyServiceClient.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class CompanyServiceClientFallback implements CompanyServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(CompanyServiceClientFallback.class);

    @Override
    public ResponseEntity<CanAddVehicleResponse> canAddVehicle(UUID companyId) {
        logger.warn("Company Service unavailable - using fallback for vehicle limit check: {}", companyId);

        CanAddVehicleResponse fallbackResponse = new CanAddVehicleResponse();
        fallbackResponse.setCanAdd(true); // Allow operation when service is down
        fallbackResponse.setReason("Company Service temporarily unavailable - allowing operation");
        fallbackResponse.setCurrentVehicles(0);
        fallbackResponse.setMaxVehicles(10); // Default fallback limit
        fallbackResponse.setRemainingSlots(10);

        return ResponseEntity.ok(fallbackResponse);
    }

    @Override
    public ResponseEntity<Void> incrementVehicleCount(UUID companyId) {
        logger.warn("Company Service unavailable - could not increment vehicle count for company: {}", companyId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> decrementVehicleCount(UUID companyId) {
        logger.warn("Company Service unavailable - could not decrement vehicle count for company: {}", companyId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<CompanyValidationResponse> validateCompanyLimits(UUID companyId) {
        logger.warn("Company Service unavailable - using fallback for company validation: {}", companyId);

        CompanyValidationResponse fallbackResponse = new CompanyValidationResponse();
        fallbackResponse.setCompanyId(companyId);
        fallbackResponse.setCanAddVehicle(true); // Allow when service is down
        fallbackResponse.setMessage("Company Service temporarily unavailable - using fallback validation");
        fallbackResponse.setCurrentVehicleCount(0);
        fallbackResponse.setMaxVehicleLimit(10);
        fallbackResponse.setSubscriptionPlan("BASIC");

        return ResponseEntity.ok(fallbackResponse);
    }

    @Override
    public ResponseEntity<CompanySubscriptionResponse> getCompanySubscription(UUID companyId) {
        logger.warn("Company Service unavailable - using fallback for subscription: {}", companyId);

        CompanySubscriptionResponse fallbackResponse = new CompanySubscriptionResponse();
        fallbackResponse.setCompanyId(companyId);
        fallbackResponse.setSubscriptionPlan("BASIC");
        fallbackResponse.setBillingModel("STANDARD");
        fallbackResponse.setMaxVehicles(10);
        fallbackResponse.setMaxUsers(5);
        fallbackResponse.setCurrentVehicleCount(0);
        fallbackResponse.setCurrentUserCount(0);
        fallbackResponse.setActive(true);
        fallbackResponse.setSubscriptionStartDate(java.time.LocalDate.now());
        fallbackResponse.setSubscriptionEndDate(java.time.LocalDate.now().plusMonths(1));

        return ResponseEntity.ok(fallbackResponse);
    }

    @Override
    public ResponseEntity<CompanyResponse> getCompanyById(UUID companyId) {
        logger.warn("Company Service unavailable - using fallback for company: {}", companyId);

        CompanyResponse fallbackResponse = new CompanyResponse();
        fallbackResponse.setId(companyId);
        fallbackResponse.setName("Company (Service Unavailable)");
        fallbackResponse.setStatus("UNKNOWN");
        fallbackResponse.setSubscriptionPlan("BASIC");
        fallbackResponse.setMaxVehicles(10);
        fallbackResponse.setCurrentVehicleCount(0);
        fallbackResponse.setActive(true);

        return ResponseEntity.ok(fallbackResponse);
    }
}