package com.fleetmanagement.deviceservice.controller;

import com.fleetmanagement.deviceservice.dto.request.DeviceTypeRequest;
import com.fleetmanagement.deviceservice.dto.response.DeviceTypeResponse;
import com.fleetmanagement.deviceservice.dto.common.ApiResponse;
import com.fleetmanagement.deviceservice.dto.common.PagedResponse;
import com.fleetmanagement.deviceservice.service.DeviceTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Device Type Management
 * 
 * Provides endpoints for managing device types and their configurations
 * in the fleet management system.
 */
@RestController
@RequestMapping("/api/device-types")
@RequiredArgsConstructor
@Tag(name = "Device Type Management", description = "Device type creation, management, and configuration operations")
public class DeviceTypeController {

    private final DeviceTypeService deviceTypeService;

    /**
     * Create a new device type
     * 
     * @param request device type creation request
     * @return created device type information
     */
    @PostMapping
    @Operation(summary = "Create device type", description = "Create a new device type with specifications")
    public ResponseEntity<ApiResponse<DeviceTypeResponse>> createDeviceType(
            @Valid @RequestBody DeviceTypeRequest request) {
        DeviceTypeResponse deviceType = deviceTypeService.createDeviceType(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(deviceType, "Device type created successfully"));
    }

    /**
     * Get all device types with pagination
     * 
     * @param pageable pagination parameters
     * @return paginated list of device types
     */
    @GetMapping
    @Operation(summary = "Get all device types", description = "Retrieve paginated list of all device types")
    public ResponseEntity<ApiResponse<PagedResponse<DeviceTypeResponse>>> getAllDeviceTypes(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        PagedResponse<DeviceTypeResponse> deviceTypes = deviceTypeService.getAllDeviceTypes(pageable);
        return ResponseEntity.ok(ApiResponse.success(deviceTypes, "Device types retrieved successfully"));
    }

    /**
     * Get device type by ID
     * 
     * @param id device type ID
     * @return device type information
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get device type by ID", description = "Retrieve device type information by ID")
    public ResponseEntity<ApiResponse<DeviceTypeResponse>> getDeviceTypeById(
            @Parameter(description = "Device Type ID") @PathVariable Long id) {
        DeviceTypeResponse deviceType = deviceTypeService.getDeviceTypeById(id);
        return ResponseEntity.ok(ApiResponse.success(deviceType, "Device type retrieved successfully"));
    }

    /**
     * Update device type
     * 
     * @param id device type ID
     * @param request device type update request
     * @return updated device type information
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update device type", description = "Update device type information and specifications")
    public ResponseEntity<ApiResponse<DeviceTypeResponse>> updateDeviceType(
            @Parameter(description = "Device Type ID") @PathVariable Long id,
            @Valid @RequestBody DeviceTypeRequest request) {
        DeviceTypeResponse deviceType = deviceTypeService.updateDeviceType(id, request);
        return ResponseEntity.ok(ApiResponse.success(deviceType, "Device type updated successfully"));
    }

    /**
     * Delete device type
     * 
     * @param id device type ID
     * @return success response
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete device type", description = "Delete device type from the system")
    public ResponseEntity<ApiResponse<Void>> deleteDeviceType(
            @Parameter(description = "Device Type ID") @PathVariable Long id) {
        deviceTypeService.deleteDeviceType(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Device type deleted successfully"));
    }

    /**
     * Get device types by category
     * 
     * @param category device category
     * @return list of device types in category
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "Get device types by category", description = "Retrieve device types by category")
    public ResponseEntity<ApiResponse<List<DeviceTypeResponse>>> getDeviceTypesByCategory(
            @Parameter(description = "Device Category") @PathVariable String category) {
        List<DeviceTypeResponse> deviceTypes = deviceTypeService.getDeviceTypesByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(deviceTypes, "Device types by category retrieved successfully"));
    }

    /**
     * Get device types by brand
     * 
     * @param brand device brand
     * @return list of device types by brand
     */
    @GetMapping("/brand/{brand}")
    @Operation(summary = "Get device types by brand", description = "Retrieve device types by brand")
    public ResponseEntity<ApiResponse<List<DeviceTypeResponse>>> getDeviceTypesByBrand(
            @Parameter(description = "Device Brand") @PathVariable String brand) {
        List<DeviceTypeResponse> deviceTypes = deviceTypeService.getDeviceTypesByBrand(brand);
        return ResponseEntity.ok(ApiResponse.success(deviceTypes, "Device types by brand retrieved successfully"));
    }

    /**
     * Search device types
     * 
     * @param query search query
     * @param pageable pagination parameters
     * @return paginated search results
     */
    @GetMapping("/search")
    @Operation(summary = "Search device types", description = "Search device types by name or description")
    public ResponseEntity<ApiResponse<PagedResponse<DeviceTypeResponse>>> searchDeviceTypes(
            @Parameter(description = "Search query") @RequestParam String query,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        PagedResponse<DeviceTypeResponse> deviceTypes = deviceTypeService.searchDeviceTypes(query, pageable);
        return ResponseEntity.ok(ApiResponse.success(deviceTypes, "Search results retrieved successfully"));
    }
}


