package com.fleetmanagement.companyservice.client;

import com.fleetmanagement.companyservice.dto.request.BulkUserCreateRequest;
import com.fleetmanagement.companyservice.dto.request.BulkUserUpdateRequest;
import com.fleetmanagement.companyservice.dto.response.UserResponse;
import com.fleetmanagement.companyservice.dto.response.UserCountResponse;
import com.fleetmanagement.companyservice.dto.response.BulkOperationResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * User Service Client
 *
 * Feign client for integrating with the User Service to:
 * - Get user counts and statistics
 * - Perform bulk user operations
 * - Retrieve user information
 * - Manage user-company relationships
 */
@FeignClient(
        name = "user-service",
        url = "${feign.client.config.user-service.url:http://localhost:8082}",
        fallback = UserServiceClientFallback.class
)
public interface UserServiceClient {

    /**
     * Get total user count for company
     */
    @GetMapping("/api/users/count")
    @CircuitBreaker(name = "user-service", fallbackMethod = "getUserCountFallback")
    ResponseEntity<UserCountResponse> getUserCount(@RequestParam("companyId") UUID companyId);

    /**
     * Get driver count for company
     */
    @GetMapping("/api/users/drivers/count")
    @CircuitBreaker(name = "user-service", fallbackMethod = "getDriverCountFallback")
    ResponseEntity<UserCountResponse> getDriverCount(@RequestParam("companyId") UUID companyId);

    /**
     * Get all users for company with pagination
     */
    @GetMapping("/api/users/company/{companyId}")
    @CircuitBreaker(name = "user-service", fallbackMethod = "getCompanyUsersFallback")
    ResponseEntity<Page<UserResponse>> getCompanyUsers(
            @PathVariable("companyId") UUID companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection);

    /**
     * Get users by role for company
     */
    @GetMapping("/api/users/company/{companyId}/role/{role}")
    @CircuitBreaker(name = "user-service", fallbackMethod = "getUsersByRoleFallback")
    ResponseEntity<List<UserResponse>> getUsersByRole(
            @PathVariable("companyId") UUID companyId,
            @PathVariable("role") String role);

    /**
     * Create multiple users in bulk
     */
    @PostMapping("/api/users/bulk-create")
    @CircuitBreaker(name = "user-service", fallbackMethod = "createBulkUsersFallback")
    ResponseEntity<BulkOperationResponse> createBulkUsers(@RequestBody BulkUserCreateRequest request);

    /**
     * Update multiple users in bulk
     */
    @PutMapping("/api/users/bulk-update")
    @CircuitBreaker(name = "user-service", fallbackMethod = "bulkUpdateUsersFallback")
    ResponseEntity<BulkOperationResponse> bulkUpdateUsers(@RequestBody BulkUserUpdateRequest request);

    /**
     * Delete multiple users in bulk
     */
    @DeleteMapping("/api/users/bulk-delete")
    @CircuitBreaker(name = "user-service", fallbackMethod = "bulkDeleteUsersFallback")
    ResponseEntity<BulkOperationResponse> bulkDeleteUsers(
            @RequestParam("companyId") UUID companyId,
            @RequestBody List<UUID> userIds);

    /**
     * Get user by ID
     */
    @GetMapping("/api/users/{userId}")
    @CircuitBreaker(name = "user-service", fallbackMethod = "getUserByIdFallback")
    ResponseEntity<UserResponse> getUserById(@PathVariable("userId") UUID userId);

    /**
     * Validate user belongs to company
     */
    @GetMapping("/api/users/{userId}/validate-company")
    @CircuitBreaker(name = "user-service", fallbackMethod = "validateUserCompanyFallback")
    ResponseEntity<Boolean> validateUserBelongsToCompany(
            @PathVariable("userId") UUID userId,
            @RequestParam("companyId") UUID companyId);

    /**
     * Update user company assignment
     */
    @PutMapping("/api/users/{userId}/company")
    @CircuitBreaker(name = "user-service", fallbackMethod = "updateUserCompanyFallback")
    ResponseEntity<UserResponse> updateUserCompany(
            @PathVariable("userId") UUID userId,
            @RequestParam("companyId") UUID companyId);

    /**
     * Get user statistics for company
     */
    @GetMapping("/api/users/company/{companyId}/statistics")
    @CircuitBreaker(name = "user-service", fallbackMethod = "getUserStatisticsFallback")
    ResponseEntity<UserStatisticsResponse> getUserStatistics(@PathVariable("companyId") UUID companyId);

    /**
     * Check if users can be created (subscription validation)
     */
    @PostMapping("/api/users/validate-bulk-creation")
    @CircuitBreaker(name = "user-service", fallbackMethod = "validateBulkCreationFallback")
    ResponseEntity<BulkValidationResponse> validateBulkUserCreation(
            @RequestParam("companyId") UUID companyId,
            @RequestParam("userCount") int userCount);

    // Fallback methods (implemented in UserServiceClientFallback)
    default ResponseEntity<UserCountResponse> getUserCountFallback(UUID companyId, Exception ex) {
        return ResponseEntity.ok(UserCountResponse.builder()
                .count(0)
                .message("Service unavailable")
                .build());
    }

    default ResponseEntity<UserCountResponse> getDriverCountFallback(UUID companyId, Exception ex) {
        return ResponseEntity.ok(UserCountResponse.builder()
                .count(0)
                .message("Service unavailable")
                .build());
    }

    default ResponseEntity<Page<UserResponse>> getCompanyUsersFallback(UUID companyId, int page, int size, String sortBy, String sortDirection, Exception ex) {
        return ResponseEntity.ok(Page.empty());
    }

    default ResponseEntity<List<UserResponse>> getUsersByRoleFallback(UUID companyId, String role, Exception ex) {
        return ResponseEntity.ok(List.of());
    }

    default ResponseEntity<BulkOperationResponse> createBulkUsersFallback(BulkUserCreateRequest request, Exception ex) {
        return ResponseEntity.ok(BulkOperationResponse.builder()
                .successful(0)
                .failed(request.getUsers().size())
                .errors(List.of("User service unavailable"))
                .build());
    }

    default ResponseEntity<BulkOperationResponse> bulkUpdateUsersFallback(BulkUserUpdateRequest request, Exception ex) {
        return ResponseEntity.ok(BulkOperationResponse.builder()
                .successful(0)
                .failed(request.getUsers().size())
                .errors(List.of("User service unavailable"))
                .build());
    }

    default ResponseEntity<BulkOperationResponse> bulkDeleteUsersFallback(UUID companyId, List<UUID> userIds, Exception ex) {
        return ResponseEntity.ok(BulkOperationResponse.builder()
                .successful(0)
                .failed(userIds.size())
                .errors(List.of("User service unavailable"))
                .build());
    }

    default ResponseEntity<UserResponse> getUserByIdFallback(UUID userId, Exception ex) {
        return ResponseEntity.notFound().build();
    }

    default ResponseEntity<Boolean> validateUserCompanyFallback(UUID userId, UUID companyId, Exception ex) {
        return ResponseEntity.ok(false);
    }

    default ResponseEntity<UserResponse> updateUserCompanyFallback(UUID userId, UUID companyId, Exception ex) {
        return ResponseEntity.badRequest().build();
    }

    default ResponseEntity<UserStatisticsResponse> getUserStatisticsFallback(UUID companyId, Exception ex) {
        return ResponseEntity.ok(UserStatisticsResponse.builder()
                .totalUsers(0)
                .activeUsers(0)
                .inactiveUsers(0)
                .driverCount(0)
                .adminCount(0)
                .message("Service unavailable")
                .build());
    }

    default ResponseEntity<BulkValidationResponse> validateBulkCreationFallback(UUID companyId, int userCount, Exception ex) {
        return ResponseEntity.ok(BulkValidationResponse.builder()
                .canCreate(false)
                .maxAllowed(0)
                .currentCount(0)
                .requestedCount(userCount)
                .message("Service unavailable - cannot validate user creation")
                .build());
    }

    // Response DTOs
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserStatisticsResponse {
        private int totalUsers;
        private int activeUsers;
        private int inactiveUsers;
        private int driverCount;
        private int adminCount;
        private int managerCount;
        private int viewerCount;
        private String message;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BulkValidationResponse {
        private boolean canCreate;
        private int maxAllowed;
        private int currentCount;
        private int requestedCount;
        private int availableSlots;
        private String message;
        private List<String> errors;
    }
}