package com.fleetmanagement.userservice.controller;

import com.fleetmanagement.userservice.dto.request.DriverAssignmentNotification;
import com.fleetmanagement.userservice.dto.response.ApiResponse;
import com.fleetmanagement.userservice.dto.response.DriverResponse;
import com.fleetmanagement.userservice.dto.response.DriverValidationResponse;
import com.fleetmanagement.userservice.dto.response.UserResponse;
import com.fleetmanagement.userservice.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/drivers")
@Tag(name = "Driver Management", description = "Driver-specific operations")
@Validated
public class DriverController {

    private static final Logger logger = LoggerFactory.getLogger(DriverController.class);

    private final DriverService driverService;

    @Autowired
    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Get company drivers", description = "Get all drivers for a company")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<Page<DriverResponse>> getCompanyDrivers(
            @PathVariable UUID companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<DriverResponse> drivers = driverService.getCompanyDrivers(companyId, pageable);

        return ResponseEntity.ok(drivers);
    }

    @GetMapping("/company/{companyId}/available")
    @Operation(summary = "Get available drivers", description = "Get all available drivers for a company")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<List<DriverResponse>> getAvailableDrivers(@PathVariable UUID companyId) {
        List<DriverResponse> availableDrivers = driverService.getAvailableDrivers(companyId);
        return ResponseEntity.ok(availableDrivers);
    }

    @GetMapping("/{driverId}")
    @Operation(summary = "Get driver by ID", description = "Get driver details by ID")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<DriverResponse> getDriverById(@PathVariable UUID driverId) {
        DriverResponse driver = driverService.getDriverById(driverId);
        return ResponseEntity.ok(driver);
    }

    @PostMapping("/{userId}/promote")
    @Operation(summary = "Promote user to driver", description = "Promote an existing user to driver role")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<DriverResponse> promoteToDriver(@PathVariable UUID userId) {
        DriverResponse driver = driverService.promoteToDriver(userId);
        return ResponseEntity.ok(driver);
    }

    @PostMapping("/{userId}/demote")
    @Operation(summary = "Demote driver", description = "Demote a driver to regular user")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<UserResponse> demoteFromDriver(@PathVariable UUID userId) {
        UserResponse user = driverService.demoteFromDriver(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/available")
    @Operation(summary = "Get available drivers", description = "Retrieve drivers available for vehicle assignment")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Available drivers retrieved successfully")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<List<DriverResponse>>> getAvailableDriversForAssignment(
            @RequestParam @Parameter(description = "Company ID") UUID companyId,
            Authentication authentication) {

        logger.debug("Get available drivers request for company: {}", companyId);

        UUID requestingUserCompanyId = getCompanyIdFromAuth(authentication);
        if (!hasAccessToCompany(requestingUserCompanyId, companyId, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<DriverResponse> availableDrivers = driverService.getAvailableDrivers(companyId);

        ApiResponse<List<DriverResponse>> response = ApiResponse.success(
                availableDrivers,
                "Available drivers retrieved successfully"
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/validate")
    @Operation(summary = "Validate driver", description = "Check if user is a valid driver and available for assignment")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Driver validation completed")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<DriverValidationResponse>> validateDriver(
            @PathVariable @Parameter(description = "User ID") UUID userId,
            @RequestParam @Parameter(description = "Company ID") UUID companyId,
            Authentication authentication) {

        logger.debug("Validate driver request for user: {} in company: {}", userId, companyId);

        UUID requestingUserCompanyId = getCompanyIdFromAuth(authentication);
        if (!hasAccessToCompany(requestingUserCompanyId, companyId, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        DriverValidationResponse validation = driverService.validateDriver(userId, companyId);

        ApiResponse<DriverValidationResponse> response = ApiResponse.success(
                validation,
                "Driver validation completed"
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/assign-vehicle")
    @Operation(summary = "Notify driver assignment", description = "Notify user service that driver has been assigned to a vehicle")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Driver assignment notification processed")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Driver not found")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> notifyDriverAssignment(
            @PathVariable @Parameter(description = "Driver User ID") UUID userId,
            @Valid @RequestBody DriverAssignmentNotification notification,
            Authentication authentication) {

        logger.info("Driver assignment notification for user: {} to vehicle: {}",
                userId, notification.getVehicleId());

        UUID requestingUserCompanyId = getCompanyIdFromAuth(authentication);
        if (!hasAccessToCompany(requestingUserCompanyId, notification.getCompanyId(), authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        driverService.notifyDriverAssignment(userId, notification);

        ApiResponse<Void> response = ApiResponse.success(
                null,
                "Driver assignment notification processed successfully"
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/unassign-vehicle")
    @Operation(summary = "Notify driver unassignment", description = "Notify user service that driver has been unassigned from vehicle")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Driver unassignment notification processed")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Driver not found")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> notifyDriverUnassignment(
            @PathVariable @Parameter(description = "Driver User ID") UUID userId,
            @RequestParam @Parameter(description = "Company ID") UUID companyId,
            Authentication authentication) {

        logger.info("Driver unassignment notification for user: {} in company: {}", userId, companyId);

        UUID requestingUserCompanyId = getCompanyIdFromAuth(authentication);
        if (!hasAccessToCompany(requestingUserCompanyId, companyId, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        driverService.notifyDriverUnassignment(userId, companyId);

        ApiResponse<Void> response = ApiResponse.success(
                null,
                "Driver unassignment notification processed successfully"
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get driver statistics", description = "Retrieve driver statistics for the company")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Driver statistics retrieved successfully")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<DriverStatisticsResponse>> getDriverStatistics(
            @RequestParam @Parameter(description = "Company ID") UUID companyId,
            Authentication authentication) {

        logger.debug("Get driver statistics for company: {}", companyId);

        UUID requestingUserCompanyId = getCompanyIdFromAuth(authentication);
        if (!hasAccessToCompany(requestingUserCompanyId, companyId, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        DriverStatisticsResponse statistics = driverService.getDriverStatistics(companyId);

        ApiResponse<DriverStatisticsResponse> response = ApiResponse.success(
                statistics,
                "Driver statistics retrieved successfully"
        );

        return ResponseEntity.ok(response);
    }

    private UUID getCompanyIdFromAuth(Authentication authentication) {
        return UUID.fromString(authentication.getDetails().toString());
    }

    private boolean hasAccessToCompany(UUID userCompanyId, UUID targetCompanyId, Authentication authentication) {
        if (authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
            return true;
        }

        return userCompanyId.equals(targetCompanyId);
    }

    public static class DriverStatisticsResponse {
        private int totalDrivers;
        private int availableDrivers;
        private int assignedDrivers;
        private int activeDrivers;
        private int inactiveDrivers;

        public DriverStatisticsResponse() {
        }

        public DriverStatisticsResponse(int totalDrivers, int availableDrivers, int assignedDrivers,
                                        int activeDrivers, int inactiveDrivers) {
            this.totalDrivers = totalDrivers;
            this.availableDrivers = availableDrivers;
            this.assignedDrivers = assignedDrivers;
            this.activeDrivers = activeDrivers;
            this.inactiveDrivers = inactiveDrivers;
        }

        public int getTotalDrivers() {
            return totalDrivers;
        }

        public void setTotalDrivers(int totalDrivers) {
            this.totalDrivers = totalDrivers;
        }

        public int getAvailableDrivers() {
            return availableDrivers;
        }

        public void setAvailableDrivers(int availableDrivers) {
            this.availableDrivers = availableDrivers;
        }

        public int getAssignedDrivers() {
            return assignedDrivers;
        }

        public void setAssignedDrivers(int assignedDrivers) {
            this.assignedDrivers = assignedDrivers;
        }

        public int getActiveDrivers() {
            return activeDrivers;
        }

        public void setActiveDrivers(int activeDrivers) {
            this.activeDrivers = activeDrivers;
        }

        public int getInactiveDrivers() {
            return inactiveDrivers;
        }

        public void setInactiveDrivers(int inactiveDrivers) {
            this.inactiveDrivers = inactiveDrivers;
        }
    }
}