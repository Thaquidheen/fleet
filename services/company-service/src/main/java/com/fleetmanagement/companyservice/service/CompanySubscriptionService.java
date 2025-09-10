package com.fleetmanagement.companyservice.service;

import com.fleetmanagement.companyservice.domain.enums.SubscriptionPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * FIXED CompanySubscriptionService - Compatible with Java 17 switch syntax and enum values
 */
@Service
public class CompanySubscriptionService {

    private static final Logger logger = LoggerFactory.getLogger(CompanySubscriptionService.class);

    /**
     * Get maximum users allowed for subscription plan
     */
    public int getMaxUsersForPlan(SubscriptionPlan plan) {
        logger.debug("Getting max users for plan: {}", plan);

        // Use the values from the enum itself
        if (plan.getMaxUsers() == -1) {
            return Integer.MAX_VALUE; // Represent unlimited as max int
        }
        return plan.getMaxUsers();
    }

    /**
     * Get maximum vehicles allowed for subscription plan
     */
    public int getMaxVehiclesForPlan(SubscriptionPlan plan) {
        logger.debug("Getting max vehicles for plan: {}", plan);

        // Use the values from the enum itself
        if (plan.getMaxVehicles() == -1) {
            return Integer.MAX_VALUE; // Represent unlimited as max int
        }
        return plan.getMaxVehicles();
    }

    /**
     * Get maximum drivers allowed for subscription plan
     */
    public int getMaxDriversForPlan(SubscriptionPlan plan) {
        logger.debug("Getting max drivers for plan: {}", plan);

        return switch (plan) {
            case BASIC -> 5;
            case PREMIUM -> 50;
            case ENTERPRISE -> 1000;
            case OWNER -> Integer.MAX_VALUE; // Unlimited
        };
    }

    /**
     * Check if plan allows unlimited resources
     */
    public boolean isUnlimitedPlan(SubscriptionPlan plan) {
        return plan == SubscriptionPlan.OWNER;
    }

    /**
     * Get plan priority (higher number = higher priority)
     */
    public int getPlanPriority(SubscriptionPlan plan) {
        return switch (plan) {
            case BASIC -> 1;
            case PREMIUM -> 2;
            case ENTERPRISE -> 3;
            case OWNER -> 4;
        };
    }

    /**
     * Check if plan upgrade is valid
     */
    public boolean isValidUpgrade(SubscriptionPlan fromPlan, SubscriptionPlan toPlan) {
        int fromPriority = getPlanPriority(fromPlan);
        int toPriority = getPlanPriority(toPlan);
        return toPriority >= fromPriority;
    }

    /**
     * Get plan display name
     */
    public String getPlanDisplayName(SubscriptionPlan plan) {
        return plan.getDisplayName();
    }

    /**
     * Get plan description
     */
    public String getPlanDescription(SubscriptionPlan plan) {
        return plan.getDescription();
    }

    /**
     * Check if current usage exceeds plan limits
     */
    public boolean exceedsLimits(SubscriptionPlan plan, int currentUsers, int currentVehicles, int currentDrivers) {
        if (isUnlimitedPlan(plan)) {
            return false;
        }

        int maxUsers = getMaxUsersForPlan(plan);
        int maxVehicles = getMaxVehiclesForPlan(plan);
        int maxDrivers = getMaxDriversForPlan(plan);

        return currentUsers > maxUsers ||
                currentVehicles > maxVehicles ||
                currentDrivers > maxDrivers;
    }

    /**
     * Calculate usage percentage for plan
     */
    public double calculateUsagePercentage(SubscriptionPlan plan, int currentUsers, int currentVehicles) {
        if (isUnlimitedPlan(plan)) {
            return 0.0; // No limits, so 0% usage of "unlimited"
        }

        int maxUsers = getMaxUsersForPlan(plan);
        int maxVehicles = getMaxVehiclesForPlan(plan);

        double userUsage = (double) currentUsers / maxUsers;
        double vehicleUsage = (double) currentVehicles / maxVehicles;

        // Return the higher usage percentage
        return Math.max(userUsage, vehicleUsage) * 100;
    }

    /**
     * Get recommended plan based on current usage
     */
    public SubscriptionPlan getRecommendedPlan(int currentUsers, int currentVehicles, int currentDrivers) {
        // Check if current usage fits in each plan (from lowest to highest)
        for (SubscriptionPlan plan : SubscriptionPlan.values()) {
            if (plan == SubscriptionPlan.OWNER) {
                continue; // Skip owner plan in recommendations
            }

            if (!exceedsLimits(plan, currentUsers, currentVehicles, currentDrivers)) {
                return plan;
            }
        }

        // If nothing fits, recommend enterprise
        return SubscriptionPlan.ENTERPRISE;
    }

    /**
     * Get monthly price for plan
     */
    public double getMonthlyPrice(SubscriptionPlan plan) {
        return plan.getMonthlyPrice();
    }

    /**
     * Format plan limits as human-readable string
     */
    public String formatPlanLimits(SubscriptionPlan plan) {
        if (isUnlimitedPlan(plan)) {
            return "Unlimited users, vehicles, and drivers";
        }

        return String.format("%d users, %d vehicles, %d drivers",
                getMaxUsersForPlan(plan),
                getMaxVehiclesForPlan(plan),
                getMaxDriversForPlan(plan));
    }

    /**
     * Check if plan supports a specific feature count
     */
    public boolean supportsUserCount(SubscriptionPlan plan, int userCount) {
        return isUnlimitedPlan(plan) || userCount <= getMaxUsersForPlan(plan);
    }

    /**
     * Check if plan supports a specific vehicle count
     */
    public boolean supportsVehicleCount(SubscriptionPlan plan, int vehicleCount) {
        return isUnlimitedPlan(plan) || vehicleCount <= getMaxVehiclesForPlan(plan);
    }

    /**
     * Get upgrade cost difference between plans
     */
    public double getUpgradeCost(SubscriptionPlan fromPlan, SubscriptionPlan toPlan) {
        if (!isValidUpgrade(fromPlan, toPlan)) {
            throw new IllegalArgumentException("Invalid upgrade path from " + fromPlan + " to " + toPlan);
        }

        return getMonthlyPrice(toPlan) - getMonthlyPrice(fromPlan);
    }
}