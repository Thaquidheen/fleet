package com.fleetmanagement.deviceservice.service;



import com.fleetmanagement.deviceservice.domain.entity.Device;
import com.fleetmanagement.deviceservice.domain.enums.SensorType;
import com.fleetmanagement.deviceservice.dto.request.SensorSubscriptionRequest;
import com.fleetmanagement.deviceservice.dto.response.SensorSubscriptionResponse;


import java.util.UUID;

/**
 * Sensor Subscription Service Interface
 * Sensor management and billing
 */
public interface SensorSubscriptionService {

    /**
     * Subscribe sensor to device
     */
    SensorSubscriptionResponse subscribeSensorToDevice(SensorSubscriptionRequest request);

    /**
     * Cancel sensor subscription
     */
    void cancelSensorSubscription(UUID subscriptionId, UUID userId, String reason);

    /**
     * Update sensor subscription
     */
    SensorSubscriptionResponse updateSensorSubscription(UUID subscriptionId, SensorSubscriptionRequest request);

    /**
     * Get sensor subscriptions for company
     */
    List<SensorSubscriptionResponse> getSensorSubscriptions(UUID companyId);

    /**
     * Get sensor subscriptions for device
     */
    List<SensorSubscriptionResponse> getDeviceSensorSubscriptions(String deviceId);

    /**
     * Calculate monthly sensor cost for device
     */
    Double calculateMonthlySensorCost(String deviceId);

    /**
     * Calculate total sensor revenue for company
     */
    Double calculateCompanySensorRevenue(UUID companyId);

    /**
     * Process sensor billing
     */
    void processSensorBilling();

    /**
     * Validate sensor compatibility
     */
    void validateSensorCompatibility(Device device, SensorType sensorType);

    /**
     * Validate sensor subscription limits
     */
    void validateSensorSubscriptionLimits(UUID companyId, SensorType sensorType);
}

