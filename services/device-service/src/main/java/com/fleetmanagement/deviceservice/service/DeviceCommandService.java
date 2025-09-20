package com.fleetmanagement.deviceservice.service;

import com.fleetmanagement.deviceservice.dto.request.DeviceCommandRequest;
import com.fleetmanagement.deviceservice.dto.response.CommandExecutionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface DeviceCommandService {
    CommandExecutionResponse sendCommand(UUID deviceId, DeviceCommandRequest request);
    Page<CommandExecutionResponse> getDeviceCommands(UUID deviceId, Pageable pageable);
    CommandExecutionResponse getCommandStatus(UUID commandId);
}
