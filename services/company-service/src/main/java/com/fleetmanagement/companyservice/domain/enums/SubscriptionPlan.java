package com.fleetmanagement.companyservice.domain.enums;

/**
 * FIXED SubscriptionPlan enum - Added missing OWNER value
 */
public enum SubscriptionPlan {
    BASIC("Basic", "Basic plan with limited features", 5, 10, 0.0),
    PREMIUM("Premium", "Premium plan with advanced features", 50, 100, 99.99),
    ENTERPRISE("Enterprise", "Enterprise plan with all features", 1000, 10000, 999.99),
    OWNER("Owner", "Unlimited plan for fleet owners", -1, -1, 1999.99); // Added missing OWNER

    private final String displayName;
    private final String description;
    private final Integer maxUsers;
    private final Integer maxVehicles;
    private final Double monthlyPrice;

    SubscriptionPlan(String displayName, String description, Integer maxUsers, Integer maxVehicles, Double monthlyPrice) {
        this.displayName = displayName;
        this.description = description;
        this.maxUsers = maxUsers;
        this.maxVehicles = maxVehicles;
        this.monthlyPrice = monthlyPrice;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public Integer getMaxUsers() {
        return maxUsers;
    }

    public Integer getMaxVehicles() {
        return maxVehicles;
    }

    public Double getMonthlyPrice() {
        return monthlyPrice;
    }

    /**
     * Check if this plan allows unlimited resources
     */
    public boolean isUnlimited() {
        return this == OWNER;
    }

    /**
     * Get plan priority (higher number = higher priority)
     */
    public int getPriority() {
        return switch (this) {
            case BASIC -> 1;
            case PREMIUM -> 2;
            case ENTERPRISE -> 3;
            case OWNER -> 4;
        };
    }
}