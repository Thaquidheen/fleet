package com.fleetmanagement.vehicleservice.controller;

import com.fleetmanagement.vehicleservice.dto.request.*;
import com.fleetmanagement.vehicleservice.dto.response.*;
import com.fleetmanagement.vehicleservice.service.VehicleGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.fleetmanagement.vehicleservice.dto.response.VehicleApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

/**
 * Vehicle Group Controller
 *
 * REST API endpoints for vehicle group management including:
 * - Group CRUD operations
 * - Hierarchical group management
 * - Group search and filtering
 * - Group statistics and analytics
 */
@RestController
@RequestMapping("/api/vehicle-groups")
@Tag(name = "Vehicle Group Management", description = "Fleet organization and group management")
@Validated
public class VehicleGroupController {

    private static final Logger logger = LoggerFactory.getLogger(VehicleGroupController.class);

    private final VehicleGroupService vehicleGroupService;

    @Autowired
    public VehicleGroupController(VehicleGroupService vehicleGroupService) {
        this.vehicleGroupService = vehicleGroupService;
    }

    /**
     * Create a new vehicle group
     */
    @PostMapping
    @Operation(summary = "Create vehicle group", description = "Create a new vehicle group for fleet organization")
    @ApiResponse(responseCode = "201", description = "Vehicle group created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid group data")
    @ApiResponse(responseCode = "409", description = "Group name already exists")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<VehicleApiResponse<VehicleGroupResponse>> createVehicleGroup(
            @Valid @RequestBody CreateVehicleGroupRequest request,
            Authentication authentication) {

        logger.info("Create vehicle group request for: {}", request.getName());

        UUID companyId = getCompanyIdFromAuth(authentication);
        UUID createdBy = getUserIdFromAuth(authentication);

        VehicleGroupResponse group = vehicleGroupService.createVehicleGroup(request, companyId, createdBy);

        VehicleApiResponse<VehicleGroupResponse> response = VehicleApiResponse.success(group, "Vehicle group created successfully");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get vehicle group by ID
     */
    @GetMapping("/{groupId}")
    @Operation(summary = "Get vehicle group", description = "Retrieve vehicle group details by ID")
    @ApiResponse(responseCode = "200", description = "Vehicle group retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Vehicle group not found")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER') or hasRole('DRIVER') or hasRole('VIEWER')")
    public ResponseEntity<VehicleApiResponse<VehicleGroupResponse>> getVehicleGroup(
            @PathVariable @Parameter(description = "Vehicle group ID") UUID groupId,
            Authentication authentication) {

        logger.debug("Get vehicle group request for ID: {}", groupId);

        UUID companyId = getCompanyIdFromAuth(authentication);

        VehicleGroupResponse group = vehicleGroupService.getVehicleGroupById(groupId, companyId);

        VehicleApiResponse<VehicleGroupResponse> response = VehicleApiResponse.success(group);

        return ResponseEntity.ok(response);
    }

    /**
     * Update vehicle group
     */
    @PutMapping("/{groupId}")
    @Operation(summary = "Update vehicle group", description = "Update vehicle group information")
    @ApiResponse(responseCode = "200", description = "Vehicle group updated successfully")
    @ApiResponse(responseCode = "404", description = "Vehicle group not found")
    @ApiResponse(responseCode = "400", description = "Invalid update data")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<VehicleApiResponse<VehicleGroupResponse>> updateVehicleGroup(
            @PathVariable @Parameter(description = "Vehicle group ID") UUID groupId,
            @Valid @RequestBody UpdateVehicleGroupRequest request,
            Authentication authentication) {

        logger.info("Update vehicle group request for ID: {}", groupId);

        UUID companyId = getCompanyIdFromAuth(authentication);
        UUID updatedBy = getUserIdFromAuth(authentication);

        VehicleGroupResponse group = vehicleGroupService.updateVehicleGroup(groupId, request, companyId, updatedBy);

        VehicleApiResponse<VehicleGroupResponse> response = VehicleApiResponse.success(group, "Vehicle group updated successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Delete vehicle group
     */
    @DeleteMapping("/{groupId}")
    @Operation(summary = "Delete vehicle group", description = "Remove vehicle group from the system")
    @ApiResponse(responseCode = "204", description = "Vehicle group deleted successfully")
    @ApiResponse(responseCode = "404", description = "Vehicle group not found")
    @ApiResponse(responseCode = "409", description = "Cannot delete group with vehicles or child groups")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<VehicleApiResponse<Void>> deleteVehicleGroup(
            @PathVariable @Parameter(description = "Vehicle group ID") UUID groupId,
            Authentication authentication) {

        logger.info("Delete vehicle group request for ID: {}", groupId);

        UUID companyId = getCompanyIdFromAuth(authentication);
        UUID deletedBy = getUserIdFromAuth(authentication);

        vehicleGroupService.deleteVehicleGroup(groupId, companyId, deletedBy);

        VehicleApiResponse<Void> response = VehicleApiResponse.success(null, "Vehicle group deleted successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Get all vehicle groups for company
     */
    @GetMapping
    @Operation(summary = "Get vehicle groups", description = "Retrieve all vehicle groups for the company")
    @ApiResponse(responseCode = "200", description = "Vehicle groups retrieved successfully")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER') or hasRole('DRIVER') or hasRole('VIEWER')")
    public ResponseEntity<VehicleApiResponse<List<VehicleGroupResponse>>> getVehicleGroups(
            Authentication authentication) {

        logger.debug("Get vehicle groups request");

        UUID companyId = getCompanyIdFromAuth(authentication);

        List<VehicleGroupResponse> groups = vehicleGroupService.getVehicleGroupsByCompany(companyId);

        VehicleApiResponse<List<VehicleGroupResponse>> response = VehicleApiResponse.success(groups);

        return ResponseEntity.ok(response);
    }

    /**
     * Get root vehicle groups (no parent)
     */
    @GetMapping("/root")
    @Operation(summary = "Get root vehicle groups", description = "Retrieve top-level vehicle groups (no parent)")
    @ApiResponse(responseCode = "200", description = "Root vehicle groups retrieved successfully")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER') or hasRole('DRIVER') or hasRole('VIEWER')")
    public ResponseEntity<VehicleApiResponse<List<VehicleGroupResponse>>> getRootVehicleGroups(
            Authentication authentication) {

        logger.debug("Get root vehicle groups request");

        UUID companyId = getCompanyIdFromAuth(authentication);

        List<VehicleGroupResponse> groups = vehicleGroupService.getRootVehicleGroups(companyId);

        VehicleApiResponse<List<VehicleGroupResponse>> response = VehicleApiResponse.success(groups);

        return ResponseEntity.ok(response);
    }

    /**
     * Get child groups for a parent group
     */
    @GetMapping("/{parentGroupId}/children")
    @Operation(summary = "Get child groups", description = "Retrieve child groups for a parent group")
    @ApiResponse(responseCode = "200", description = "Child groups retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Parent group not found")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER') or hasRole('DRIVER') or hasRole('VIEWER')")
    public ResponseEntity<VehicleApiResponse<List<VehicleGroupResponse>>> getChildGroups(
            @PathVariable @Parameter(description = "Parent group ID") UUID parentGroupId,
            Authentication authentication) {

        logger.debug("Get child groups request for parent: {}", parentGroupId);

        UUID companyId = getCompanyIdFromAuth(authentication);

        List<VehicleGroupResponse> groups = vehicleGroupService.getChildGroups(parentGroupId, companyId);

        VehicleApiResponse<List<VehicleGroupResponse>> response = VehicleApiResponse.success(groups);

        return ResponseEntity.ok(response);
    }
}

    /**
     * Get complete group hierarchy
     */
//    @GetMapping("/hierarchy")
//    @Operation(summary = "Get group hierarchy", description = "Retrieve complete group hierarchy for the company")
//    @ApiResponse(responseCode = "200", description = "Group hierarchy retrieved successfully")
//    @PreAuthorize("hasRole('