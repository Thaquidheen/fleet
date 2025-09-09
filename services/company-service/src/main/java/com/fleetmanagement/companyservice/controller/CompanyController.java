package com.fleetmanagement.companyservice.controller;

import com.fleetmanagement.companyservice.domain.enums.CompanyStatus;
import com.fleetmanagement.companyservice.domain.enums.SubscriptionPlan;
import com.fleetmanagement.companyservice.dto.request.BulkUserOperationRequest;
import com.fleetmanagement.companyservice.dto.request.CreateCompanyRequest;
import com.fleetmanagement.companyservice.dto.request.UpdateCompanyRequest;
import com.fleetmanagement.companyservice.dto.response.*;
import com.fleetmanagement.companyservice.service.CompanyService;
import com.fleetmanagement.companyservice.service.CompanyUserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
 * Company Controller
 *
 * REST API endpoints for company management operations including:
 * - Company CRUD operations
 * - User management and validation
 * - Subscription management
 * - Bulk user operations
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
            @ApiResponse(responseCode = "400", description = "Invalid company data")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CompanyResponse>> createCompany(
            @Valid @RequestBody CreateCompanyRequest request,
            Authentication authentication) {

        logger.info("Create company request for name: {}", request.getName());

        UUID createdBy = getUserIdFromAuth(authentication);
        CompanyResponse response = companyService.createCompany(request, createdBy);

        ApiResponse<CompanyResponse> apiResponse = ApiResponse.success(
                response,
                "Company created successfully"
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping
    @Operation(summary = "Get all companies", description = "Retrieve all companies with pagination (SUPER_ADMIN only)")
    @SwaggerApiResponse(responseCode = "200", description = "Companies retrieved successfully")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<CompanyResponse>>> getAllCompanies(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        logger.info("Get all companies request - page: {}, size: {}", page, size);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CompanyResponse> response = companyService.getAllCompanies(pageable);

        ApiResponse<Page<CompanyResponse>> apiResponse = ApiResponse.success(
                response,
                "Companies retrieved successfully"
        );

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{companyId}")
    @Operation(summary = "Get company by ID", description = "Retrieve company information by ID")
    @SwaggerApiResponse(responseCode = "200", description = "Company found")
    @SwaggerApiResponse(responseCode = "404", description = "Company not found")
    @PreAuthorize("hasRole('SUPER_ADMIN') or @companyService.canAccessCompany(authentication.name, #companyId)")
    public ResponseEntity<ApiResponse<CompanyResponse>> getCompanyById(@PathVariable UUID companyId) {
        logger.info("Get company request for ID: {}", companyId);

        CompanyResponse response = companyService.getCompanyById(companyId);

        ApiResponse<CompanyResponse> apiResponse = ApiResponse.success(
                response,
                "Company retrieved successfully"
        );

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/subdomain/{subdomain}")
    @Operation(summary = "Get company by subdomain", description = "Retrieve company information by subdomain")
    @SwaggerApiResponse(responseCode = "200", description = "Company found")
    @SwaggerApiResponse(responseCode = "404", description = "Company not found")
    public ResponseEntity<ApiResponse<CompanyResponse>> getCompanyBySubdomain(@PathVariable String subdomain) {
        logger.info("Get company request for subdomain: {}", subdomain);

        CompanyResponse response = companyService.getCompanyBySubdomain(subdomain);

        ApiResponse<CompanyResponse> apiResponse = ApiResponse.success(
                response,
                "Company retrieved successfully"
        );

        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{companyId}")
    @Operation(summary = "Update company", description = "Update company information")
    @SwaggerApiResponse(responseCode = "200", description = "Company updated successfully")
    @SwaggerApiResponse(responseCode = "404", description = "Company not found")
    @SwaggerApiResponse(responseCode = "400", description = "Invalid company data")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<CompanyResponse>> updateCompany(
            @PathVariable UUID companyId,
            @Valid @RequestBody UpdateCompanyRequest request,
            Authentication authentication) {

        logger.info("Update company request for ID: {}", companyId);

        // Validate access
        validateCompanyAccess(companyId, authentication);

        UUID updatedBy = getUserIdFromAuth(authentication);
        CompanyResponse response = companyService.updateCompany(companyId, request, updatedBy);

        ApiResponse<CompanyResponse> apiResponse = ApiResponse.success(
                response,
                "Company updated successfully"
        );

        return ResponseEntity.ok(apiResponse);
    }

    // ==================== SEARCH AND FILTERING ====================

    @GetMapping("/search")
    @Operation(summary = "Search companies", description = "Search companies by name, email, or industry")
    @SwaggerApiResponse(responseCode = "200", description = "Search completed successfully")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<CompanyResponse>>> searchCompanies(
            @Parameter(description = "Search term") @RequestParam String q,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {

        logger.info("Search companies request with term: {}", q);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CompanyResponse> response = companyService.searchCompanies(q, pageable);

        ApiResponse<Page<CompanyResponse>> apiResponse = ApiResponse.success(
                response,
                "Search completed successfully"
        );

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get companies by status", description = "Retrieve companies by status")
    @SwaggerApiResponse(responseCode = "200", description = "Companies retrieved successfully")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<CompanyResponse>>> getCompaniesByStatus(
            @PathVariable CompanyStatus status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        logger.info("Get companies by status request for status: {}", status);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CompanyResponse> response = companyService.getCompaniesByStatus(status, pageable);

        ApiResponse<Page<CompanyResponse>> apiResponse = ApiResponse.success(
                response,
                "Companies retrieved successfully"
        );

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active companies", description = "Retrieve all active companies")
    @SwaggerApiResponse(responseCode = "200", description = "Active companies retrieved successfully")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<CompanyResponse>>> getActiveCompanies(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {

        logger.info("Get active companies request");

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CompanyResponse> response = companyService.getActiveCompanies(pageable);

        ApiResponse<Page<CompanyResponse>> apiResponse = ApiResponse.success(
                response,
                "Active companies retrieved successfully"
        );

        return ResponseEntity.ok(apiResponse);
    }

    // ==================== SUBSCRIPTION MANAGEMENT ====================

    @PutMapping("/{companyId}/subscription")
    @Operation(summary = "Update subscription plan", description = "Update company subscription plan (SUPER_ADMIN only)")
    @SwaggerApiResponse(responseCode = "200", description = "Subscription updated successfully")
    @SwaggerApiResponse(responseCode = "404", description = "Company not found")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CompanyResponse>> updateSubscriptionPlan(
            @PathVariable UUID companyId,
            @RequestParam SubscriptionPlan plan,
            Authentication authentication) {

        logger.info("Update subscription plan request for company: {} to plan: {}", companyId, plan);

        UUID updatedBy = getUserIdFromAuth(authentication);
        CompanyResponse response = companyService.updateSubscriptionPlan(companyId, plan, updatedBy);

        ApiResponse<CompanyResponse> apiResponse = ApiResponse.success(
                response,
                "Subscription updated successfully"
        );

        return ResponseEntity.ok(apiResponse);
    }

    // ==================== COMPANY STATUS MANAGEMENT ====================

    @PutMapping("/{companyId}/suspend")
    @Operation(summary = "Suspend company", description = "Suspend company operations (SUPER_ADMIN only)")
    @SwaggerApiResponse(responseCode = "200", description = "Company suspended successfully")
    @SwaggerApiResponse(responseCode = "404", description = "Company not found")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, String>>> suspendCompany(
            @PathVariable UUID companyId,
            Authentication authentication) {

        logger.info("Suspend company request for ID: {}", companyId);

        UUID updatedBy = getUserIdFromAuth(authentication);
        companyService.suspendCompany(companyId, updatedBy);

        Map<String, String> result = new HashMap<>();
        result.put("message", "Company suspended successfully");
        result.put("companyId", companyId.toString());

        ApiResponse<Map<String, String>> response = ApiResponse.success(
                result,
                "Company suspended successfully"
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{companyId}/activate")
    @Operation(summary = "Activate company", description = "Activate company operations (SUPER_ADMIN only)")
    @SwaggerApiResponse(responseCode = "200", description = "Company activated successfully")
    @SwaggerApiResponse(responseCode = "404", description = "Company not found")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, String>>> activateCompany(
            @PathVariable UUID companyId,
            Authentication authentication) {

        logger.info("Activate company request for ID: {}", companyId);

        UUID updatedBy = getUserIdFromAuth(authentication);
        companyService.activateCompany(companyId, updatedBy);

        Map<String, String> result = new HashMap<>();
        result.put("message", "Company activated successfully");
        result.put("companyId", companyId.toString());

        ApiResponse<Map<String, String>> response = ApiResponse.success(
                result,
                "Company activated successfully"
        );

        return ResponseEntity.ok(response);
    }

    // ==================== USER MANAGEMENT ENDPOINTS ====================

    /**
     * Validate user limit for company (called by User Service)
     */
    @GetMapping("/{companyId}/validation/user-limit")
    @Operation(summary = "Validate user limit", description = "Check if company can add more users based on subscription")
    @SwaggerApiResponse(responseCode = "200", description = "User limit validation completed")
    @SwaggerApiResponse(responseCode = "404", description = "Company not found")
    public ResponseEntity<ApiResponse<CompanyValidationResponse>> validateUserLimit(
            @PathVariable @Parameter(description = "Company ID") UUID companyId) {

        logger.debug("Validate user limit request for company: {}", companyId);

        CompanyValidationResponse validation = userManagementService.validateUserLimit(companyId);

        ApiResponse<CompanyValidationResponse> response = ApiResponse.success(
                validation,
                "User limit validation completed"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Increment user count for company (called by User Service)
     */
    @PostMapping("/{companyId}/users/increment")
    @Operation(summary = "Increment user count", description = "Increment the user count for company")
    @SwaggerApiResponse(responseCode = "200", description = "User count incremented successfully")
    @SwaggerApiResponse(responseCode = "404", description = "Company not found")
    public ResponseEntity<ApiResponse<Void>> incrementUserCount(
            @PathVariable @Parameter(description = "Company ID") UUID companyId) {

        logger.info("Increment user count request for company: {}", companyId);

        userManagementService.incrementUserCount(companyId);

        ApiResponse<Void> response = ApiResponse.success(
                null,
                "User count incremented successfully"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Decrement user count for company (called by User Service)
     */
    @PostMapping("/{companyId}/users/decrement")
    @Operation(summary = "Decrement user count", description = "Decrement the user count for company")
    @SwaggerApiResponse(responseCode = "200", description = "User count decremented successfully")
    @SwaggerApiResponse(responseCode = "404", description = "Company not found")
    public ResponseEntity<ApiResponse<Void>> decrementUserCount(
            @PathVariable @Parameter(description = "Company ID") UUID companyId) {

        logger.info("Decrement user count request for company: {}", companyId);

        userManagementService.decrementUserCount(companyId);

        ApiResponse<Void> response = ApiResponse.success(
                null,
                "User count decremented successfully"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get all users for company
     */
    @GetMapping("/{companyId}/users")
    @Operation(summary = "Get company users", description = "Retrieve all users for a company with pagination")
    @SwaggerApiResponse(responseCode = "200", description = "Company users retrieved successfully")
    @SwaggerApiResponse(responseCode = "404", description = "Company not found")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getCompanyUsers(
            @PathVariable @Parameter(description = "Company ID") UUID companyId,
            @RequestParam(defaultValue = "0") @Parameter(description = "Page number") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "Page size") int size,
            @RequestParam(defaultValue = "createdAt") @Parameter(description = "Sort field") String sortBy,
            @RequestParam(defaultValue = "desc") @Parameter(description = "Sort direction") String sortDirection,
            Authentication authentication) {

        logger.debug("Get company users request for company: {} (page: {}, size: {})", companyId, page, size);

        // Validate access
        validateCompanyAccess(companyId, authentication);

        PagedResponse<UserResponse> users = userManagementService.getCompanyUsers(companyId, page, size, sortBy, sortDirection);

        ApiResponse<PagedResponse<UserResponse>> response = ApiResponse.success(
                users,
                "Company users retrieved successfully"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Perform bulk user operations
     */
    @PostMapping("/{companyId}/users/bulk-operations")
    @Operation(summary = "Bulk user operations", description = "Perform bulk operations on users (create, update, delete)")
    @SwaggerApiResponse(responseCode = "200", description = "Bulk operation completed")
    @SwaggerApiResponse(responseCode = "400", description = "Invalid bulk operation request")
    @SwaggerApiResponse(responseCode = "404", description = "Company not found")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<BulkOperationResponse>> performBulkUserOperations(
            @PathVariable @Parameter(description = "Company ID") UUID companyId,
            @Valid @RequestBody BulkUserOperationRequest request,
            Authentication authentication) {

        logger.info("Bulk user operations request for company: {} (operation: {})", companyId, request.getOperation());

        // Validate access
        validateCompanyAccess(companyId, authentication);

        BulkOperationResponse result = userManagementService.performBulkUserOperations(companyId, request);

        ApiResponse<BulkOperationResponse> response = ApiResponse.success(
                result,
                "Bulk operation completed successfully"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Synchronize user count with User Service
     */
    @PostMapping("/{companyId}/users/synchronize")
    @Operation(summary = "Synchronize user count", description = "Synchronize user count with User Service")
    @SwaggerApiResponse(responseCode = "200", description = "User count synchronized successfully")
    @SwaggerApiResponse(responseCode = "404", description = "Company not found")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> synchronizeUserCount(
            @PathVariable @Parameter(description = "Company ID") UUID companyId,
            Authentication authentication) {

        logger.info("Synchronize user count request for company: {}", companyId);

        // Validate access
        validateCompanyAccess(companyId, authentication);

        userManagementService.synchronizeUserCount(companyId);

        ApiResponse<Void> response = ApiResponse.success(
                null,
                "User count synchronized successfully"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get user statistics for company
     */
    @GetMapping("/{companyId}/users/statistics")
    @Operation(summary = "Get user statistics", description = "Get comprehensive user statistics for company")
    @SwaggerApiResponse(responseCode = "200", description = "User statistics retrieved successfully")
    @SwaggerApiResponse(responseCode = "404", description = "Company not found")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<UserStatisticsResponse>> getUserStatistics(
            @PathVariable @Parameter(description = "Company ID") UUID companyId,
            Authentication authentication) {

        logger.debug("Get user statistics request for company: {}", companyId);

        // Validate access
        validateCompanyAccess(companyId, authentication);

        UserStatisticsResponse statistics = userManagementService.getUserStatistics(companyId);

        ApiResponse<UserStatisticsResponse> response = ApiResponse.success(
                statistics,
                "User statistics retrieved successfully"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get company user counts summary
     */
    @GetMapping("/{companyId}/users/summary")
    @Operation(summary = "Get user count summary", description = "Get quick summary of user counts for company")
    @SwaggerApiResponse(responseCode = "200", description = "User summary retrieved successfully")
    @SwaggerApiResponse(responseCode = "404", description = "Company not found")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<UserCountSummaryResponse>> getUserCountSummary(
            @PathVariable @Parameter(description = "Company ID") UUID companyId,
            Authentication authentication) {

        logger.debug("Get user count summary request for company: {}", companyId);

        // Validate access
        validateCompanyAccess(companyId, authentication);

        CompanyValidationResponse validation = userManagementService.validateUserLimit(companyId);
        UserStatisticsResponse statistics = userManagementService.getUserStatistics(companyId);

        UserCountSummaryResponse summary = UserCountSummaryResponse.builder()
                .companyId(companyId)
                .totalUsers(statistics.getTotalUsers())
                .activeUsers(statistics.getActiveUsers())
                .driverCount(statistics.getDriverCount())
                .maxAllowedUsers(validation.getMaxUsers())
                .availableSlots(validation.getAvailableSlots())
                .subscriptionPlan(validation.getSubscriptionPlan())
                .isAtLimit(!validation.isCanAddUser())
                .utilizationPercentage(calculateUtilizationPercentage(statistics.getTotalUsers(), validation.getMaxUsers()))
                .build();

        ApiResponse<UserCountSummaryResponse> response = ApiResponse.success(
                summary,
                "User count summary retrieved successfully"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Validate company subscription can support bulk user creation
     */
    @PostMapping("/{companyId}/users/validate-bulk-creation")
    @Operation(summary = "Validate bulk user creation", description = "Check if company can create specified number of users")
    @SwaggerApiResponse(responseCode = "200", description = "Bulk creation validation completed")
    @SwaggerApiResponse(responseCode = "404", description = "Company not found")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<BulkCreationValidationResponse>> validateBulkUserCreation(
            @PathVariable @Parameter(description = "Company ID") UUID companyId,
            @RequestParam @Parameter(description = "Number of users to create") int userCount,
            Authentication authentication) {

        logger.debug("Validate bulk user creation for company: {} (count: {})", companyId, userCount);

        // Validate access
        validateCompanyAccess(companyId, authentication);

        CompanyValidationResponse currentValidation = userManagementService.validateUserLimit(companyId);

        boolean canCreate = currentValidation.getAvailableSlots() >= userCount;
        String message = canCreate ?
                String.format("Can create %d users (%d slots available)", userCount, currentValidation.getAvailableSlots()) :
                String.format("Cannot create %d users (only %d slots available)", userCount, currentValidation.getAvailableSlots());

        BulkCreationValidationResponse validation = BulkCreationValidationResponse.builder()
                .canCreate(canCreate)
                .requestedCount(userCount)
                .currentUserCount(currentValidation.getCurrentUsers())
                .maxAllowedUsers(currentValidation.getMaxUsers())
                .availableSlots(currentValidation.getAvailableSlots())
                .subscriptionPlan(currentValidation.getSubscriptionPlan())
                .message(message)
                .build();

        ApiResponse<BulkCreationValidationResponse> response = ApiResponse.success(
                validation,
                "Bulk creation validation completed"
        );

        return ResponseEntity.ok(response);
    }

    // ==================== HELPER METHODS ====================

    private void validateCompanyAccess(UUID companyId, Authentication authentication) {
        // Extract user's company from authentication
        UUID userCompanyId = getCompanyIdFromAuth(authentication);

        // Super admins can access any company
        if (isSuperAdmin(authentication)) {
            return;
        }

        // Other users can only access their own company
        if (!companyId.equals(userCompanyId)) {
            throw new SecurityException("Access denied: Cannot access company " + companyId);
        }
    }

    private UUID getUserIdFromAuth(Authentication authentication) {
        // Extract user ID from JWT token or security context
        // Implementation depends on your security setup
        return UUID.fromString(authentication.getName());
    }

    private UUID getCompanyIdFromAuth(Authentication authentication) {
        // Extract company ID from JWT token or security context
        // Implementation depends on your security setup
        return UUID.fromString(authentication.getDetails().toString());
    }

    private boolean isSuperAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }

    private double calculateUtilizationPercentage(int currentUsers, int maxUsers) {
        if (maxUsers == -1) return 0.0; // Unlimited
        if (maxUsers == 0) return 0.0;
        return (double) currentUsers / maxUsers * 100.0;
    }

    // Supporting response DTOs
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserCountSummaryResponse {
        private UUID companyId;
        private int totalUsers;
        private int activeUsers;
        private int driverCount;
        private int maxAllowedUsers;
        private int availableSlots;
        private String subscriptionPlan;
        private boolean isAtLimit;
        private double utilizationPercentage;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BulkCreationValidationResponse {
        private boolean canCreate;
        private int requestedCount;
        private int currentUserCount;
        private int maxAllowedUsers;
        private int availableSlots;
        private String subscriptionPlan;
        private String message;
        private java.util.List<String> warnings;
    }
}