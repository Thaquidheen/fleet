package com.fleetmanagement.companyservice.domain.enums;

public enum SubscriptionPlan {
    BASIC("Basic", 99.0, 50, 10),
    PREMIUM("Premium", 299.0, 200, 50),
    ENTERPRISE("Enterprise", 599.0, -1, -1); // -1 means unlimited

    private final String displayName;
    private final Double monthlyPrice;
    private final Integer maxVehicles;
    private final Integer maxUsers;

    SubscriptionPlan(String displayName, Double monthlyPrice, Integer maxVehicles, Integer maxUsers) {
        this.displayName = displayName;
        this.monthlyPrice = monthlyPrice;
        this.maxVehicles = maxVehicles;
        this.maxUsers = maxUsers;
    }

    public String getDisplayName() { return displayName; }
    public Double getMonthlyPrice() { return monthlyPrice; }
    public Integer getMaxVehicles() { return maxVehicles; }
    public Integer getMaxUsers() { return maxUsers; }

    public boolean isUnlimited() { return this == ENTERPRISE; }
}