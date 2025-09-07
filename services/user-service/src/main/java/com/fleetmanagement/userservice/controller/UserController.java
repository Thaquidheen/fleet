package com.fleetmanagement.userservice.controller;

import com.fleetmanagement.userservice.domain.enums.UserRole;
import com.fleetmanagement.userservice.domain.enums.UserStatus;
import com.fleetmanagement.userservice.dto.request.CreateUserRequest;
import com.fleetmanagement.userservice.dto.request.UpdateUserRequest;
import com.fleetmanagement.userservice.dto.request.UserSearchRequest;
import com.fleetmanagement.userservice.dto.response.UserResponse;
import com.fleetmanagement.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@Tag(name = "User Management", description = "User CRUD operations and management")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Create user", description = "Create a new user")
    @ApiResponse(responseCode = "201", description = "User created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid user data")
    @ApiResponse(responseCode = "409", description = "User already exists")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request,
                                                   Authentication authentication) {
        logger.info("Create user request for username: {}", request.getUsername());

        UUID createdBy = UUID.fromString(authentication.getName());
        UserResponse response = userService.createUser(request, createdBy);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieve user information by ID")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN') or @userService.canAccessUser(authentication.name, #userId)")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
        logger.info("Get user request for ID: {}", userId);

        UserResponse response = userService.getUserById(userId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "Get user by username", description = "Retrieve user information by username")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        logger.info("Get user request for username: {}", username);

        UserResponse response = userService.getUserByUsername(username);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email", description = "Retrieve user information by email")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        logger.info("Get user request for email: {}", email);

        UserResponse response = userService.getUserByEmail(email);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update user", description = "Update user information")
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "400", description = "Invalid user data")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN') or @userService.canModifyUser(authentication.name, #userId)")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID userId,
                                                   @Valid @RequestBody UpdateUserRequest request,
                                                   Authentication authentication) {
        logger.info("Update user request for ID: {}", userId);

        UUID updatedBy = UUID.fromString(authentication.getName());
        UserResponse response = userService.updateUserProfile(userId, request);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}/role")
    @Operation(summary = "Update user role", description = "Update user role")
    @ApiResponse(responseCode = "200", description = "User role updated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<UserResponse> updateUserRole(@PathVariable UUID userId,
                                                       @RequestParam UserRole role,
                                                       Authentication authentication) {
        logger.info("Update user role request for ID: {} to role: {}", userId, role);

        UUID updatedBy = UUID.fromString(authentication.getName());
        UserResponse response = userService.updateUserRole(userId, role, updatedBy);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}/status")
    @Operation(summary = "Update user status", description = "Update user status")
    @ApiResponse(responseCode = "200", description = "User status updated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<UserResponse> updateUserStatus(@PathVariable UUID userId,
                                                         @RequestParam UserStatus status,
                                                         Authentication authentication) {
        logger.info("Update user status request for ID: {} to status: {}", userId, status);

        UUID updatedBy = UUID.fromString(authentication.getName());
        UserResponse response = userService.updateUserStatus(userId, status, updatedBy);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user", description = "Delete user (soft delete)")
    @ApiResponse(responseCode = "200", description = "User deleted successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable UUID userId,
                                                          Authentication authentication) {
        logger.info("Delete user request for ID: {}", userId);

        UUID deletedBy = UUID.fromString(authentication.getName());
        userService.deleteUser(userId, deletedBy);

        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    @GetMapping
    @Operation(summary = "Search users", description = "Search and filter users")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<Page<UserResponse>> searchUsers(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) Boolean emailVerified,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        logger.info("Search users request");

        UserSearchRequest searchRequest = new UserSearchRequest();
        searchRequest.setSearchTerm(searchTerm);
        searchRequest.setCompanyId(companyId);
        searchRequest.setRole(role);
        searchRequest.setStatus(status);
        searchRequest.setEmailVerified(emailVerified);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserResponse> response = userService.searchUsers(searchRequest, pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Get users by company", description = "Retrieve all users for a specific company")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    @PreAuthorize("hasRole('SUPER_ADMIN') or (hasRole('COMPANY_ADMIN') and @userService.belongsToCompany(authentication.name, #companyId))")
    public ResponseEntity<Page<UserResponse>> getUsersByCompany(
            @PathVariable UUID companyId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        logger.info("Get users by company request for company ID: {}", companyId);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserResponse> response = userService.getUsersByCompany(companyId, pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "Get users by role", description = "Retrieve all users with specific role")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Page<UserResponse>> getUsersByRole(
            @PathVariable UserRole role,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        logger.info("Get users by role request for role: {}", role);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserResponse> response = userService.getUsersByRole(role, pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get user statistics", description = "Get user statistics for company or system")
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserStatistics(@RequestParam(required = false) UUID companyId) {
        logger.info("Get user statistics request");

        Map<String, Object> statistics = userService.getUserStatistics(companyId);

        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get current authenticated user information")
    @ApiResponse(responseCode = "200", description = "Current user information")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        UserResponse response = userService.getUserById(userId);

        return ResponseEntity.ok(response);
    }
}