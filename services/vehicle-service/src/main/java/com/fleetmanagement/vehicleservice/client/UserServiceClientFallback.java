package com.fleetmanagement.vehicleservice.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class UserServiceClientFallback implements UserServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceClientFallback.class);

    @Override
    public List<DriverResponse> getAvailableDrivers(UUID companyId) {
        logger.warn("Fallback: getAvailableDrivers called for company: {}", companyId);
        return Collections.emptyList();
    }

    @Override
    public List<DriverResponse> getCompanyDrivers(UUID companyId) {
        logger.warn("Fallback: getCompanyDrivers called for company: {}", companyId);
        return Collections.emptyList();
    }

    @Override
    public void notifyDriverAssignment(UUID driverId, DriverAssignmentNotification notification) {
        logger.warn("Fallback: notifyDriverAssignment called for driver: {}", driverId);
    }

    @Override
    public void notifyDriverUnassignment(UUID driverId, UUID vehicleId) {
        logger.warn("Fallback: notifyDriverUnassignment called for driver: {} and vehicle: {}", driverId, vehicleId);
    }

    @Override
    public DriverValidationResponse validateDriver(UUID userId, UUID companyId) {
        logger.warn("Fallback: validateDriver called for user: {} and company: {}", userId, companyId);
        DriverValidationResponse response = new DriverValidationResponse();
        response.setValid(false);
        response.setAvailable(false);
        response.setMessage("User service unavailable");
        response.setUnavailabilityReason("Service temporarily unavailable");
        return response;
    }


}