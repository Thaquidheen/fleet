package com.fleetmanagement.vehicleservice.controller;

import com.fleetmanagement.vehicleservice.client.UserServiceClient;
import com.fleetmanagement.vehicleservice.client.UserServiceClient.DriverResponse;
import com.fleetmanagement.vehicleservice.dto.request.CreateVehicleRequest;
import com.fleetmanagement.vehicleservice.dto.request.UpdateVehicleRequest;
import com.fleetmanagement.vehicleservice.dto.response.ApiResponse;
import com.fleetmanagement.vehicleservice.dto.response.VehicleResponse;
import com.fleetmanagement.vehicleservice.dto.response.VehicleStatisticsResponse;
import com.fleetmanagement.vehicleservice.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vehicles")
@Tag(name = "Vehicle Management", description = "Vehicle operations and fleet management")
@Validated
public class VehicleController {

    private static final Logger logger = LoggerFactory.getLogger(VehicleController.class);

    private final VehicleService vehicleService;
    private final UserServiceClient userServiceClient;

    @Autowired
    public VehicleController(VehicleService vehicleService, UserServiceClient userServiceClient) {
        this.vehicleService = vehicleService;
        this.userServiceClient = userServiceClient;
    }

    /**
     * Create a new vehicle with company limit validation
     */
    @PostMapping
    @Operation(summary = "Create vehicle", description = "Register a new vehicle with automatic company limit validation")
    @SwaggerApiResponse(responseCode = "201", description = "Vehicle created successfully")
    @SwaggerApiResponse(responseCode = "400", description = "Invalid vehicle data or company limit exceeded")
    @SwaggerApiResponse(responseCode = "409", description = "Vehicle already exists (VIN or license plate conflict)")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<VehicleResponse>> createVehicle(
            @Valid @RequestBody CreateVehicleRequest request,
            Authentication authentication) {

        logger.info("Create vehicle request for: {} by user: {}", request.getName(), authentication.getName());

        UUID companyId = getCompanyIdFromAuth(authentication);
        UUID createdBy = getUserIdFromAuth(authentication);

        VehicleResponse response = vehicleService.createVehicle(request, companyId, createdBy);

        ApiResponse<VehicleResponse> apiResponse = ApiResponse.<VehicleResponse>builder()
                .success(true)
                .data(response)
                .message("Vehicle created successfully")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    /**
     * Get all vehicles for company with pagination
     */
    @GetMapping
    @Operation(summary = "Get vehicles", description = "Retrieve all vehicles for the company with pagination")
    @SwaggerApiResponse(responseCode = "200", description = "Vehicles retrieved successfully")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER') or hasRole('DRIVER') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<Page<VehicleResponse>>> getVehicles(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir,
            Authentication authentication) {

        logger.debug("Get vehicles request - page: {}, size: {}", page, size);

        UUID companyId = getCompanyIdFromAuth(authentication);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<VehicleResponse> vehicles = vehicleService.getVehiclesByCompany(companyId, pageable);

        ApiResponse<Page<VehicleResponse>> response = ApiResponse.<Page<VehicleResponse>>builder()
                .success(true)
                .data(vehicles)
                .message("Vehicles retrieved successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Get vehicle by ID
     */
    @GetMapping("/{vehicleId}")
    @Operation(summary = "Get vehicle by ID", description = "Retrieve vehicle information by ID")
    @SwaggerApiResponse(responseCode = "200", description = "Vehicle found")
    @SwaggerApiResponse(responseCode = "404", description = "Vehicle not found")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER') or hasRole('DRIVER') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicleById(
            @PathVariable @Parameter(description = "Vehicle ID") UUID vehicleId,
            Authentication authentication) {

        logger.debug("Get vehicle request for ID: {}", vehicleId);

        UUID companyId = getCompanyIdFromAuth(authentication);

        VehicleResponse response = vehicleService.getVehicleById(vehicleId, companyId);

        ApiResponse<VehicleResponse> apiResponse = ApiResponse.<VehicleResponse>builder()
                .success(true)
                .data(response)
                .message("Vehicle retrieved successfully")
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Update vehicle
     */
    @PutMapping("/{vehicleId}")
    @Operation(summary = "Update vehicle", description = "Update vehicle information")
    @SwaggerApiResponse(responseCode = "200", description = "Vehicle updated successfully")
    @SwaggerApiResponse(responseCode = "404", description = "Vehicle not found")
    @SwaggerApiResponse(responseCode = "400", description = "Invalid vehicle data")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicle(
            @PathVariable @Parameter(description = "Vehicle ID") UUID vehicleId,
            @Valid @RequestBody UpdateVehicleRequest request,
            Authentication authentication) {

        logger.info("Update vehicle request for ID: {}", vehicleId);

        UUID companyId = getCompanyIdFromAuth(authentication);
        UUID updatedBy = getUserIdFromAuth(authentication);

        VehicleResponse response = vehicleService.updateVehicle(vehicleId, request, companyId, updatedBy);

        ApiResponse<VehicleResponse> apiResponse = ApiResponse.<VehicleResponse>builder()
                .success(true)
                .data(response)
                .message("Vehicle updated successfully")
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Delete vehicle
     */
    @DeleteMapping("/{vehicleId}")
    @Operation(summary = "Delete vehicle", description = "Delete/retire vehicle from fleet")
    @SwaggerApiResponse(responseCode = "204", description = "Vehicle deleted successfully")
    @SwaggerApiResponse(responseCode = "404", description = "Vehicle not found")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteVehicle(
            @PathVariable @Parameter(description = "Vehicle ID") UUID vehicleId,
            Authentication authentication) {

        logger.info("Delete vehicle request for ID: {}", vehicleId);

        UUID companyId = getCompanyIdFromAuth(authentication);
        UUID deletedBy = getUserIdFromAuth(authentication);

        vehicleService.deleteVehicle(vehicleId, companyId, deletedBy);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Vehicle deleted successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Get available drivers for vehicle assignment
     */
    @GetMapping("/{vehicleId}/available-drivers")
    @Operation(summary = "Get available drivers", description = "Get list of available drivers for vehicle assignment")
    @SwaggerApiResponse(responseCode = "200", description = "Available drivers retrieved successfully")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<List<DriverResponse>>> getAvailableDrivers(
            @PathVariable @Parameter(description = "Vehicle ID") UUID vehicleId,
            Authentication authentication) {

        logger.debug("Get available drivers for vehicle: {}", vehicleId);

        UUID companyId = getCompanyIdFromAuth(authentication);

        // Get available drivers from User Service
        List<DriverResponse> drivers = userServiceClient.getAvailableDrivers(companyId).getBody();

        ApiResponse<List<DriverResponse>> response = ApiResponse.<List<DriverResponse>>builder()
                .success(true)
                .data(drivers)
                .message("Available drivers retrieved successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Get vehicle statistics for company
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get vehicle statistics", description = "Get comprehensive vehicle statistics for the company")
    @SwaggerApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<VehicleStatisticsResponse>> getVehicleStatistics(
            Authentication authentication) {

        logger.debug("Get vehicle statistics request");

        UUID companyId = getCompanyIdFromAuth(authentication);

        VehicleStatisticsResponse statistics = vehicleService.getVehicleStatistics(companyId);

        ApiResponse<VehicleStatisticsResponse> response = ApiResponse.<VehicleStatisticsResponse>builder()
                .success(true)
                .data(statistics)
                .message("Vehicle statistics retrieved successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Search vehicles with filters
     */
    @GetMapping("/search")
    @Operation(summary = "Search vehicles", description = "Search vehicles with various filters")
    @SwaggerApiResponse(responseCode = "200", description = "Search results retrieved successfully")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER') or hasRole('DRIVER') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<Page<VehicleResponse>>> searchVehicles(
            @Parameter(description = "Vehicle name or license plate") @RequestParam(required = false) String query,
            @Parameter(description = "Vehicle type") @RequestParam(required = false) String vehicleType,
            @Parameter(description = "Vehicle status") @RequestParam(required = false) String status,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        logger.debug("Search vehicles request - query: {}, type: {}, status: {}", query, vehicleType, status);

        UUID companyId = getCompanyIdFromAuth(authentication);

        Pageable pageable = PageRequest.of(page, size);
        Page<VehicleResponse> results = vehicleService.searchVehicles(companyId, query, vehicleType, status, pageable);

        ApiResponse<Page<VehicleResponse>> response = ApiResponse.<Page<VehicleResponse>>builder()
                .success(true)
                .data(results)
                .message("Search results retrieved successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Get company drivers for vehicle assignment
     */
    @GetMapping("/drivers")
    @Operation(summary = "Get company drivers", description = "Get all drivers in the company for vehicle assignment")
    @SwaggerApiResponse(responseCode = "200", description = "Company drivers retrieved successfully")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<List<DriverResponse>>> getCompanyDrivers(
            Authentication authentication) {

        logger.debug("Get company drivers request");

        UUID companyId = getCompanyIdFromAuth(authentication);

        List<DriverResponse> drivers = userServiceClient.getCompanyDrivers(companyId).getBody();

        ApiResponse<List<DriverResponse>> response = ApiResponse.<List<DriverResponse>>builder()
                .success(true)
                .data(drivers)
                .message("Company drivers retrieved successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Validate vehicle creation limits
     */
    @GetMapping("/validate-creation")
    @Operation(summary = "Validate vehicle creation", description = "Check if company can add more vehicles")
    @SwaggerApiResponse(responseCode = "200", description = "Validation completed successfully")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<VehicleCreationValidationResponse>> validateVehicleCreation(
            Authentication authentication) {

        logger.debug("Validate vehicle creation request");

        UUID companyId = getCompanyIdFromAuth(authentication);

        VehicleCreationValidationResponse validation = vehicleService.validateVehicleCreation(companyId);

        ApiResponse<VehicleCreationValidationResponse> response = ApiResponse.<VehicleCreationValidationResponse>builder()
                .success(true)
                .data(validation)
                .message("Validation completed successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    // Helper methods
    private UUID getUserIdFromAuth(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    private UUID getCompanyIdFromAuth(Authentication authentication) {
        // Extract company ID from JWT token details
        return UUID.fromString(authentication.getDetails().toString());
    }

    // Response DTOs
    public static class VehicleCreationValidationResponse {
        private boolean canCreateVehicle;
        private String reason;
        private int remainingSlots;
        private int currentVehicles;
        private int maxVehicles;
        private String subscriptionPlan;

        // Constructors, getters, and setters
        public VehicleCreationValidationResponse() {}

        public boolean isCanCreateVehicle() { return canCreateVehicle; }
        public void setCanCreateVehicle(boolean canCreateVehicle) { this.canCreateVehicle = canCreateVehicle; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public int getRemainingSlots() { return remainingSlots; }
        public void setRemainingSlots(int remainingSlots) { this.remainingSlots = remainingSlots; }
        public int getCurrentVehicles() { return currentVehicles; }
        public void setCurrentVehicles(int currentVehicles) { this.currentVehicles = currentVehicles; }
        public int getMaxVehicles() { return maxVehicles; }
        public void setMaxVehicles(int maxVehicles) { this.maxVehicles = maxVehicles; }
        public String getSubscriptionPlan() { return subscriptionPlan; }
        public void setSubscriptionPlan(String subscriptionPlan) { this.subscriptionPlan = subscriptionPlan; }
    }
}