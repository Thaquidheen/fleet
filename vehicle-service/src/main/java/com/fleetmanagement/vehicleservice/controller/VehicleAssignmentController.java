package com.fleetmanagement.vehicleservice.controller;

import com.fleetmanagement.vehicleservice.dto.request.*;
import com.fleetmanagement.vehicleservice.dto.response.*;
import com.fleetmanagement.vehicleservice.service.VehicleAssignmentService;
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
 * Vehicle Assignment Controller
 *
 * REST API endpoints for vehicle-driver assignment management including:
 * - Assignment CRUD operations
 * - Check-in/Check-out functionality
 * - Assignment validation and conflict detection
 * - Assignment history and analytics
 */
@RestController
@RequestMapping("/api/vehicle-assignments")
@Tag(name = "Vehicle Assignment Management", description = "Driver-vehicle assignment operations")
@Validated
public class VehicleAssignmentController {

    private static final Logger logger = LoggerFactory.getLogger(VehicleAssignmentController.class);

    private final VehicleAssignmentService assignmentService;

    @Autowired
    public VehicleAssignmentController(VehicleAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    /**
     * Assign driver to vehicle
     */
    @PostMapping
    @Operation(summary = "Assign driver to vehicle", description = "Create a new driver-vehicle assignment")
    @ApiResponse(responseCode = "201", description = "Assignment created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid assignment data")
    @ApiResponse(responseCode = "409", description = "Assignment conflict detected")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<VehicleAssignmentResponse>> assignDriverToVehicle(
            @Valid @RequestBody AssignDriverRequest request,
            Authentication authentication) {

        logger.info("Assign driver {} to vehicle {} request", request.getDriverId(), request.getVehicleId());

        UUID companyId = getCompanyIdFromAuth(authentication);
        UUID assignedBy = getUserIdFromAuth(authentication);

        VehicleAssignmentResponse assignment = assignmentService.assignDriverToVehicle(request, companyId, assignedBy);

        ApiResponse<VehicleAssignmentResponse> response = ApiResponse.success(assignment, "Driver assigned to vehicle successfully");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get assignment by ID
     */
    @GetMapping("/{assignmentId}")
    @Operation(summary = "Get assignment", description = "Retrieve assignment details by ID")
    @ApiResponse(responseCode = "200", description = "Assignment retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Assignment not found")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER') or hasRole('DRIVER') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<VehicleAssignmentResponse>> getAssignment(
            @PathVariable @Parameter(description = "Assignment ID") UUID assignmentId,
            Authentication authentication) {

        logger.debug("Get assignment request for ID: {}", assignmentId);

        UUID companyId = getCompanyIdFromAuth(authentication);

        VehicleAssignmentResponse assignment = assignmentService.getAssignmentById(assignmentId, companyId);

        ApiResponse<VehicleAssignmentResponse> response = ApiResponse.success(assignment);

        return ResponseEntity.ok(response);
    }

    /**
     * Update assignment
     */
    @PutMapping("/{assignmentId}")
    @Operation(summary = "Update assignment", description = "Update assignment information")
    @ApiResponse(responseCode = "200", description = "Assignment updated successfully")
    @ApiResponse(responseCode = "404", description = "Assignment not found")
    @ApiResponse(responseCode = "400", description = "Invalid update data")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<VehicleAssignmentResponse>> updateAssignment(
            @PathVariable @Parameter(description = "Assignment ID") UUID assignmentId,
            @Valid @RequestBody UpdateAssignmentRequest request,
            Authentication authentication) {

        logger.info("Update assignment request for ID: {}", assignmentId);

        UUID companyId = getCompanyIdFromAuth(authentication);
        UUID updatedBy = getUserIdFromAuth(authentication);

        VehicleAssignmentResponse assignment = assignmentService.updateAssignment(assignmentId, request, companyId, updatedBy);

        ApiResponse<VehicleAssignmentResponse> response = ApiResponse.success(assignment, "Assignment updated successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Terminate assignment
     */
    @PostMapping("/{assignmentId}/terminate")
    @Operation(summary = "Terminate assignment", description = "Terminate an active assignment")
    @ApiResponse(responseCode = "200", description = "Assignment terminated successfully")
    @ApiResponse(responseCode = "404", description = "Assignment not found")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<VehicleAssignmentResponse>> terminateAssignment(
            @PathVariable @Parameter(description = "Assignment ID") UUID assignmentId,
            Authentication authentication) {

        logger.info("Terminate assignment request for ID: {}", assignmentId);

        UUID companyId = getCompanyIdFromAuth(authentication);
        UUID terminatedBy = getUserIdFromAuth(authentication);

        VehicleAssignmentResponse assignment = assignmentService.terminateAssignment(assignmentId, companyId, terminatedBy);

        ApiResponse<VehicleAssignmentResponse> response = ApiResponse.success(assignment, "Assignment terminated successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Get all assignments for company with pagination
     */
    @GetMapping
    @Operation(summary = "Get assignments", description = "Retrieve all assignments for the company with pagination")
    @ApiResponse(responseCode = "200", description = "Assignments retrieved successfully")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER') or hasRole('DRIVER') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<PagedResponse<VehicleAssignmentResponse>>> getAssignments(
            @RequestParam(defaultValue = "0") @Min(0) @Parameter(description = "Page number") int page,
            @RequestParam(defaultValue = "20") @Min(1) @Parameter(description = "Page size") int size,
            @RequestParam(defaultValue = "assignedDate") @Parameter(description = "Sort field") String sortBy,
            @RequestParam(defaultValue = "desc") @Parameter(description = "Sort direction") String sortDir,
            Authentication authentication) {

        logger.debug("Get assignments request - page: {}, size: {}", page, size);

        UUID companyId = getCompanyIdFromAuth(authentication);

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        PagedResponse<VehicleAssignmentResponse> assignments = assignmentService.getAssignmentsByCompany(companyId, pageable);

        ApiResponse<PagedResponse<VehicleAssignmentResponse>> response = ApiResponse.success(assignments);

        return ResponseEntity.ok(response);
    }

    /**
     * Search assignments with advanced criteria
     */
    @PostMapping("/search")
    @Operation(summary = "Search assignments", description = "Search assignments using advanced criteria")
    @ApiResponse(responseCode = "200", description = "Search completed successfully")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER') or hasRole('DRIVER') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<PagedResponse<VehicleAssignmentResponse>>> searchAssignments(
            @Valid @RequestBody AssignmentSearchRequest searchRequest,
            @RequestParam(defaultValue = "0") @Min(0) @Parameter(description = "Page number") int page,
            @RequestParam(defaultValue = "20") @Min(1) @Parameter(description = "Page size") int size,
            @RequestParam(defaultValue = "assignedDate") @Parameter(description = "Sort field") String sortBy,
            @RequestParam(defaultValue = "desc") @Parameter(description = "Sort direction") String sortDir,
            Authentication authentication) {

        logger.debug("Search assignments request with criteria");

        UUID companyId = getCompanyIdFromAuth(authentication);

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        PagedResponse<VehicleAssignmentResponse> assignments = assignmentService.searchAssignments(searchRequest, companyId, pageable);

        ApiResponse<PagedResponse<VehicleAssignmentResponse>> response = ApiResponse.success(assignments);

        return ResponseEntity.ok(response);
    }

    /**
     * Get active assignments
     */
    @GetMapping("/active")
    @Operation(summary = "Get active assignments", description = "Retrieve all currently active assignments")
    @ApiResponse(responseCode = "200", description = "Active assignments retrieved successfully")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER') or hasRole('DRIVER') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<List<VehicleAssignmentResponse>>> getActiveAssignments(
            Authentication authentication) {

        logger.debug("Get active assignments request");

        UUID companyId = getCompanyIdFromAuth(authentication);

        List<VehicleAssignmentResponse> assignments = assignmentService.getActiveAssignments(companyId);

        ApiResponse<List<VehicleAssignmentResponse>> response = ApiResponse.success(assignments);

        return ResponseEntity.ok(response);
    }

    /**
     * Get assignments for a specific driver
     */
    @GetMapping("/driver/{driverId}")
    @Operation(summary = "Get driver assignments", description = "Retrieve all assignments for a specific driver")
    @ApiResponse(responseCode = "200", description = "Driver assignments retrieved successfully")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER') or hasRole('DRIVER') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<List<VehicleAssignmentResponse>>> getDriverAssignments(
            @PathVariable @Parameter(description = "Driver ID") UUID driverId,
            Authentication authentication) {

        logger.debug("Get assignments for driver: {}", driverId);

        UUID companyId = getCompanyIdFromAuth(authentication);

        List<VehicleAssignmentResponse> assignments = assignmentService.getDriverAssignments(driverId, companyId);

        ApiResponse<List<VehicleAssignmentResponse>> response = ApiResponse.success(assignments);

        return ResponseEntity.ok(response);
    }

    /**
     * Get assignments for a specific vehicle
     */
    @GetMapping("/vehicle/{vehicleId}")
    @Operation(summary = "Get vehicle assignments", description = "Retrieve all assignments for a specific vehicle")
    @ApiResponse(responseCode = "200", description = "Vehicle assignments retrieved successfully")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER') or hasRole('DRIVER') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<List<VehicleAssignmentResponse>>> getVehicleAssignments(
            @PathVariable @Parameter(description = "Vehicle ID") UUID vehicleId,
            Authentication authentication) {

        logger.debug("Get assignments for vehicle: {}", vehicleId);

        UUID companyId = getCompanyIdFromAuth(authentication);

        List<VehicleAssignmentResponse> assignments = assignmentService.getVehicleAssignments(vehicleId, companyId);

        ApiResponse<List<VehicleAssignmentResponse>> response = ApiResponse.success(assignments);

        return ResponseEntity.ok(response);
    }

    /**
     * Get currently checked-in assignments
     */
    @GetMapping("/checked-in")
    @Operation(summary = "Get checked-in assignments", description = "Retrieve assignments that are currently checked in")
    @ApiResponse(responseCode = "200", description = "Checked-in assignments retrieved successfully")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<List<VehicleAssignmentResponse>>> getCurrentlyCheckedInAssignments(
            Authentication authentication) {

        logger.debug("Get currently checked-in assignments request");

        UUID companyId = getCompanyIdFromAuth(authentication);

        List<VehicleAssignmentResponse> assignments = assignmentService.getCurrentlyCheckedInAssignments(companyId);

        ApiResponse<List<VehicleAssignmentResponse>> response = ApiResponse.success(assignments);

        return ResponseEntity.ok(response);
    }

    /**
     * Get expiring assignments
     */
    @GetMapping("/expiring")
    @Operation(summary = "Get expiring assignments", description = "Retrieve assignments that are expiring soon")
    @ApiResponse(responseCode = "200", description = "Expiring assignments retrieved successfully")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<List<VehicleAssignmentResponse>>> getExpiringAssignments(
            @RequestParam(defaultValue = "7") @Min(1) @Parameter(description = "Days threshold") int daysThreshold,
            Authentication authentication) {

        logger.debug("Get expiring assignments request - threshold: {} days", daysThreshold);

        UUID companyId = getCompanyIdFromAuth(authentication);

        List<VehicleAssignmentResponse> assignments = assignmentService.getExpiringAssignments(companyId, daysThreshold);

        ApiResponse<List<VehicleAssignmentResponse>> response = ApiResponse.success(assignments);

        return ResponseEntity.ok(response);
    }

    /**
     * Check-in driver to vehicle
     */
    @PostMapping("/check-in")
    @Operation(summary = "Check-in driver", description = "Check-in driver to assigned vehicle")
    @ApiResponse(responseCode = "200", description = "Check-in successful")
    @ApiResponse(responseCode = "400", description = "Invalid check-in request")
    @ApiResponse(responseCode = "409", description = "Driver already checked in")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER') or hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<CheckInOutResponse>> checkInDriver(
            @Valid @RequestBody VehicleCheckinRequest request,
            Authentication authentication) {

        logger.info("Check-in request for assignment: {}", request.getAssignmentId());

        UUID companyId = getCompanyIdFromAuth(authentication);
        UUID performedBy = getUserIdFromAuth(authentication);

        CheckInOutResponse result = assignmentService.checkInDriver(request, companyId, performedBy);

        ApiResponse<CheckInOutResponse> response = ApiResponse.success(result, "Driver checked in successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Check-out driver from vehicle
     */
    @PostMapping("/check-out")
    @Operation(summary = "Check-out driver", description = "Check-out driver from assigned vehicle")
    @ApiResponse(responseCode = "200", description = "Check-out successful")
    @ApiResponse(responseCode = "400", description = "Invalid check-out request")
    @ApiResponse(responseCode = "409", description = "Driver not checked in")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER') or hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<CheckInOutResponse>> checkOutDriver(
            @Valid @RequestBody VehicleCheckoutRequest request,
            Authentication authentication) {

        logger.info("Check-out request for assignment: {}", request.getAssignmentId());

        UUID companyId = getCompanyIdFromAuth(authentication);
        UUID performedBy = getUserIdFromAuth(authentication);

        CheckInOutResponse result = assignmentService.checkOutDriver(request, companyId, performedBy);

        ApiResponse<CheckInOutResponse> response = ApiResponse.success(result, "Driver checked out successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Validate assignment for conflicts
     */
    @PostMapping("/validate")
    @Operation(summary = "Validate assignment", description = "Validate assignment for conflicts and business rules")
    @ApiResponse(responseCode = "200", description = "Validation completed")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<AssignmentValidationResponse>> validateAssignment(
            @Valid @RequestBody AssignDriverRequest request,
            Authentication authentication) {

        logger.debug("Validate assignment request for vehicle: {} and driver: {}",
                request.getVehicleId(), request.getDriverId());

        UUID companyId = getCompanyIdFromAuth(authentication);

        AssignmentValidationResponse validation = assignmentService.validateAssignment(request, companyId, null);

        ApiResponse<AssignmentValidationResponse> response = ApiResponse.success(validation);

        return ResponseEntity.ok(response);
    }

    /**
     * Get assignment statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get assignment statistics", description = "Retrieve assignment statistics and analytics")
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<VehicleAssignmentService.AssignmentStatisticsResponse>> getAssignmentStatistics(
            Authentication authentication) {

        logger.debug("Get assignment statistics request");

        UUID companyId = getCompanyIdFromAuth(authentication);

        VehicleAssignmentService.AssignmentStatisticsResponse statistics = assignmentService.getAssignmentStatistics(companyId);

        ApiResponse<VehicleAssignmentService.AssignmentStatisticsResponse> response = ApiResponse.success(statistics);

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