package com.fleetmanagement.companyservice.controller;

import com.fleetmanagement.companyservice.domain.enums.CompanyStatus;
import com.fleetmanagement.companyservice.domain.enums.SubscriptionPlan;
import com.fleetmanagement.companyservice.dto.request.BulkUserOperationRequest;
import com.fleetmanagement.companyservice.dto.request.CreateCompanyRequest;
import com.fleetmanagement.companyservice.dto.request.UpdateCompanyRequest;
import com.fleetmanagement.companyservice.dto.response.*;
import com.fleetmanagement.companyservice.service.CompanyService;
import com.fleetmanagement.companyservice.service.CompanyUserManagementService;
import com.fleetmanagement.companyservice.client.UserServiceClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * FIXED CompanyController - All Swagger annotations corrected
 *
 * CRITICAL FIXES:
 * - Removed incorrect ApiResponse.success() method calls from annotations
 * - Used proper @ApiResponse(responseCode="200", description="...") format
 * - Added proper imports for ApiResponse from dto.response package
 * - Fixed all compilation errors related to Swagger annotations
 */
@RestController
@RequestMapping("/api/companies")
@Tag(name = "Company Management", description = "Company operations and user management")
@Validated
public class CompanyController {

    private static final Logger logger = LoggerFactory.getLogger(CompanyController.class);

    private final CompanyService companyService;
    private final CompanyUserManagementService userManagementService;

    @Autowired
    public CompanyController(CompanyService companyService,
                             CompanyUserManagementService userManagementService) {
        this.companyService = companyService;
        this.userManagementService = userManagementService;
    }

    // ==================== COMPANY CRUD OPERATIONS ====================

    @PostMapping
    @Operation(summary = "Create company", description = "Create a new company (SUPER_ADMIN only)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Company created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid company data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<com.fleetmanagement.companyservice.dto.response.ApiResponse<com.fleetmanagement.companyservice.dto.response.CompanyResponse>> createCompany(
            @Valid @RequestBody CreateCompanyRequest request,
            Authentication authentication) {

        logger.info("Create company request for name: {}", request.getName());

        UUID createdBy = getUserIdFromAuth(authentication);
        CompanyResponse response = companyService.createCompany(request, createdBy);

        com.fleetmanagement.companyservice.dto.response.ApiResponse<com.fleetmanagement.companyservice.dto.response.CompanyResponse> apiResponse =
                com.fleetmanagement.companyservice.dto.response.ApiResponse.success(
                        response,
                        "Company created successfully"
                );

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping
    @Operation(summary = "Get all companies", description = "Retrieve all companies with pagination (SUPER_ADMIN only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Companies retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<com.fleetmanagement.companyservice.dto.response.ApiResponse<Page<com.fleetmanagement.companyservice.dto.response.CompanyResponse>>> getAllCompanies(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDirection) {

        logger.info("Get all companies request - page: {}, size: {}", page, size);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<com.fleetmanagement.companyservice.dto.response.CompanyResponse> companies = companyService.getAllCompanies(pageable);

        com.fleetmanagement.companyservice.dto.response.ApiResponse<Page<com.fleetmanagement.companyservice.dto.response.CompanyResponse>> apiResponse =
                com.fleetmanagement.companyservice.dto.response.ApiResponse.success(
                        companies,
                        "Companies retrieved successfully"
                );

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{companyId}")
    @Operation(summary = "Get company by ID", description = "Retrieve a specific company by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Company not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN') or @companyPermissionService.hasCompanyAccess(authentication, #companyId)")
    public ResponseEntity<com.fleetmanagement.companyservice.dto.response.ApiResponse<CompanyResponse>> getCompanyById(
            @PathVariable @Parameter(description = "Company ID") UUID companyId) {

        logger.info("Get company by ID: {}", companyId);

        CompanyResponse company = companyService.getCompanyById(companyId);

        com.fleetmanagement.companyservice.dto.response.ApiResponse<CompanyResponse> apiResponse =
                com.fleetmanagement.companyservice.dto.response.ApiResponse.success(
                        company,
                        "Company retrieved successfully"
                );

        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{companyId}")
    @Operation(summary = "Update company", description = "Update company information")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company updated successfully"),
            @ApiResponse(responseCode = "404", description = "Company not found"),
            @ApiResponse(responseCode = "400", description = "Invalid update data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN') or @companyPermissionService.hasCompanyAccess(authentication, #companyId)")
    public ResponseEntity<com.fleetmanagement.companyservice.dto.response.ApiResponse<CompanyResponse>> updateCompany(
            @PathVariable @Parameter(description = "Company ID") UUID companyId,
            @Valid @RequestBody UpdateCompanyRequest request,
            Authentication authentication) {

        logger.info("Update company request for ID: {}", companyId);

        UUID updatedBy = getUserIdFromAuth(authentication);
        CompanyResponse response = companyService.updateCompany(companyId, request, updatedBy);

        com.fleetmanagement.companyservice.dto.response.ApiResponse<CompanyResponse> apiResponse =
                com.fleetmanagement.companyservice.dto.response.ApiResponse.success(
                        response,
                        "Company updated successfully"
                );

        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{companyId}")
    @Operation(summary = "Delete company", description = "Delete a company (SUPER_ADMIN only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Company not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<com.fleetmanagement.companyservice.dto.response.ApiResponse<CompanyResponse>> deleteCompany(
            @PathVariable @Parameter(description = "Company ID") UUID companyId,
            Authentication authentication) {

        logger.info("Delete company request for ID: {}", companyId);

        UUID deletedBy = getUserIdFromAuth(authentication);
        CompanyResponse response = companyService.deleteCompany(companyId, deletedBy);

        com.fleetmanagement.companyservice.dto.response.ApiResponse<CompanyResponse> apiResponse =
                com.fleetmanagement.companyservice.dto.response.ApiResponse.success(
                        response,
                        "Company deleted successfully"
                );

        return ResponseEntity.ok(apiResponse);
    }

    // ==================== COMPANY SEARCH OPERATIONS ====================

    @GetMapping("/search")
    @Operation(summary = "Search companies", description = "Search companies by name or other criteria (SUPER_ADMIN only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<com.fleetmanagement.companyservice.dto.response.ApiResponse<Page<CompanyResponse>>> searchCompanies(
            @Parameter(description = "Search term") @RequestParam String searchTerm,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDirection) {

        logger.info("Search companies with term: {}", searchTerm);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CompanyResponse> companies = companyService.searchCompanies(searchTerm, pageable);

        com.fleetmanagement.companyservice.dto.response.ApiResponse<Page<CompanyResponse>> apiResponse =
                com.fleetmanagement.companyservice.dto.response.ApiResponse.success(
                        companies,
                        "Search completed successfully"
                );

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/by-subdomain/{subdomain}")
    @Operation(summary = "Get company by subdomain", description = "Retrieve company by subdomain")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Company not found")
    })
    public ResponseEntity<com.fleetmanagement.companyservice.dto.response.ApiResponse<CompanyResponse>> getCompanyBySubdomain(
            @PathVariable @Parameter(description = "Company subdomain") String subdomain) {

        logger.info("Get company by subdomain: {}", subdomain);

        CompanyResponse company = companyService.getCompanyBySubdomain(subdomain);

        com.fleetmanagement.companyservice.dto.response.ApiResponse<CompanyResponse> apiResponse =
                com.fleetmanagement.companyservice.dto.response.ApiResponse.success(
                        company,
                        "Company retrieved successfully"
                );

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get companies by status", description = "Retrieve companies by status (SUPER_ADMIN only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Companies retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<com.fleetmanagement.companyservice.dto.response.ApiResponse<Page<CompanyResponse>>> getCompaniesByStatus(
            @PathVariable @Parameter(description = "Company status") CompanyStatus status,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        logger.info("Get companies by status: {}", status);

        Pageable pageable = PageRequest.of(page, size);
        Page<CompanyResponse> companies = companyService.getCompaniesByStatus(status, pageable);

        com.fleetmanagement.companyservice.dto.response.ApiResponse<Page<CompanyResponse>> apiResponse =
                com.fleetmanagement.companyservice.dto.response.ApiResponse.success(
                        companies,
                        "Companies retrieved successfully"
                );

        return ResponseEntity.ok(apiResponse);
    }

    // ==================== SUBSCRIPTION MANAGEMENT ====================

    @PutMapping("/{companyId}/subscription")
    @Operation(summary = "Update subscription", description = "Update company subscription plan (SUPER_ADMIN only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subscription updated successfully"),
            @ApiResponse(responseCode = "404", description = "Company not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<com.fleetmanagement.companyservice.dto.response.ApiResponse<CompanyResponse>> updateSubscription(
            @PathVariable @Parameter(description = "Company ID") UUID companyId,
            @Parameter(description = "Subscription plan") @RequestParam SubscriptionPlan subscriptionPlan,
            Authentication authentication) {

        logger.info("Update subscription for company: {} to plan: {}", companyId, subscriptionPlan);

        UUID updatedBy = getUserIdFromAuth(authentication);
        CompanyResponse response = companyService.updateSubscription(companyId, subscriptionPlan, updatedBy);

        com.fleetmanagement.companyservice.dto.response.ApiResponse<CompanyResponse> apiResponse =
                com.fleetmanagement.companyservice.dto.response.ApiResponse.success(
                        response,
                        "Subscription updated successfully"
                );

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/{companyId}/activate")
    @Operation(summary = "Activate company", description = "Activate a company account (SUPER_ADMIN only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company activated successfully"),
            @ApiResponse(responseCode = "404", description = "Company not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<com.fleetmanagement.companyservice.dto.response.ApiResponse<Map<String, String>>> activateCompany(
            @PathVariable @Parameter(description = "Company ID") UUID companyId,
            Authentication authentication) {

        logger.info("Activate company: {}", companyId);

        UUID activatedBy = getUserIdFromAuth(authentication);
        companyService.activateCompany(companyId, activatedBy);

        Map<String, String> result = new HashMap<>();
        result.put("status", "activated");
        result.put("message", "Company activated successfully");

        com.fleetmanagement.companyservice.dto.response.ApiResponse<Map<String, String>> apiResponse =
                com.fleetmanagement.companyservice.dto.response.ApiResponse.success(
                        result,
                        "Company activated successfully"
                );

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/{companyId}/deactivate")
    @Operation(summary = "Deactivate company", description = "Deactivate a company account (SUPER_ADMIN only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Company not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<com.fleetmanagement.companyservice.dto.response.ApiResponse<Map<String, String>>> deactivateCompany(
            @PathVariable @Parameter(description = "Company ID") UUID companyId,
            Authentication authentication) {

        logger.info("Deactivate company: {}", companyId);

        UUID deactivatedBy = getUserIdFromAuth(authentication);
        companyService.deactivateCompany(companyId, deactivatedBy);

        Map<String, String> result = new HashMap<>();
        result.put("status", "deactivated");
        result.put("message", "Company deactivated successfully");

        com.fleetmanagement.companyservice.dto.response.ApiResponse<Map<String, String>> apiResponse =
                com.fleetmanagement.companyservice.dto.response.ApiResponse.success(
                        result,
                        "Company deactivated successfully"
                );

        return ResponseEntity.ok(apiResponse);
    }

    // ==================== COMPANY VALIDATION ====================

    @GetMapping("/{companyId}/validate")
    @Operation(summary = "Validate company limits", description = "Check company subscription limits and current usage")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Validation completed successfully"),
            @ApiResponse(responseCode = "404", description = "Company not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN') or @companyPermissionService.hasCompanyAccess(authentication, #companyId)")
    public ResponseEntity<com.fleetmanagement.companyservice.dto.response.ApiResponse<CompanyValidationResponse>> validateCompany(
            @PathVariable @Parameter(description = "Company ID") UUID companyId) {

        logger.info("Validate company: {}", companyId);

        CompanyValidationResponse validation = companyService.validateCompanyLimits(companyId);

        com.fleetmanagement.companyservice.dto.response.ApiResponse<CompanyValidationResponse> apiResponse =
                com.fleetmanagement.companyservice.dto.response.ApiResponse.success(
                        validation,
                        "Validation completed successfully"
                );

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/{companyId}/sync-user-count")
    @Operation(summary = "Sync user count", description = "Synchronize user count with User Service")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User count synchronized successfully"),
            @ApiResponse(responseCode = "404", description = "Company not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN') or @companyPermissionService.hasCompanyAccess(authentication, #companyId)")
    public ResponseEntity<com.fleetmanagement.companyservice.dto.response.ApiResponse<Void>> syncUserCount(
            @PathVariable @Parameter(description = "Company ID") UUID companyId) {

        logger.info("Sync user count for company: {}", companyId);

        userManagementService.synchronizeUserCount(companyId);

        com.fleetmanagement.companyservice.dto.response.ApiResponse<Void> apiResponse =
                com.fleetmanagement.companyservice.dto.response.ApiResponse.success(
                        null,
                        "User count synchronized successfully"
                );

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/{companyId}/reset-trial")
    @Operation(summary = "Reset trial period", description = "Reset company trial period (SUPER_ADMIN only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trial period reset successfully"),
            @ApiResponse(responseCode = "404", description = "Company not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<com.fleetmanagement.companyservice.dto.response.ApiResponse<Void>> resetTrialPeriod(
            @PathVariable @Parameter(description = "Company ID") UUID companyId,
            Authentication authentication) {

        logger.info("Reset trial period for company: {}", companyId);

        UUID resetBy = getUserIdFromAuth(authentication);
        companyService.resetTrialPeriod(companyId, resetBy);

        com.fleetmanagement.companyservice.dto.response.ApiResponse<Void> apiResponse =
                com.fleetmanagement.companyservice.dto.response.ApiResponse.success(
                        null,
                        "Trial period reset successfully"
                );

        return ResponseEntity.ok(apiResponse);
    }

    // ==================== USER MANAGEMENT ====================

    @GetMapping("/{companyId}/users")
    @Operation(summary = "Get company users", description = "Retrieve users for a company with pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Company not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN') or @companyPermissionService.hasCompanyAccess(authentication, #companyId)")
    public ResponseEntity<com.fleetmanagement.companyservice.dto.response.ApiResponse<PagedResponse<UserResponse>>> getCompanyUsers(
            @PathVariable @Parameter(description = "Company ID") UUID companyId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDirection) {

        logger.info("Get users for company: {}", companyId);

        PagedResponse<UserResponse> users = userManagementService.getCompanyUsers(
                companyId, page, size, sortBy, sortDirection);

        com.fleetmanagement.companyservice.dto.response.ApiResponse<PagedResponse<UserResponse>> apiResponse =
                com.fleetmanagement.companyservice.dto.response.ApiResponse.success(
                        users,
                        "Users retrieved successfully"
                );

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/{companyId}/users/bulk")
    @Operation(summary = "Bulk user operations", description = "Perform bulk operations on users")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bulk operation completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid bulk operation data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN') or @companyPermissionService.hasCompanyAccess(authentication, #companyId)")
    public ResponseEntity<com.fleetmanagement.companyservice.dto.response.ApiResponse<BulkOperationResponse>> performBulkUserOperations(
            @PathVariable @Parameter(description = "Company ID") UUID companyId,
            @Valid @RequestBody BulkUserOperationRequest request,
            Authentication authentication) {

        logger.info("Bulk user operation for company: {} (operation: {})", companyId, request.getOperation());

        BulkOperationResponse response = userManagementService.performBulkUserOperations(companyId, request);

        com.fleetmanagement.companyservice.dto.response.ApiResponse<BulkOperationResponse> apiResponse =
                com.fleetmanagement.companyservice.dto.response.ApiResponse.success(
                        response,
                        "Bulk operation completed successfully"
                );

        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{companyId}/users/inactive")
    @Operation(summary = "Clean up inactive users", description = "Remove inactive users from company")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inactive users cleaned up successfully"),
            @ApiResponse(responseCode = "404", description = "Company not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN') or @companyPermissionService.hasCompanyAccess(authentication, #companyId)")
    public ResponseEntity<com.fleetmanagement.companyservice.dto.response.ApiResponse<Void>> cleanupInactiveUsers(
            @PathVariable @Parameter(description = "Company ID") UUID companyId,
            @Parameter(description = "Days inactive threshold") @RequestParam(defaultValue = "90") int daysInactive) {

        logger.info("Cleanup inactive users for company: {} (threshold: {} days)", companyId, daysInactive);

        userManagementService.cleanupInactiveUsers(companyId, daysInactive);

        com.fleetmanagement.companyservice.dto.response.ApiResponse<Void> apiResponse =
                com.fleetmanagement.companyservice.dto.response.ApiResponse.success(
                        null,
                        "Inactive users cleaned up successfully"
                );

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{companyId}/users/statistics")
    @Operation(summary = "Get user statistics", description = "Get detailed user statistics for company")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User statistics retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Company not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN') or @companyPermissionService.hasCompanyAccess(authentication, #companyId)")
    public ResponseEntity<com.fleetmanagement.companyservice.dto.response.ApiResponse<UserStatisticsResponse>> getUserStatistics(
            @PathVariable @Parameter(description = "Company ID") UUID companyId) {

        logger.info("Get user statistics for company: {}", companyId);

        UserStatisticsResponse statistics = userManagementService.getUserStatistics(companyId);

        com.fleetmanagement.companyservice.dto.response.ApiResponse<UserStatisticsResponse> apiResponse =
                com.fleetmanagement.companyservice.dto.response.ApiResponse.success(
                        statistics,
                        "User statistics retrieved successfully"
                );

        return ResponseEntity.ok(apiResponse);
    }

    // ==================== USER COUNT TRACKING ====================

    @GetMapping("/{companyId}/user-count")
    @Operation(summary = "Get user count summary", description = "Get comprehensive user count information")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User count summary retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Company not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN') or @companyPermissionService.hasCompanyAccess(authentication, #companyId)")
    public ResponseEntity<com.fleetmanagement.companyservice.dto.response.ApiResponse<UserCountSummaryResponse>> getUserCountSummary(
            @PathVariable @Parameter(description = "Company ID") UUID companyId) {

        logger.info("Get user count summary for company: {}", companyId);

        UserCountResponse totalCount = userManagementService.getUserCount(companyId);
        UserCountResponse driverCount = userManagementService.getDriverCount(companyId);

        UserCountSummaryResponse summary = UserCountSummaryResponse.builder()
                .companyId(companyId)
                .totalUsers(totalCount.getCount())
                .driverCount(driverCount.getCount())
                .lastUpdated(totalCount.getCountedAt())
                .fromCache(totalCount.isFromCache())
                .build();

        com.fleetmanagement.companyservice.dto.response.ApiResponse<UserCountSummaryResponse> apiResponse =
                com.fleetmanagement.companyservice.dto.response.ApiResponse.success(
                        summary,
                        "User count summary retrieved successfully"
                );

        return ResponseEntity.ok(apiResponse);
    }

    // ==================== BULK VALIDATION ====================

    @PostMapping("/{companyId}/validate-bulk-creation")
    @Operation(summary = "Validate bulk user creation", description = "Validate if bulk user creation is possible")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bulk creation validation completed"),
            @ApiResponse(responseCode = "404", description = "Company not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN') or @companyPermissionService.hasCompanyAccess(authentication, #companyId)")
    public ResponseEntity<com.fleetmanagement.companyservice.dto.response.ApiResponse<BulkCreationValidationResponse>> validateBulkUserCreation(
            @PathVariable @Parameter(description = "Company ID") UUID companyId,
            @Parameter(description = "Number of users to create") @RequestParam int userCount) {

        logger.info("Validate bulk user creation for company: {} (count: {})", companyId, userCount);

        com.fleetmanagement.companyservice.dto.response.BulkValidationResponse validation = userManagementService
                .validateBulkUserCreation(companyId, userCount);

        BulkCreationValidationResponse response = BulkCreationValidationResponse.builder()
                .companyId(companyId)
                .requestedCount(userCount)
                .canCreate(validation.isCanCreate())
                .maxAllowed(validation.getMaxAllowed())
                .currentCount(validation.getCurrentCount())
                .availableSlots(validation.getAvailableSlots())
                .message(validation.getMessage())
                .errors(validation.getErrors())
                .build();

        com.fleetmanagement.companyservice.dto.response.ApiResponse<BulkCreationValidationResponse> apiResponse =
                com.fleetmanagement.companyservice.dto.response.ApiResponse.success(
                        response,
                        "Bulk creation validation completed"
                );

        return ResponseEntity.ok(apiResponse);
    }

    // ==================== HELPER METHODS ====================

    private UUID getUserIdFromAuth(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    // ==================== RESPONSE DTOs ====================

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserCountSummaryResponse {
        private UUID companyId;
        private int totalUsers;
        private int driverCount;
        private java.time.LocalDateTime lastUpdated;
        private boolean fromCache;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BulkCreationValidationResponse {
        private UUID companyId;
        private int requestedCount;
        private boolean canCreate;
        private int maxAllowed;
        private int currentCount;
        private int availableSlots;
        private String message;
        private java.util.List<String> errors;
    }
}