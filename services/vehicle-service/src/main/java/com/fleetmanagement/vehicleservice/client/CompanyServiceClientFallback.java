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
    public ResponseEntity<CompanyValidationResponse> validateCompanyLimits(UUID companyId) {
        logger.warn("Company Service unavailable - using fallback for company validation: {}", companyId);

        CompanyValidationResponse fallbackResponse = new CompanyValidationResponse();
        fallbackResponse.setCompanyId(companyId);
        fallbackResponse.setCanAddVehicle(false);
        fallbackResponse.setMessage("Company Service temporarily unavailable - cannot validate limits");

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
    public ResponseEntity<CanAddVehicleResponse> canAddVehicle(UUID companyId) {
        logger.warn("Company Service unavailable - using fallback for vehicle limit check: {}", companyId);

        CanAddVehicleResponse fallbackResponse = new CanAddVehicleResponse();
        fallbackResponse.setCanAdd(false);
        fallbackResponse.setReason("Company Service temporarily unavailable");

        return ResponseEntity.ok(fallbackResponse);
    }

    @Override
    public ResponseEntity<CompanySubscriptionResponse> getCompanySubscription(UUID companyId) {
        logger.warn("Company Service unavailable - returning null subscription for company: {}", companyId);
        return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<CompanyResponse> getCompanyById(UUID companyId) {
        logger.warn("Company Service unavailable - returning null company: {}", companyId);
        return ResponseEntity.notFound().build();
    }
}