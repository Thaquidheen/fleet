package com.fleetmanagement.vehicleservice.client;

import com.fleetmanagement.vehicleservice.client.UserServiceClient.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class UserServiceClientFallback implements UserServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceClientFallback.class);

    @Override
    public ResponseEntity<DriverValidationResponse> validateDriver(UUID userId, UUID companyId) {
        logger.warn("User Service unavailable - using fallback for driver validation: {}", userId);

        DriverValidationResponse fallbackResponse = new DriverValidationResponse();
        fallbackResponse.setValid(false);
        fallbackResponse.setAvailable(false);
        fallbackResponse.setUnavailabilityReason("User Service temporarily unavailable");

        return ResponseEntity.ok(fallbackResponse);
    }

    @Override
    public ResponseEntity<List<DriverResponse>> getAvailableDrivers(UUID companyId) {
        logger.warn("User Service unavailable - returning empty driver list for company: {}", companyId);
        return ResponseEntity.ok(Collections.emptyList());
    }

    @Override
    public ResponseEntity<Void> notifyDriverAssignment(UUID userId, DriverAssignmentNotification notification) {
        logger.warn("User Service unavailable - could not notify driver assignment: {}", userId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> notifyDriverUnassignment(UUID userId, UUID vehicleId) {
        logger.warn("User Service unavailable - could not notify driver unassignment: {}", userId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<UserResponse> getUserById(UUID userId) {
        logger.warn("User Service unavailable - returning null for user: {}", userId);
        return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<List<DriverResponse>> getCompanyDrivers(UUID companyId) {
        logger.warn("User Service unavailable - returning empty driver list for company: {}", companyId);
        return ResponseEntity.ok(Collections.emptyList());
    }
}