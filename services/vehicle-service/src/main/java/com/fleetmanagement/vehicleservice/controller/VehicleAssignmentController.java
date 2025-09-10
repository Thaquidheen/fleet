package com.fleetmanagement.vehicleservice.controller;

import com.fleetmanagement.vehicleservice.dto.request.AssignDriverRequest;
import com.fleetmanagement.vehicleservice.dto.request.UpdateAssignmentRequest;
import com.fleetmanagement.vehicleservice.dto.response.ApiResponse;
import com.fleetmanagement.vehicleservice.dto.response.VehicleAssignmentResponse;
import com.fleetmanagement.vehicleservice.service.VehicleAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vehicle-assignments")
@Tag(name = "Vehicle Assignment Management", description = "Driver-vehicle assignment operations with real-time validation")
@Validated
public class VehicleAssignmentController {

    private static final Logger logger = LoggerFactory.getLogger(VehicleAssignmentController.class);

    private final VehicleAssignmentService assignmentService;

    @Autowired
    public VehicleAssignmentController(VehicleAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    /**
     * Assign driver to vehicle with comprehensive validation
     */
    @PostMapping
    @Operation(summary = "Assign driver to vehicle", description = "Create driver-vehicle assignment with real-time availability checking")
    @SwaggerApiResponse(responseCode = "201", description = "Assignment created successfully")
    @SwaggerApiResponse(responseCode = "400", description = "Invalid assignment data")
    @SwaggerApiResponse(responseCode = "409", description = "Assignment conflict or driver not available")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<VehicleAssignmentResponse>> assignDriverToVehicle(
            @Valid @RequestBody AssignDriverRequest request,
            Authentication authentication) {

        logger.info("Assign driver {} to vehicle {} request", request.getDriverId(), request.getVehicleId());

        UUID companyId = getCompanyIdFromAuth(authentication);
        UUID assignedBy = getUserIdFromAuth(authentication);

        VehicleAssignmentResponse response = assignmentService.assignDriverToVehicle(request, companyId, assignedBy);

        ApiResponse<VehicleAssignmentResponse> apiResponse = ApiResponse.<VehicleAssignmentResponse>builder()
                .success(true)
                .data(response)
                .message("Driver assigned to vehicle successfully")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    /**
     * Get assignments for a specific driver
     */
    @GetMapping("/driver/{driverId}")
    @Operation(summary = "Get driver assignments", description = "Retrieve all assignments for a specific driver")
    @SwaggerApiResponse(responseCode = "200", description = "Driver assignments retrieved successfully")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER') or @vehicleAssignmentService.canAccessDriverAssignments(authentication.name, #driverId)")
    public ResponseEntity<ApiResponse<List<VehicleAssignmentResponse>>> getDriverAssignments(
            @PathVariable @Parameter(description = "Driver ID") UUID driverId,
            Authentication authentication) {

        logger.debug("Get assignments for driver: {}", driverId);

        UUID companyId = getCompanyIdFromAuth(authentication);

        List<VehicleAssignmentResponse> assignments = assignmentService.getDriverAssignments(driverId, companyId);

        ApiResponse<List<VehicleAssignmentResponse>> response = ApiResponse.<List<VehicleAssignmentResponse>>builder()
                .success(true)
                .data(assignments)
                .message("Driver assignments retrieved successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Get assignments for a specific vehicle
     */
    @GetMapping("/vehicle/{vehicleId}")
    @Operation(summary = "Get vehicle assignments", description = "Retrieve all assignments for a specific vehicle")
    @SwaggerApiResponse(responseCode = "200", description = "Vehicle assignments retrieved successfully")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER') or hasRole('DRIVER') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<List<VehicleAssignmentResponse>>> getVehicleAssignments(
            @PathVariable @Parameter(description = "Vehicle ID") UUID vehicleId,
            Authentication authentication) {

        logger.debug("Get assignments for vehicle: {}", vehicleId);

        UUID companyId = getCompanyIdFromAuth(authentication);

        List<VehicleAssignmentResponse> assignments = assignmentService.getVehicleAssignments(vehicleId, companyId);

        ApiResponse<List<VehicleAssignmentResponse>> response = ApiResponse.<List<VehicleAssignmentResponse>>builder()
                .success(true)
                .data(assignments)
                .message("Vehicle assignments retrieved successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Get currently active assignments
     */
    @GetMapping("/active")
    @Operation(summary = "Get active assignments", description = "Retrieve all currently active assignments for the company")
    @SwaggerApiResponse(responseCode = "200", description = "Active assignments retrieved successfully")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<List<VehicleAssignmentResponse>>> getActiveAssignments(
            Authentication authentication) {

        logger.debug("Get active assignments request");

        UUID companyId = getCompanyIdFromAuth(authentication);

        List<VehicleAssignmentResponse> assignments = assignmentService.getActiveAssignments(companyId);

        ApiResponse<List<VehicleAssignmentResponse>> response = ApiResponse.<List<VehicleAssignmentResponse>>builder()
                .success(true)
                .data(assignments)
                .message("Active assignments retrieved successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Terminate assignment
     */
    @PostMapping("/{assignmentId}/terminate")
    @Operation(summary = "Terminate assignment", description = "Terminate an active assignment and notify User Service")
    @SwaggerApiResponse(responseCode = "200", description = "Assignment terminated successfully")
    @SwaggerApiResponse(responseCode = "404", description = "Assignment not found")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<VehicleAssignmentResponse>> terminateAssignment(
            @PathVariable @Parameter(description = "Assignment ID") UUID assignmentId,
            Authentication authentication) {

        logger.info("Terminate assignment request for ID: {}", assignmentId);

        UUID companyId = getCompanyIdFromAuth(authentication);
        UUID terminatedBy = getUserIdFromAuth(authentication);

        VehicleAssignmentResponse response = assignmentService.terminateAssignment(assignmentId, companyId, terminatedBy);

        ApiResponse<VehicleAssignmentResponse> apiResponse = ApiResponse.<VehicleAssignmentResponse>builder()
                .success(true)
                .data(response)
                .message("Assignment terminated successfully")
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Check-in assignment
     */
    @PostMapping("/{assignmentId}/check-in")
    @Operation(summary = "Check-in assignment", description = "Check-in driver for vehicle usage")
    @SwaggerApiResponse(responseCode = "200", description = "Assignment checked in successfully")
    @SwaggerApiResponse(responseCode = "404", description = "Assignment not found")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER') or @vehicleAssignmentService.canCheckInAssignment(authentication.name, #assignmentId)")
    public ResponseEntity<ApiResponse<VehicleAssignmentResponse>> checkInAssignment(
            @PathVariable @Parameter(description = "Assignment ID") UUID assignmentId,
            Authentication authentication) {

        logger.info("Check-in assignment request for ID: {}", assignmentId);

        UUID companyId = getCompanyIdFromAuth(authentication);
        UUID checkedInBy = getUserIdFromAuth(authentication);

        VehicleAssignmentResponse response = assignmentService.checkInAssignment(assignmentId, companyId, checkedInBy);

        ApiResponse<VehicleAssignmentResponse> apiResponse = ApiResponse.<VehicleAssignmentResponse>builder()
                .success(true)
                .data(response)
                .message("Assignment checked in successfully")
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Check-out assignment
     */
    @PostMapping("/{assignmentId}/check-out")
    @Operation(summary = "Check-out assignment", description = "Check-out driver from vehicle usage")
    @SwaggerApiResponse(responseCode = "200", description = "Assignment checked out successfully")
    @SwaggerApiResponse(responseCode = "404", description = "Assignment not found")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER') or @vehicleAssignmentService.canCheckOutAssignment(authentication.name, #assignmentId)")
    public ResponseEntity<ApiResponse<VehicleAssignmentResponse>> checkOutAssignment(
            @PathVariable @Parameter(description = "Assignment ID") UUID assignmentId,
            Authentication authentication) {

        logger.info("Check-out assignment request for ID: {}", assignmentId);

        UUID companyId = getCompanyIdFromAuth(authentication);
        UUID checkedOutBy = getUserIdFromAuth(authentication);

        VehicleAssignmentResponse response = assignmentService.checkOutAssignment(assignmentId, companyId, checkedOutBy);

        ApiResponse<VehicleAssignmentResponse> apiResponse = ApiResponse.<VehicleAssignmentResponse>builder()
                .success(true)
                .data(response)
                .message("Assignment checked out successfully")
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    // Helper methods
    private UUID getUserIdFromAuth(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    private UUID getCompanyIdFromAuth(Authentication authentication) {
        return UUID.fromString(authentication.getDetails().toString());
    }
}