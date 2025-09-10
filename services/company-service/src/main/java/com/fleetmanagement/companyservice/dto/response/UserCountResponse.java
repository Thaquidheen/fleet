package com.fleetmanagement.companyservice.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * FIXED: This class was missing proper Lombok annotations and methods
 * causing compilation errors in CompanyController where getCount(), getCountedAt(),
 * isFromCache() methods were being called
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCountResponse {

    private int count;
    private String message;

    @Builder.Default
    private LocalDateTime countedAt = LocalDateTime.now();

    @Builder.Default
    private boolean fromCache = false;

    // Additional metadata
    private String source; // e.g., "USER_SERVICE", "CACHE", "FALLBACK"
    private boolean accurate; // Whether the count is considered accurate
    private LocalDateTime lastSyncTime; // When was the last successful sync

    // Helper methods for common scenarios
    public static UserCountResponse success(int count) {
        return UserCountResponse.builder()
                .count(count)
                .message("User count retrieved successfully")
                .countedAt(LocalDateTime.now())
                .fromCache(false)
                .accurate(true)
                .source("USER_SERVICE")
                .build();
    }

    public static UserCountResponse cached(int count, LocalDateTime cacheTime) {
        return UserCountResponse.builder()
                .count(count)
                .message("User count retrieved from cache")
                .countedAt(cacheTime)
                .fromCache(true)
                .accurate(true)
                .source("CACHE")
                .build();
    }

    public static UserCountResponse fallback(String errorMessage) {
        return UserCountResponse.builder()
                .count(0)
                .message(errorMessage)
                .countedAt(LocalDateTime.now())
                .fromCache(false)
                .accurate(false)
                .source("FALLBACK")
                .build();
    }
}