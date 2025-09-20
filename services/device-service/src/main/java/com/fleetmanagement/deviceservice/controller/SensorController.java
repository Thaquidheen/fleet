package com.fleetmanagement.deviceservice.controller;

import com.fleetmanagement.deviceservice.dto.request.SensorSubscriptionRequest;
import com.fleetmanagement.deviceservice.dto.response.SensorSubscriptionResponse;
import com.fleetmanagement.deviceservice.dto.response.SensorTypeResponse;
import com.fleetmanagement.deviceservice.dto.common.ApiResponse;
import com.fleetmanagement.deviceservice.dto.common.PagedResponse;
import com.fleetmanagement.deviceservice.service.SensorSubscriptionService;
import com.fleetmanagement.deviceservice.service.SensorTypeService;
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
 * REST Controller for Sensor Management
 * 
 * Provides endpoints for managing sensor types, subscriptions,
 * and sensor data collection in the fleet management system.
 */
@RestController
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
@Tag(name = "Sensor Management", description = "Sensor type management and subscription operations")
public class SensorController {

    private final SensorTypeService sensorTypeService;
    private final SensorSubscriptionService sensorSubscriptionService;

    /**
     * Get all sensor types
     * 
     * @param pageable pagination parameters
     * @return paginated list of sensor types
     */
    @GetMapping("/types")
    @Operation(summary = "Get all sensor types", description = "Retrieve paginated list of all available sensor types")
    public ResponseEntity<ApiResponse<PagedResponse<SensorTypeResponse>>> getAllSensorTypes(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        PagedResponse<SensorTypeResponse> sensorTypes = sensorTypeService.getAllSensorTypes(pageable);
        return ResponseEntity.ok(ApiResponse.success(sensorTypes, "Sensor types retrieved successfully"));
    }

    /**
     * Get sensor type by ID
     * 
     * @param id sensor type ID
     * @return sensor type information
     */
    @GetMapping("/types/{id}")
    @Operation(summary = "Get sensor type by ID", description = "Retrieve sensor type information by ID")
    public ResponseEntity<ApiResponse<SensorTypeResponse>> getSensorTypeById(
            @Parameter(description = "Sensor Type ID") @PathVariable Long id) {
        SensorTypeResponse sensorType = sensorTypeService.getSensorTypeById(id);
        return ResponseEntity.ok(ApiResponse.success(sensorType, "Sensor type retrieved successfully"));
    }

    /**
     * Get sensor types by category
     * 
     * @param category sensor category
     * @return list of sensor types in category
     */
    @GetMapping("/types/category/{category}")
    @Operation(summary = "Get sensor types by category", description = "Retrieve sensor types by category")
    public ResponseEntity<ApiResponse<List<SensorTypeResponse>>> getSensorTypesByCategory(
            @Parameter(description = "Sensor Category") @PathVariable String category) {
        List<SensorTypeResponse> sensorTypes = sensorTypeService.getSensorTypesByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(sensorTypes, "Sensor types by category retrieved successfully"));
    }

    /**
     * Subscribe to sensor
     * 
     * @param request sensor subscription request
     * @return subscription response
     */
    @PostMapping("/subscribe")
    @Operation(summary = "Subscribe to sensor", description = "Subscribe device to a specific sensor type")
    public ResponseEntity<ApiResponse<SensorSubscriptionResponse>> subscribeToSensor(
            @Valid @RequestBody SensorSubscriptionRequest request) {
        SensorSubscriptionResponse subscription = sensorSubscriptionService.subscribeToSensor(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(subscription, "Sensor subscription created successfully"));
    }

    /**
     * Get all sensor subscriptions
     * 
     * @param pageable pagination parameters
     * @param deviceId filter by device ID
     * @param sensorTypeId filter by sensor type ID
     * @return paginated list of subscriptions
     */
    @GetMapping("/subscriptions")
    @Operation(summary = "Get sensor subscriptions", description = "Retrieve paginated list of sensor subscriptions")
    public ResponseEntity<ApiResponse<PagedResponse<SensorSubscriptionResponse>>> getSensorSubscriptions(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(description = "Filter by device ID") @RequestParam(required = false) Long deviceId,
            @Parameter(description = "Filter by sensor type ID") @RequestParam(required = false) Long sensorTypeId) {
        PagedResponse<SensorSubscriptionResponse> subscriptions = sensorSubscriptionService
                .getSensorSubscriptions(pageable, deviceId, sensorTypeId);
        return ResponseEntity.ok(ApiResponse.success(subscriptions, "Sensor subscriptions retrieved successfully"));
    }

    /**
     * Get sensor subscription by ID
     * 
     * @param id subscription ID
     * @return subscription information
     */
    @GetMapping("/subscriptions/{id}")
    @Operation(summary = "Get sensor subscription by ID", description = "Retrieve sensor subscription information by ID")
    public ResponseEntity<ApiResponse<SensorSubscriptionResponse>> getSensorSubscriptionById(
            @Parameter(description = "Subscription ID") @PathVariable Long id) {
        SensorSubscriptionResponse subscription = sensorSubscriptionService.getSensorSubscriptionById(id);
        return ResponseEntity.ok(ApiResponse.success(subscription, "Sensor subscription retrieved successfully"));
    }

    /**
     * Update sensor subscription
     * 
     * @param id subscription ID
     * @param request subscription update request
     * @return updated subscription information
     */
    @PutMapping("/subscriptions/{id}")
    @Operation(summary = "Update sensor subscription", description = "Update sensor subscription configuration")
    public ResponseEntity<ApiResponse<SensorSubscriptionResponse>> updateSensorSubscription(
            @Parameter(description = "Subscription ID") @PathVariable Long id,
            @Valid @RequestBody SensorSubscriptionRequest request) {
        SensorSubscriptionResponse subscription = sensorSubscriptionService.updateSensorSubscription(id, request);
        return ResponseEntity.ok(ApiResponse.success(subscription, "Sensor subscription updated successfully"));
    }

    /**
     * Unsubscribe from sensor
     * 
     * @param id subscription ID
     * @return success response
     */
    @DeleteMapping("/subscriptions/{id}")
    @Operation(summary = "Unsubscribe from sensor", description = "Remove sensor subscription")
    public ResponseEntity<ApiResponse<Void>> unsubscribeFromSensor(
            @Parameter(description = "Subscription ID") @PathVariable Long id) {
        sensorSubscriptionService.unsubscribeFromSensor(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Sensor subscription removed successfully"));
    }

    /**
     * Get sensor subscriptions by device
     * 
     * @param deviceId device ID
     * @return list of device sensor subscriptions
     */
    @GetMapping("/device/{deviceId}/subscriptions")
    @Operation(summary = "Get device sensor subscriptions", description = "Retrieve all sensor subscriptions for a device")
    public ResponseEntity<ApiResponse<List<SensorSubscriptionResponse>>> getDeviceSensorSubscriptions(
            @Parameter(description = "Device ID") @PathVariable Long deviceId) {
        List<SensorSubscriptionResponse> subscriptions = sensorSubscriptionService.getDeviceSensorSubscriptions(deviceId);
        return ResponseEntity.ok(ApiResponse.success(subscriptions, "Device sensor subscriptions retrieved successfully"));
    }

    /**
     * Get sensor subscriptions by sensor type
     * 
     * @param sensorTypeId sensor type ID
     * @param pageable pagination parameters
     * @return paginated list of subscriptions for sensor type
     */
    @GetMapping("/types/{sensorTypeId}/subscriptions")
    @Operation(summary = "Get subscriptions by sensor type", description = "Retrieve all subscriptions for a sensor type")
    public ResponseEntity<ApiResponse<PagedResponse<SensorSubscriptionResponse>>> getSubscriptionsBySensorType(
            @Parameter(description = "Sensor Type ID") @PathVariable Long sensorTypeId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<SensorSubscriptionResponse> subscriptions = sensorSubscriptionService
                .getSubscriptionsBySensorType(sensorTypeId, pageable);
        return ResponseEntity.ok(ApiResponse.success(subscriptions, "Sensor type subscriptions retrieved successfully"));
    }

    /**
     * Search sensor types
     * 
     * @param query search query
     * @param pageable pagination parameters
     * @return paginated search results
     */
    @GetMapping("/types/search")
    @Operation(summary = "Search sensor types", description = "Search sensor types by name or description")
    public ResponseEntity<ApiResponse<PagedResponse<SensorTypeResponse>>> searchSensorTypes(
            @Parameter(description = "Search query") @RequestParam String query,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        PagedResponse<SensorTypeResponse> sensorTypes = sensorTypeService.searchSensorTypes(query, pageable);
        return ResponseEntity.ok(ApiResponse.success(sensorTypes, "Search results retrieved successfully"));
    }
}

