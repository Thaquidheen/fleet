package com.fleetmanagement.deviceservice.controller;

import com.fleetmanagement.deviceservice.dto.request.RegisterMobileDeviceRequest;
import com.fleetmanagement.deviceservice.dto.request.UpdateMobileDeviceRequest;
import com.fleetmanagement.deviceservice.dto.response.MobileDeviceResponse;
import com.fleetmanagement.deviceservice.dto.common.ApiResponse;
import com.fleetmanagement.deviceservice.dto.common.PagedResponse;
import com.fleetmanagement.deviceservice.service.MobileDeviceService;
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
 * REST Controller for Mobile Device Management
 * 
 * Provides endpoints for managing mobile devices, their configurations,
 * and mobile-specific operations in the fleet management system.
 */
@RestController
@RequestMapping("/api/mobile-devices")
@RequiredArgsConstructor
@Tag(name = "Mobile Device Management", description = "Mobile device registration, management, and configuration operations")
public class MobileDeviceController {

    private final MobileDeviceService mobileDeviceService;

    /**
     * Register a new mobile device
     * 
     * @param request mobile device registration request
     * @return registered mobile device information
     */
    @PostMapping
    @Operation(summary = "Register mobile device", description = "Register a new mobile device in the fleet management system")
    public ResponseEntity<ApiResponse<MobileDeviceResponse>> registerMobileDevice(
            @Valid @RequestBody RegisterMobileDeviceRequest request) {
        MobileDeviceResponse mobileDevice = mobileDeviceService.registerMobileDevice(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(mobileDevice, "Mobile device registered successfully"));
    }

    /**
     * Get all mobile devices with pagination and filtering
     * 
     * @param pageable pagination parameters
     * @param companyId filter by company ID
     * @param userId filter by user ID
     * @param status filter by device status
     * @return paginated list of mobile devices
     */
    @GetMapping
    @Operation(summary = "Get all mobile devices", description = "Retrieve paginated list of mobile devices with optional filtering")
    public ResponseEntity<ApiResponse<PagedResponse<MobileDeviceResponse>>> getAllMobileDevices(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(description = "Filter by company ID") @RequestParam(required = false) Long companyId,
            @Parameter(description = "Filter by user ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "Filter by device status") @RequestParam(required = false) String status) {
        
        PagedResponse<MobileDeviceResponse> mobileDevices = mobileDeviceService
                .getAllMobileDevices(pageable, companyId, userId, status);
        return ResponseEntity.ok(ApiResponse.success(mobileDevices, "Mobile devices retrieved successfully"));
    }

    /**
     * Get mobile device by ID
     * 
     * @param id mobile device ID
     * @return mobile device information
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get mobile device by ID", description = "Retrieve mobile device information by ID")
    public ResponseEntity<ApiResponse<MobileDeviceResponse>> getMobileDeviceById(
            @Parameter(description = "Mobile Device ID") @PathVariable Long id) {
        MobileDeviceResponse mobileDevice = mobileDeviceService.getMobileDeviceById(id);
        return ResponseEntity.ok(ApiResponse.success(mobileDevice, "Mobile device retrieved successfully"));
    }

    /**
     * Update mobile device information
     * 
     * @param id mobile device ID
     * @param request mobile device update request
     * @return updated mobile device information
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update mobile device", description = "Update mobile device information and configuration")
    public ResponseEntity<ApiResponse<MobileDeviceResponse>> updateMobileDevice(
            @Parameter(description = "Mobile Device ID") @PathVariable Long id,
            @Valid @RequestBody UpdateMobileDeviceRequest request) {
        MobileDeviceResponse mobileDevice = mobileDeviceService.updateMobileDevice(id, request);
        return ResponseEntity.ok(ApiResponse.success(mobileDevice, "Mobile device updated successfully"));
    }

    /**
     * Delete mobile device
     * 
     * @param id mobile device ID
     * @return success response
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete mobile device", description = "Delete mobile device from the system")
    public ResponseEntity<ApiResponse<Void>> deleteMobileDevice(
            @Parameter(description = "Mobile Device ID") @PathVariable Long id) {
        mobileDeviceService.deleteMobileDevice(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Mobile device deleted successfully"));
    }

    /**
     * Get mobile devices by company
     * 
     * @param companyId company ID
     * @param pageable pagination parameters
     * @return paginated list of company mobile devices
     */
    @GetMapping("/company/{companyId}")
    @Operation(summary = "Get mobile devices by company", description = "Retrieve all mobile devices belonging to a specific company")
    public ResponseEntity<ApiResponse<PagedResponse<MobileDeviceResponse>>> getMobileDevicesByCompany(
            @Parameter(description = "Company ID") @PathVariable Long companyId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<MobileDeviceResponse> mobileDevices = mobileDeviceService
                .getMobileDevicesByCompany(companyId, pageable);
        return ResponseEntity.ok(ApiResponse.success(mobileDevices, "Company mobile devices retrieved successfully"));
    }

    /**
     * Get mobile devices by user
     * 
     * @param userId user ID
     * @return list of user mobile devices
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get mobile devices by user", description = "Retrieve all mobile devices assigned to a specific user")
    public ResponseEntity<ApiResponse<List<MobileDeviceResponse>>> getMobileDevicesByUser(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        List<MobileDeviceResponse> mobileDevices = mobileDeviceService.getMobileDevicesByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(mobileDevices, "User mobile devices retrieved successfully"));
    }

    /**
     * Activate mobile device
     * 
     * @param id mobile device ID
     * @return activated mobile device information
     */
    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate mobile device", description = "Activate mobile device for use")
    public ResponseEntity<ApiResponse<MobileDeviceResponse>> activateMobileDevice(
            @Parameter(description = "Mobile Device ID") @PathVariable Long id) {
        MobileDeviceResponse mobileDevice = mobileDeviceService.activateMobileDevice(id);
        return ResponseEntity.ok(ApiResponse.success(mobileDevice, "Mobile device activated successfully"));
    }

    /**
     * Deactivate mobile device
     * 
     * @param id mobile device ID
     * @return deactivated mobile device information
     */
    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate mobile device", description = "Deactivate mobile device")
    public ResponseEntity<ApiResponse<MobileDeviceResponse>> deactivateMobileDevice(
            @Parameter(description = "Mobile Device ID") @PathVariable Long id) {
        MobileDeviceResponse mobileDevice = mobileDeviceService.deactivateMobileDevice(id);
        return ResponseEntity.ok(ApiResponse.success(mobileDevice, "Mobile device deactivated successfully"));
    }

    /**
     * Update mobile device configuration
     * 
     * @param id mobile device ID
     * @param configuration configuration update request
     * @return updated mobile device information
     */
    @PutMapping("/{id}/configuration")
    @Operation(summary = "Update mobile device configuration", description = "Update mobile device configuration settings")
    public ResponseEntity<ApiResponse<MobileDeviceResponse>> updateMobileDeviceConfiguration(
            @Parameter(description = "Mobile Device ID") @PathVariable Long id,
            @Valid @RequestBody Object configuration) {
        MobileDeviceResponse mobileDevice = mobileDeviceService.updateMobileDeviceConfiguration(id, configuration);
        return ResponseEntity.ok(ApiResponse.success(mobileDevice, "Mobile device configuration updated successfully"));
    }

    /**
     * Search mobile devices
     * 
     * @param query search query
     * @param pageable pagination parameters
     * @return paginated search results
     */
    @GetMapping("/search")
    @Operation(summary = "Search mobile devices", description = "Search mobile devices by device name, IMEI, or phone number")
    public ResponseEntity<ApiResponse<PagedResponse<MobileDeviceResponse>>> searchMobileDevices(
            @Parameter(description = "Search query") @RequestParam String query,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<MobileDeviceResponse> mobileDevices = mobileDeviceService.searchMobileDevices(query, pageable);
        return ResponseEntity.ok(ApiResponse.success(mobileDevices, "Search results retrieved successfully"));
    }

    /**
     * Get mobile device statistics
     * 
     * @param companyId company ID (optional)
     * @return mobile device statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get mobile device statistics", description = "Retrieve mobile device statistics and metrics")
    public ResponseEntity<ApiResponse<Object>> getMobileDeviceStatistics(
            @Parameter(description = "Company ID") @RequestParam(required = false) Long companyId) {
        Object statistics = mobileDeviceService.getMobileDeviceStatistics(companyId);
        return ResponseEntity.ok(ApiResponse.success(statistics, "Mobile device statistics retrieved successfully"));
    }
}


