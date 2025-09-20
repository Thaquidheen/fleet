package com.fleetmanagement.deviceservice.controller;

import com.fleetmanagement.deviceservice.dto.request.DeviceCommandRequest;
import com.fleetmanagement.deviceservice.dto.response.CommandExecutionResponse;
import com.fleetmanagement.deviceservice.dto.response.DeviceCommandResponse;
import com.fleetmanagement.deviceservice.dto.common.ApiResponse;
import com.fleetmanagement.deviceservice.dto.common.PagedResponse;
import com.fleetmanagement.deviceservice.service.DeviceCommandService;
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
 * REST Controller for Device Command Management
 * 
 * Provides endpoints for sending commands to devices,
 * tracking command execution status, and managing command history.
 */
@RestController
@RequestMapping("/api/commands")
@RequiredArgsConstructor
@Tag(name = "Device Command Management", description = "Device command execution and status tracking")
public class DeviceCommandController {

    private final DeviceCommandService deviceCommandService;

    /**
     * Send command to device
     * 
     * @param request device command request
     * @return command execution response
     */
    @PostMapping
    @Operation(summary = "Send command to device", description = "Send a command to a specific device")
    public ResponseEntity<ApiResponse<CommandExecutionResponse>> sendCommand(
            @Valid @RequestBody DeviceCommandRequest request) {
        CommandExecutionResponse response = deviceCommandService.sendCommand(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(response, "Command sent successfully"));
    }

    /**
     * Get command execution status
     * 
     * @param commandId command ID
     * @return command execution status
     */
    @GetMapping("/{commandId}/status")
    @Operation(summary = "Get command status", description = "Retrieve the execution status of a specific command")
    public ResponseEntity<ApiResponse<CommandExecutionResponse>> getCommandStatus(
            @Parameter(description = "Command ID") @PathVariable Long commandId) {
        CommandExecutionResponse response = deviceCommandService.getCommandStatus(commandId);
        return ResponseEntity.ok(ApiResponse.success(response, "Command status retrieved successfully"));
    }

    /**
     * Get all commands with pagination and filtering
     * 
     * @param pageable pagination parameters
     * @param deviceId filter by device ID
     * @param status filter by command status
     * @param commandType filter by command type
     * @return paginated list of commands
     */
    @GetMapping
    @Operation(summary = "Get all commands", description = "Retrieve paginated list of commands with optional filtering")
    public ResponseEntity<ApiResponse<PagedResponse<DeviceCommandResponse>>> getAllCommands(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(description = "Filter by device ID") @RequestParam(required = false) Long deviceId,
            @Parameter(description = "Filter by command status") @RequestParam(required = false) String status,
            @Parameter(description = "Filter by command type") @RequestParam(required = false) String commandType) {
        
        PagedResponse<DeviceCommandResponse> commands = deviceCommandService
                .getAllCommands(pageable, deviceId, status, commandType);
        return ResponseEntity.ok(ApiResponse.success(commands, "Commands retrieved successfully"));
    }

    /**
     * Get command by ID
     * 
     * @param commandId command ID
     * @return command information
     */
    @GetMapping("/{commandId}")
    @Operation(summary = "Get command by ID", description = "Retrieve command information by ID")
    public ResponseEntity<ApiResponse<DeviceCommandResponse>> getCommandById(
            @Parameter(description = "Command ID") @PathVariable Long commandId) {
        DeviceCommandResponse command = deviceCommandService.getCommandById(commandId);
        return ResponseEntity.ok(ApiResponse.success(command, "Command retrieved successfully"));
    }

    /**
     * Get commands by device
     * 
     * @param deviceId device ID
     * @param pageable pagination parameters
     * @return paginated list of device commands
     */
    @GetMapping("/device/{deviceId}")
    @Operation(summary = "Get commands by device", description = "Retrieve all commands for a specific device")
    public ResponseEntity<ApiResponse<PagedResponse<DeviceCommandResponse>>> getCommandsByDevice(
            @Parameter(description = "Device ID") @PathVariable Long deviceId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        PagedResponse<DeviceCommandResponse> commands = deviceCommandService
                .getCommandsByDevice(deviceId, pageable);
        return ResponseEntity.ok(ApiResponse.success(commands, "Device commands retrieved successfully"));
    }

    /**
     * Get pending commands
     * 
     * @param pageable pagination parameters
     * @param deviceId filter by device ID
     * @return paginated list of pending commands
     */
    @GetMapping("/pending")
    @Operation(summary = "Get pending commands", description = "Retrieve all pending commands")
    public ResponseEntity<ApiResponse<PagedResponse<DeviceCommandResponse>>> getPendingCommands(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable,
            @Parameter(description = "Filter by device ID") @RequestParam(required = false) Long deviceId) {
        
        PagedResponse<DeviceCommandResponse> commands = deviceCommandService
                .getPendingCommands(pageable, deviceId);
        return ResponseEntity.ok(ApiResponse.success(commands, "Pending commands retrieved successfully"));
    }

    /**
     * Get failed commands
     * 
     * @param pageable pagination parameters
     * @param deviceId filter by device ID
     * @return paginated list of failed commands
     */
    @GetMapping("/failed")
    @Operation(summary = "Get failed commands", description = "Retrieve all failed commands")
    public ResponseEntity<ApiResponse<PagedResponse<DeviceCommandResponse>>> getFailedCommands(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(description = "Filter by device ID") @RequestParam(required = false) Long deviceId) {
        
        PagedResponse<DeviceCommandResponse> commands = deviceCommandService
                .getFailedCommands(pageable, deviceId);
        return ResponseEntity.ok(ApiResponse.success(commands, "Failed commands retrieved successfully"));
    }

    /**
     * Retry failed command
     * 
     * @param commandId command ID
     * @return retry response
     */
    @PostMapping("/{commandId}/retry")
    @Operation(summary = "Retry failed command", description = "Retry a failed command")
    public ResponseEntity<ApiResponse<CommandExecutionResponse>> retryCommand(
            @Parameter(description = "Command ID") @PathVariable Long commandId) {
        CommandExecutionResponse response = deviceCommandService.retryCommand(commandId);
        return ResponseEntity.ok(ApiResponse.success(response, "Command retry initiated successfully"));
    }

    /**
     * Cancel pending command
     * 
     * @param commandId command ID
     * @return cancellation response
     */
    @PostMapping("/{commandId}/cancel")
    @Operation(summary = "Cancel pending command", description = "Cancel a pending command")
    public ResponseEntity<ApiResponse<Void>> cancelCommand(
            @Parameter(description = "Command ID") @PathVariable Long commandId) {
        deviceCommandService.cancelCommand(commandId);
        return ResponseEntity.ok(ApiResponse.success(null, "Command cancelled successfully"));
    }

    /**
     * Get command execution history
     * 
     * @param deviceId device ID
     * @param pageable pagination parameters
     * @return paginated command execution history
     */
    @GetMapping("/device/{deviceId}/history")
    @Operation(summary = "Get command execution history", description = "Retrieve command execution history for a device")
    public ResponseEntity<ApiResponse<PagedResponse<DeviceCommandResponse>>> getCommandExecutionHistory(
            @Parameter(description = "Device ID") @PathVariable Long deviceId,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        PagedResponse<DeviceCommandResponse> history = deviceCommandService
                .getCommandExecutionHistory(deviceId, pageable);
        return ResponseEntity.ok(ApiResponse.success(history, "Command execution history retrieved successfully"));
    }

    /**
     * Get command statistics
     * 
     * @param deviceId device ID (optional)
     * @param timeRange time range for statistics (e.g., "1h", "24h", "7d")
     * @return command statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get command statistics", description = "Retrieve command execution statistics")
    public ResponseEntity<ApiResponse<Object>> getCommandStatistics(
            @Parameter(description = "Device ID") @RequestParam(required = false) Long deviceId,
            @Parameter(description = "Time range") @RequestParam(defaultValue = "24h") String timeRange) {
        Object statistics = deviceCommandService.getCommandStatistics(deviceId, timeRange);
        return ResponseEntity.ok(ApiResponse.success(statistics, "Command statistics retrieved successfully"));
    }

    /**
     * Get available command types for device
     * 
     * @param deviceId device ID
     * @return list of available command types
     */
    @GetMapping("/device/{deviceId}/available-types")
    @Operation(summary = "Get available command types", description = "Retrieve available command types for a specific device")
    public ResponseEntity<ApiResponse<List<String>>> getAvailableCommandTypes(
            @Parameter(description = "Device ID") @PathVariable Long deviceId) {
        List<String> commandTypes = deviceCommandService.getAvailableCommandTypes(deviceId);
        return ResponseEntity.ok(ApiResponse.success(commandTypes, "Available command types retrieved successfully"));
    }

    /**
     * Bulk send commands
     * 
     * @param requests list of command requests
     * @return bulk command execution response
     */
    @PostMapping("/bulk")
    @Operation(summary = "Bulk send commands", description = "Send multiple commands to devices")
    public ResponseEntity<ApiResponse<List<CommandExecutionResponse>>> bulkSendCommands(
            @Valid @RequestBody List<DeviceCommandRequest> requests) {
        List<CommandExecutionResponse> responses = deviceCommandService.bulkSendCommands(requests);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(responses, "Bulk commands sent successfully"));
    }
}


