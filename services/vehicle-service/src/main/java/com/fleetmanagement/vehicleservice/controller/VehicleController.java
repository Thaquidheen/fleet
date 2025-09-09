package com.fleetmanagement.vehicleservice.controller;

import com.fleetmanagement.vehicleservice.dto.request.*;
import com.fleetmanagement.vehicleservice.dto.response.*;
import com.fleetmanagement.vehicleservice.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

/**
 * Vehicle Controller
 *
 * REST API endpoints for vehicle management operations including:
 * - Vehicle CRUD operations
 * - Fleet analytics and reporting
 * - Vehicle search and filtering
 * - Bulk operations
 * - Vehicle validation
 */
@RestController
@RequestMapping("/api/vehicles")
@Tag(name = "Vehicle Management", description = "Vehicle operations and fleet management")
@Validated
public class VehicleController {

    private static final Logger logger = LoggerFactory.getLogger(VehicleController.class);

    private final VehicleService vehicleService;

    @Autowired
    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    /**
     * Create a new vehicle
     */
    @PostMapping
    @Operation(summary = "Create vehicle", description = "Register a new vehicle in the fleet")
    @ApiResponse(responseCode = "201", description = "Vehicle created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid vehicle data")
    @ApiResponse(responseCode = "409", description = "Vehicle already exists (VIN or license plate conflict)")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<VehicleResponse>> createVehicle(
            @Valid @RequestBody CreateVehicleRequest request,
            Authentication authentication) {

        logger.info("Create vehicle request for: {}", request.getName());

        UUID companyId = getCompanyIdFromAuth(authentication);
        UUID createdBy = getUserIdFromAuth(authentication);

        VehicleResponse vehicle = vehicleService.createVehicle(request, companyId, createdBy);

        ApiResponse<VehicleResponse> response = ApiResponse.success(vehicle, "Vehicle created successfully");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get vehicle by ID
     */
    @GetMapping("/{vehicleId}")
    @Operation(summary = "Get vehicle", description = "Retrieve vehicle details by ID")
    @ApiResponse(responseCode = "200", description = "Vehicle retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Vehicle not found")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER') or hasRole('DRIVER') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicle(
            @PathVariable @Parameter(description = "Vehicle ID") UUID vehicleId,
            Authentication authentication) {

        logger.debug("Get vehicle request for ID: {}", vehicleId);

        UUID companyId = getCompanyIdFromAuth(authentication);

        VehicleResponse vehicle = vehicleService.getVehicleById(vehicleId, companyId);

        ApiResponse<VehicleResponse> response = ApiResponse.success(vehicle);

        return ResponseEntity.ok(response);
    }

    /**
     * Update vehicle
     */
    @PutMapping("/{vehicleId}")
    @Operation(summary = "Update vehicle", description = "Update vehicle information")
    @ApiResponse(responseCode = "200", description = "Vehicle updated successfully")
    @ApiResponse(responseCode = "404", description = "Vehicle not found")
    @ApiResponse(responseCode = "400", description = "Invalid update data")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicle(
            @PathVariable @Parameter(description = "Vehicle ID") UUID vehicleId,
            @Valid @RequestBody UpdateVehicleRequest request,
            Authentication authentication) {

        logger.info("Update vehicle request for ID: {}", vehicleId);

        UUID companyId = getCompanyIdFromAuth(authentication);
        UUID updatedBy = getUserIdFromAuth(authentication);

        VehicleResponse vehicle = vehicleService.updateVehicle(vehicleId, request, companyId, updatedBy);

        ApiResponse<VehicleResponse> response = ApiResponse.success(vehicle, "Vehicle updated successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Delete vehicle
     */
    @DeleteMapping("/{vehicleId}")
    @Operation(summary = "Delete vehicle", description = "Remove vehicle from the fleet")
    @ApiResponse(responseCode = "204", description = "Vehicle deleted successfully")
    @ApiResponse(responseCode = "404", description = "Vehicle not found")
    @ApiResponse(responseCode = "409", description = "Cannot delete assigned vehicle")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteVehicle(
            @PathVariable @Parameter(description = "Vehicle ID") UUID vehicleId,
            Authentication authentication) {

        logger.info("Delete vehicle request for ID: {}", vehicleId);

        UUID companyId = getCompanyIdFromAuth(authentication);
        UUID deletedBy = getUserIdFromAuth(authentication);

        vehicleService.deleteVehicle(vehicleId, companyId, deletedBy);

        ApiResponse<Void> response = ApiResponse.success(null, "Vehicle deleted successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Get all vehicles for company with pagination
     */
    @GetMapping
    @Operation(summary = "Get vehicles", description = "Retrieve all vehicles for the company with pagination")
    @ApiResponse(responseCode = "200", description = "Vehicles retrieved successfully")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER') or hasRole('DRIVER') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<PagedResponse<VehicleSummaryResponse>>> getVehicles(
            @RequestParam(defaultValue = "0") @Min(0) @Parameter(description = "Page number") int page,
            @RequestParam(defaultValue = "20") @Min(1) @Parameter(description = "Page size") int size,
            @RequestParam(defaultValue = "name") @Parameter(description = "Sort field") String sortBy,
            @RequestParam(defaultValue = "asc") @Parameter(description = "Sort direction") String sortDir,
            Authentication authentication) {

        logger.debug("Get vehicles request - page: {}, size: {}", page, size);

        UUID companyId = getCompanyIdFromAuth(authentication);

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        PagedResponse<VehicleSummaryResponse> vehicles = vehicleService.getVehiclesByCompany(companyId, pageable);

        ApiResponse<PagedResponse<VehicleSummaryResponse>> response = ApiResponse.success(vehicles);

        return ResponseEntity.ok(response);
    }

    /**
     * Search vehicles with advanced criteria
     */
    @PostMapping("/search")
    @Operation(summary = "Search vehicles", description = "Search vehicles using advanced criteria")
    @ApiResponse(responseCode = "200", description = "Search completed successfully")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER') or hasRole('DRIVER') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<PagedResponse<VehicleSummaryResponse>>> searchVehicles(
            @Valid @RequestBody VehicleSearchRequest searchRequest,
            @RequestParam(defaultValue = "0") @Min(0) @Parameter(description = "Page number") int page,
            @RequestParam(defaultValue = "20") @Min(1) @Parameter(description = "Page size") int size,
            @RequestParam(defaultValue = "name") @Parameter(description = "Sort field") String sortBy,
            @RequestParam(defaultValue = "asc") @Parameter(description = "Sort direction") String sortDir,
            Authentication authentication) {

        logger.debug("Search vehicles request with criteria");

        UUID companyId = getCompanyIdFromAuth(authentication);

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        PagedResponse<VehicleSummaryResponse> vehicles = vehicleService.searchVehicles(searchRequest, companyId, pageable);

        ApiResponse<PagedResponse<VehicleSummaryResponse>> response = ApiResponse.success(vehicles);

        return ResponseEntity.ok(response);
    }

    /**
     * Get available vehicles (not assigned)
     */
    @GetMapping("/available")
    @Operation(summary = "Get available vehicles", description = "Retrieve vehicles available for assignment")
    @ApiResponse(responseCode = "200", description = "Available vehicles retrieved successfully")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<List<VehicleSummaryResponse>>> getAvailableVehicles(
            Authentication authentication) {

        logger.debug("Get available vehicles request");

        UUID companyId = getCompanyIdFromAuth(authentication);

        List<VehicleSummaryResponse> vehicles = vehicleService.getAvailableVehicles(companyId);

        ApiResponse<List<VehicleSummaryResponse>> response = ApiResponse.success(vehicles);

        return ResponseEntity.ok(response);
    }

    /**
     * Get vehicles due for maintenance
     */
    @GetMapping("/maintenance-due")
    @Operation(summary = "Get vehicles due for maintenance", description = "Retrieve vehicles that require maintenance")
    @ApiResponse(responseCode = "200", description = "Maintenance due vehicles retrieved successfully")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<List<VehicleSummaryResponse>>> getVehiclesDueForMaintenance(
            Authentication authentication) {

        logger.debug("Get vehicles due for maintenance request");

        UUID companyId = getCompanyIdFromAuth(authentication);

        List<VehicleSummaryResponse> vehicles = vehicleService.getVehiclesDueForMaintenance(companyId);

        ApiResponse<List<VehicleSummaryResponse>> response = ApiResponse.success(vehicles);

        return ResponseEntity.ok(response);
    }

    /**
     * Get vehicles with expiring insurance
     */
    @GetMapping("/insurance-expiring")
    @Operation(summary = "Get vehicles with expiring insurance", description = "Retrieve vehicles with insurance expiring soon")
    @ApiResponse(responseCode = "200", description = "Insurance expiring vehicles retrieved successfully")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<List<VehicleSummaryResponse>>> getVehiclesWithExpiringInsurance(
            @RequestParam(defaultValue = "30") @Min(1) @Parameter(description = "Days threshold") int daysThreshold,
            Authentication authentication) {

        logger.debug("Get vehicles with expiring insurance request - threshold: {} days", daysThreshold);

        UUID companyId = getCompanyIdFromAuth(authentication);

        List<VehicleSummaryResponse> vehicles = vehicleService.getVehiclesWithExpiringInsurance(companyId, daysThreshold);

        ApiResponse<List<VehicleSummaryResponse>> response = ApiResponse.success(vehicles);

        return ResponseEntity.ok(response);
    }

    /**
     * Get vehicles with expiring registration
     */
    @GetMapping("/registration-expiring")
    @Operation(summary = "Get vehicles with expiring registration", description = "Retrieve vehicles with registration expiring soon")
    @ApiResponse(responseCode = "200", description = "Registration expiring vehicles retrieved successfully")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<List<VehicleSummaryResponse>>> getVehiclesWithExpiringRegistration(
            @RequestParam(defaultValue = "30") @Min(1) @Parameter(description = "Days threshold") int daysThreshold,
            Authentication authentication) {

        logger.debug("Get vehicles with expiring registration request - threshold: {} days", daysThreshold);

        UUID companyId = getCompanyIdFromAuth(authentication);

        List<VehicleSummaryResponse> vehicles = vehicleService.getVehiclesWithExpiringRegistration(companyId, daysThreshold);

        ApiResponse<List<VehicleSummaryResponse>> response = ApiResponse.success(vehicles);

        return ResponseEntity.ok(response);
    }

    /**
     * Update vehicle mileage
     */
    @PutMapping("/{vehicleId}/mileage")
    @Operation(summary = "Update vehicle mileage", description = "Update the current mileage of a vehicle")
    @ApiResponse(responseCode = "200", description = "Mileage updated successfully")
    @ApiResponse(responseCode = "404", description = "Vehicle not found")
    @ApiResponse(responseCode = "400", description = "Invalid mileage value")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER') or hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicleMileage(
            @PathVariable @Parameter(description = "Vehicle ID") UUID vehicleId,
            @Valid @RequestBody VehicleMileageUpdateRequest request,
            Authentication authentication) {

        logger.info("Update mileage request for vehicle: {} to {}", vehicleId, request.getNewMileage());

        UUID companyId = getCompanyIdFromAuth(authentication);
        UUID updatedBy = getUserIdFromAuth(authentication);

        // Set vehicle ID in request
        request.setVehicleId(vehicleId);

        VehicleResponse vehicle = vehicleService.updateVehicleMileage(request, companyId, updatedBy);

        ApiResponse<VehicleResponse> response = ApiResponse.success(vehicle, "Vehicle mileage updated successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Update vehicle location
     */
    @PutMapping("/{vehicleId}/location")
    @Operation(summary = "Update vehicle location", description = "Update the current location of a vehicle")
    @ApiResponse(responseCode = "200", description = "Location updated successfully")
    @ApiResponse(responseCode = "404", description = "Vehicle not found")
    @ApiResponse(responseCode = "400", description = "Invalid location coordinates")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER') or hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicleLocation(
            @PathVariable @Parameter(description = "Vehicle ID") UUID vehicleId,
            @Valid @RequestBody VehicleLocationUpdateRequest request,
            Authentication authentication) {

        logger.debug("Update location request for vehicle: {}", vehicleId);

        UUID companyId = getCompanyIdFromAuth(authentication);
        UUID updatedBy = getUserIdFromAuth(authentication);

        // Set vehicle ID in request
        request.setVehicleId(vehicleId);

        VehicleResponse vehicle = vehicleService.updateVehicleLocation(request, companyId, updatedBy);

        ApiResponse<VehicleResponse> response = ApiResponse.success(vehicle, "Vehicle location updated successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Validate vehicle
     */
    @PostMapping("/{vehicleId}/validate")
    @Operation(summary = "Validate vehicle", description = "Perform comprehensive vehicle validation")
    @ApiResponse(responseCode = "200", description = "Validation completed")
    @ApiResponse(responseCode = "404", description = "Vehicle not found")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<VehicleValidationResponse>> validateVehicle(
            @PathVariable @Parameter(description = "Vehicle ID") UUID vehicleId,
            Authentication authentication) {

        logger.debug("Validate vehicle request for ID: {}", vehicleId);

        UUID companyId = getCompanyIdFromAuth(authentication);

        VehicleValidationResponse validation = vehicleService.validateVehicle(vehicleId, companyId);

        ApiResponse<VehicleValidationResponse> response = ApiResponse.success(validation);

        return ResponseEntity.ok(response);
    }

    /**
     * Get fleet analytics
     */
    @PostMapping("/analytics")
    @Operation(summary = "Get fleet analytics", description = "Generate comprehensive fleet analytics and reports")
    @ApiResponse(responseCode = "200", description = "Analytics generated successfully")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<FleetAnalyticsResponse>> getFleetAnalytics(
            @Valid @RequestBody FleetAnalyticsRequest request,
            Authentication authentication) {

        logger.info("Fleet analytics request");

        UUID companyId = getCompanyIdFromAuth(authentication);

        FleetAnalyticsResponse analytics = vehicleService.getFleetAnalytics(request, companyId);

        ApiResponse<FleetAnalyticsResponse> response = ApiResponse.success(analytics);

        return ResponseEntity.ok(response);
    }

    /**
     * Perform bulk operations on vehicles
     */
    @PostMapping("/bulk-operations")
    @Operation(summary = "Bulk vehicle operations", description = "Perform bulk operations on multiple vehicles")
    @ApiResponse(responseCode = "200", description = "Bulk operation completed")
    @ApiResponse(responseCode = "400", description = "Invalid bulk operation request")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<BulkOperationResponse>> performBulkOperation(
            @Valid @RequestBody BulkVehicleOperationRequest request,
            Authentication authentication) {

        logger.info("Bulk operation request: {} on {} vehicles",
                request.getOperationType(), request.getVehicleIds().size());

        UUID companyId = getCompanyIdFromAuth(authentication);
        UUID performedBy = getUserIdFromAuth(authentication);

        BulkOperationResponse result = vehicleService.performBulkOperation(request, companyId, performedBy);

        ApiResponse<BulkOperationResponse> response = ApiResponse.success(result, "Bulk operation completed");

        return ResponseEntity.ok(response);
    }

    // Helper methods to extract information from authentication

    private UUID getCompanyIdFromAuth(Authentication authentication) {
        // Extract company ID from authentication context
        // This would be implemented based on your JWT token structure
        return UUID.fromString("123e4567-e89b-12d3-a456-426614174000"); // Placeholder
    }

    private UUID getUserIdFromAuth(Authentication authentication) {
        // Extract user ID from authentication context
        // This would be implemented based on your JWT token structure
        return UUID.fromString("123e4567-e89b-12d3-a456-426614174001"); // Placeholder
    }
}