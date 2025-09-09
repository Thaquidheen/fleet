package com.fleetmanagement.companyservice.exception;

public class SubscriptionLimitExceededException extends RuntimeException {

    public SubscriptionLimitExceededException(String message) {
        super(message);
    }

    public SubscriptionLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}