package com.fleetmanagement.companyservice.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * MISSING DTO: UserStatisticsResponse
 *
 * This class was missing and causing compilation errors in CompanyUserManagementService
 * Fixed the companyId field type to be UUID instead of String
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsResponse {

    // FIXED: Changed from String to UUID to match the assignment in service
    private UUID companyId;

    private int totalUsers;
    private int activeUsers;
    private int inactiveUsers;
    private int driverCount;
    private int adminCount;
    private int managerCount;
    private int viewerCount;

    @Builder.Default
    private LocalDateTime lastSyncAt = LocalDateTime.now();

    // Additional metadata
    private boolean fromCache;
    private String source; // e.g., "USER_SERVICE", "CACHE", "FALLBACK"
    private LocalDateTime statisticsGeneratedAt;

    /**
     * Calculate total active and inactive users
     */
    public int getTotalCalculated() {
        return activeUsers + inactiveUsers;
    }

    /**
     * Calculate percentage of active users
     */
    public double getActiveUserPercentage() {
        if (totalUsers == 0) {
            return 0.0;
        }
        return (double) activeUsers / totalUsers * 100.0;
    }

    /**
     * Check if statistics are recent (within last hour)
     */
    public boolean isRecent() {
        if (lastSyncAt == null) {
            return false;
        }
        return lastSyncAt.isAfter(LocalDateTime.now().minusHours(1));
    }

    /**
     * Create a fallback response when service is unavailable
     */
    public static UserStatisticsResponse fallback(UUID companyId, String errorMessage) {
        return UserStatisticsResponse.builder()
                .companyId(companyId)
                .totalUsers(0)
                .activeUsers(0)
                .inactiveUsers(0)
                .driverCount(0)
                .adminCount(0)
                .managerCount(0)
                .viewerCount(0)
                .fromCache(false)
                .source("FALLBACK")
                .lastSyncAt(LocalDateTime.now())
                .build();
    }
}