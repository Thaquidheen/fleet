package com.fleetmanagement.companyservice.client;

import com.fleetmanagement.companyservice.dto.request.BulkUserCreateRequest;
import com.fleetmanagement.companyservice.dto.request.BulkUserUpdateRequest;
import com.fleetmanagement.companyservice.dto.response.BulkOperationResponse;
import com.fleetmanagement.companyservice.dto.response.UserCountResponse;
import com.fleetmanagement.companyservice.dto.response.UserResponse;
import com.fleetmanagement.companyservice.client.UserServiceClient;
import com.fleetmanagement.companyservice.dto.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class UserServiceClientFallback implements UserServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceClientFallback.class);

    @Override
    public ResponseEntity<UserCountResponse> getUserCount(UUID companyId) {
        logger.warn("User Service unavailable - using fallback for getUserCount: {}", companyId);
        return ResponseEntity.ok(UserCountResponse.builder()
                .count(0)
                .message("Service unavailable")
                .build());
    }

    @Override
    public ResponseEntity<UserCountResponse> getDriverCount(UUID companyId) {
        logger.warn("User Service unavailable - using fallback for getDriverCount: {}", companyId);
        return ResponseEntity.ok(UserCountResponse.builder()
                .count(0)
                .message("Service unavailable")
                .build());
    }

    @Override
    public ResponseEntity<Page<UserResponse>> getCompanyUsers(UUID companyId, int page, int size, String sortBy, String sortDirection) {
        logger.warn("User Service unavailable - using fallback for getCompanyUsers: {}", companyId);
        return ResponseEntity.ok(new PageImpl<>(new ArrayList<>()));
    }

    @Override
    public ResponseEntity<List<UserResponse>> getUsersByRole(UUID companyId, String role) {
        logger.warn("User Service unavailable - using fallback for getUsersByRole: {}", companyId);
        return ResponseEntity.ok(new ArrayList<>());
    }

    @Override
    public ResponseEntity<BulkOperationResponse> createBulkUsers(BulkUserCreateRequest request) {
        logger.warn("User Service unavailable - using fallback for createBulkUsers");
        return ResponseEntity.ok(com.fleetmanagement.companyservice.dto.response.BulkOperationResponse.builder()
                .successful(0)
                .failed(request.getUsers() != null ? request.getUsers().size() : 0)
                .errors(List.of("User service unavailable"))
                .build());
    }

    @Override
    public ResponseEntity<BulkOperationResponse> bulkUpdateUsers(BulkUserUpdateRequest request) {
        logger.warn("User Service unavailable - using fallback for bulkUpdateUsers");
        return ResponseEntity.ok(BulkOperationResponse.builder()
                .successful(0)
                .failed(request.getUsers() != null ? request.getUsers().size() : 0)
                .errors(List.of("User service unavailable"))
                .build());
    }

    @Override
    public ResponseEntity<BulkOperationResponse> bulkDeleteUsers(UUID companyId, List<UUID> userIds) {
        logger.warn("User Service unavailable - using fallback for bulkDeleteUsers: {}", companyId);
        return ResponseEntity.ok(BulkOperationResponse.builder()
                .successful(0)
                .failed(userIds != null ? userIds.size() : 0)
                .errors(List.of("User service unavailable"))
                .build());
    }

    @Override
    public ResponseEntity<UserResponse> getUserById(UUID userId) {
        logger.warn("User Service unavailable - using fallback for getUserById: {}", userId);
        return ResponseEntity.ok(UserResponse.builder()
                .id(userId)
                .username("unknown")
                .email("unknown@company.com")
                .firstName("Unknown")
                .lastName("User")
                .build());
    }

    @Override
    public ResponseEntity<Boolean> validateUserBelongsToCompany(UUID userId, UUID companyId) {
        logger.warn("User Service unavailable - using fallback for validateUserBelongsToCompany: {}", userId);
        return ResponseEntity.ok(false);
    }

    @Override
    public ResponseEntity<UserResponse> updateUserCompany(UUID userId, UUID companyId) {
        logger.warn("User Service unavailable - using fallback for updateUserCompany: {}", userId);
        return ResponseEntity.ok(UserResponse.builder()
                .id(userId)
                .companyId(companyId)
                .build());
    }

    @Override
    public ResponseEntity<UserStatisticsResponse> getUserStatistics(UUID companyId) {
        logger.warn("User Service unavailable - using fallback for getUserStatistics: {}", companyId);
        return ResponseEntity.ok(UserStatisticsResponse.builder()
                .totalUsers(0)
                .activeUsers(0)
                .driverCount(0)
                .build());
    }

    @Override
    public ResponseEntity<BulkValidationResponse> validateBulkUserCreation(UUID companyId, int userCount) {
        logger.warn("User Service unavailable - using fallback for validateBulkUserCreation: {}", companyId);
        return ResponseEntity.ok(BulkValidationResponse.builder()
                .canCreate(false)
                .message("Service unavailable")
                .build());
    }
}