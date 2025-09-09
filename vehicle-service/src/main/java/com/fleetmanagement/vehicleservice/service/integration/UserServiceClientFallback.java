package com.fleetmanagement.vehicleservice.service.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * User Service Client Fallback
 *
 * Provides fallback implementations when the User Service is unavailable.
 * Implements circuit breaker pattern for resilience.
 */
@Component
public class UserServiceClientFallback implements UserServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceClientFallback.class);

    @Override
    public DriverResponse getDriver(UUID driverId) {
        logger.warn("User Service unavailable - using fallback for getDriver: {}", driverId);

        DriverResponse fallbackResponse = new DriverResponse();
        fallbackResponse.setId(driverId);
        fallbackResponse.setUsername("driver-" + driverId.toString().substring(0, 8));
        fallbackResponse.setEmail("driver@company.com");
        fallbackResponse.setFirstName("Driver");
        fallbackResponse.setLastName("(Service Unavailable)");
        fallbackResponse.setFullName("Driver (Service Unavailable)");
        fallbackResponse.setRole("DRIVER");
        fallbackResponse.setStatus("ACTIVE");
        fallbackResponse.setActive(true);
        fallbackResponse.setAvailableForAssignment(true);

        return fallbackResponse;
    }

    @Override
    public DriverValidationResponse validateDriver(UUID driverId, UUID companyId) {
        logger.warn("User Service unavailable - using fallback for validateDriver: {}", driverId);

        DriverValidationResponse fallbackResponse = new DriverValidationResponse();
        fallbackResponse.setValid(true); // Allow operation when service is down
        fallbackResponse.setDriver(true);
        fallbackResponse.setActive(true);
        fallbackResponse.setBelongsToCompany(true);
        fallbackResponse.setMessage("User service unavailable - allowing operation");
        fallbackResponse.setValidationErrors(new ArrayList<>());

        return fallbackResponse;
    }

    @Override
    public DriverAvailabilityResponse checkDriverAvailability(UUID driverId, UUID companyId) {
        logger.warn("User Service unavailable - using fallback for checkDriverAvailability: {}", driverId);

        DriverAvailabilityResponse fallbackResponse = new DriverAvailabilityResponse();
        fallbackResponse.setAvailable(true); // Allow operation when service is down
        fallbackResponse.setCurrentlyAssigned(false);
        fallbackResponse.setUnavailabilityReason("User service unavailable - status unknown");

        return fallbackResponse;
    }

    @Override
    public List<DriverResponse> getDriversByCompany(UUID companyId) {
        logger.warn("User Service unavailable - using fallback for getDriversByCompany: {}", companyId);

        // Return empty list when service is unavailable
        return new ArrayList<>();
    }

    @Override
    public List<DriverResponse> getAvailableDrivers(UUID companyId) {
        logger.warn("User Service unavailable - using fallback for getAvailableDrivers: {}", companyId);

        // Return empty list when service is unavailable
        return new ArrayList<>();
    }

    @Override
    public void notifyDriverAssignment(UUID driverId, DriverAssignmentNotification notification) {
        logger.warn("User Service unavailable - skipping notifyDriverAssignment for driver: {}", driverId);
        // No-op in fallback - notification will be missed but assignment can proceed
    }

    @Override
    public void notifyDriverUnassignment(UUID driverId, UUID companyId) {
        logger.warn("User Service unavailable - skipping notifyDriverUnassignment for driver: {}", driverId);
        // No-op in fallback - notification will be missed but unassignment can proceed
    }
}