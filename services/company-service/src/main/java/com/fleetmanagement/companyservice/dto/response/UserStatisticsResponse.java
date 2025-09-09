// UserStatisticsResponse.java
package com.fleetmanagement.companyservice.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsResponse {
    private UUID companyId;
    private int totalUsers;
    private int activeUsers;
    private int inactiveUsers;
    private int driverCount;
    private int adminCount;
    private int managerCount;
    private int viewerCount;
    private LocalDateTime lastSyncAt;
    private Map<String, Integer> usersByRole;
    private Map<String, Integer> usersByStatus;
    private Map<String, Integer> usersByDepartment;
    private double avgUsersPerMonth; // Growth metrics
    private int newUsersThisMonth;
    private int deletedUsersThisMonth;
}