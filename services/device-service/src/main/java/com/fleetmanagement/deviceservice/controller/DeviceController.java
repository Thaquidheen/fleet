// services/device-service/src/main/java/com/fleetmanagement/deviceservice/controller/DeviceController.java
package com.fleetmanagement.deviceservice.controller;

import com.fleetmanagement.deviceservice.dto.DeviceResponse;
import com.fleetmanagement.deviceservice.dto.request.RegisterDeviceRequest;
import com.fleetmanagement.deviceservice.dto.request.AssignDeviceRequest;
import com.fleetmanagement.deviceservice.dto.request.DeviceCommandRequest;
import com.fleetmanagement.deviceservice.dto.response.DeviceResponse;
import com.fleetmanagement.deviceservice.dto.response.DeviceListResponse;
import com.fleetmanagement.deviceservice.dto.response.CommandExecutionResponse;
import com.fleetmanagement.deviceservice.dto.common.ApiResponse;
import com.fleetmanagement.deviceservice.service.DeviceService;
import com.fleetmanagement.deviceservice.service.DeviceCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Device Management", description = "Device registration, management and control")
public class DeviceController {

    private final DeviceService deviceService;
    private final DeviceCommandService deviceCommandService;

    @PostMapping("/register")
    @Operation(summary = "Register a new device", description = "Register a new GPS tracking device")
    public ResponseEntity<ApiResponse<DeviceResponse>> registerDevice(
            @Valid @RequestBody RegisterDeviceRequest request) {

        log.info("Registering device: {}", request.getDeviceId());
        DeviceResponse response = deviceService.registerDevice(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Device registered successfully"));
    }

    @GetMapping("/{deviceId}")
    @Operation(summary = "Get device by ID", description = "Retrieve device information by UUID")
    public ResponseEntity<ApiResponse<DeviceResponse>> getDevice(@PathVariable UUID deviceId) {

        DeviceResponse response = deviceService.getDevice(deviceId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/device-id/{deviceId}")
    @Operation(summary = "Get device by device ID", description = "Retrieve device information by IMEI/device ID")
    public ResponseEntity<ApiResponse<DeviceResponse>> getDeviceByDeviceId(@PathVariable String deviceId) {

        DeviceResponse response = deviceService.getDeviceByDeviceId(deviceId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Get company devices", description = "Retrieve all devices for a company")
    public ResponseEntity<ApiResponse<DeviceListResponse>> getCompanyDevices(
            @PathVariable UUID companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<DeviceResponse> devices = deviceService.getCompanyDevices(companyId, pageable);

        DeviceListResponse response = DeviceListResponse.builder()
                .devices(devices.getContent())
                .totalElements(devices.getTotalElements())
                .totalPages(devices.getTotalPages())
                .currentPage(devices.getNumber())
                .pageSize(devices.getSize())
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{deviceId}/assign")
    @Operation(summary = "Assign device", description = "Assign device to vehicle or user")
    public ResponseEntity<ApiResponse<DeviceResponse>> assignDevice(
            @PathVariable UUID deviceId,
            @Valid @RequestBody AssignDeviceRequest request) {

        DeviceResponse response;

        if (request.getVehicleId() != null) {
            response = deviceService.assignDeviceToVehicle(deviceId, request.getVehicleId());
        } else if (request.getUserId() != null) {
            response = deviceService.assignDeviceToUser(deviceId, request.getUserId());
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Either vehicleId or userId must be provided"));
        }

        return ResponseEntity.ok(ApiResponse.success(response, "Device assigned successfully"));
    }

    @PostMapping("/{deviceId}/activate")
    @Operation(summary = "Activate device", description = "Activate a device for tracking")
    public ResponseEntity<ApiResponse<String>> activateDevice(@PathVariable UUID deviceId) {

        deviceService.activateDevice(deviceId);
        return ResponseEntity.ok(ApiResponse.success("Device activated successfully"));
    }

    @PostMapping("/{deviceId}/deactivate")
    @Operation(summary = "Deactivate device", description = "Deactivate a device")
    public ResponseEntity<ApiResponse<String>> deactivateDevice(@PathVariable UUID deviceId) {

        deviceService.deactivateDevice(deviceId);
        return ResponseEntity.ok(ApiResponse.success("Device deactivated successfully"));
    }

    @DeleteMapping("/{deviceId}")
    @Operation(summary = "Delete device", description = "Delete a device (soft delete)")
    public ResponseEntity<ApiResponse<String>> deleteDevice(@PathVariable UUID deviceId) {

        deviceService.deleteDevice(deviceId);
        return ResponseEntity.ok(ApiResponse.success("Device deleted successfully"));
    }

    @PostMapping("/{deviceId}/commands")
    @Operation(summary = "Send command to device", description = "Send a command to the device via Traccar")
    public ResponseEntity<ApiResponse<CommandExecutionResponse>> sendCommand(
            @PathVariable UUID deviceId,
            @Valid @RequestBody DeviceCommandRequest request) {

        CommandExecutionResponse response = deviceCommandService.sendCommand(deviceId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Command sent successfully"));
    }

    @GetMapping("/{deviceId}/commands")
    @Operation(summary = "Get device commands", description = "Retrieve command history for device")
    public ResponseEntity<ApiResponse<Page<CommandExecutionResponse>>> getDeviceCommands(
            @PathVariable UUID deviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt"));
        Page<CommandExecutionResponse> commands = deviceCommandService.getDeviceCommands(deviceId, pageable);

        return ResponseEntity.ok(ApiResponse.success(commands));
    }

    @GetMapping("/{deviceId}/health")
    @Operation(summary = "Get device health", description = "Get current device health status")
    public ResponseEntity<ApiResponse<DeviceResponse>> getDeviceHealth(@PathVariable UUID deviceId) {

        DeviceResponse response = deviceService.getDeviceHealth(deviceId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

