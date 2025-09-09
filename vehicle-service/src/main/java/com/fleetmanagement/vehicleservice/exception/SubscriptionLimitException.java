package com.fleetmanagement.vehicleservice.exception;

import java.util.List;

/**
 * Subscription Limit Exception
 */
public class SubscriptionLimitException extends VehicleServiceException {
    public SubscriptionLimitException(String message) {
        super(message);
    }
}