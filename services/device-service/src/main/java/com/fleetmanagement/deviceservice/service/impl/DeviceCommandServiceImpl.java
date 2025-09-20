package com.fleetmanagement.deviceservice.service.impl;

import com.fleetmanagement.deviceservice.domain.entity.Device;
import com.fleetmanagement.deviceservice.domain.entity.DeviceCommand;
import com.fleetmanagement.deviceservice.domain.enums.CommandStatus;
import com.fleetmanagement.deviceservice.dto.request.DeviceCommandRequest;
import com.fleetmanagement.deviceservice.dto.response.CommandExecutionResponse;
import com.fleetmanagement.deviceservice.exception.DeviceNotFoundException;
import com.fleetmanagement.deviceservice.external.client.TraccarApiClient;
import com.fleetmanagement.deviceservice.repository.DeviceRepository;
import com.fleetmanagement.deviceservice.repository.DeviceCommandRepository;
import com.fleetmanagement.deviceservice.service.DeviceCommandService;
import com.fleetmanagement.deviceservice.event.publisher.CommandEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class DeviceCommandServiceImpl implements DeviceCommandService {

    private final DeviceRepository deviceRepository;
    private final DeviceCommandRepository commandRepository;
    private final TraccarApiClient traccarApiClient;
    private final CommandEventPublisher eventPublisher;

    @Override
    public CommandExecutionResponse sendCommand(UUID deviceId, DeviceCommandRequest request) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found: " + deviceId));

        if (device.getTraccarId() == null) {
            throw new RuntimeException("Device not linked to Traccar: " + deviceId);
        }

        // Create command record
        DeviceCommand command = DeviceCommand.builder().
                .deviceId(deviceId)
                .traccarDeviceId(device.getTraccarId())
                .commandType(request.getCommandType())
                .parameters(request.getParameters())
                .status(CommandStatus.PENDING)
                .requestedBy(request.getRequestedBy())
                .description(request.getDescription())
                .sentAt(LocalDateTime.now())
                .build();

        command = commandRepository.save(command);

        try {
            // Send command to Traccar
            traccarApiClient.sendCommand(device.getTraccarId(), request.getCommandType());

            // Update command status
            command.setStatus(CommandStatus.SENT);
            command.setSentAt(LocalDateTime.now());
            command = commandRepository.save(command);

            // Publish command sent event
            eventPublisher.publishCommandSent(command);

            log.info("Command {} sent to device {}", request.getCommandType(), deviceId);

        } catch (Exception e) {
            log.error("Error sending command to device: {}", deviceId, e);

            command.setStatus(CommandStatus.FAILED);
            command.setErrorMessage(e.getMessage());
            command = commandRepository.save(command);

            throw new RuntimeException("Failed to send command", e);
        }

        return mapToCommandResponse(command);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommandExecutionResponse> getDeviceCommands(UUID deviceId, Pageable pageable) {
        Page<DeviceCommand> commands = commandRepository.findByDeviceId(deviceId, pageable);
        return commands.map(this::mapToCommandResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CommandExecutionResponse getCommandStatus(UUID commandId) {
        DeviceCommand command = commandRepository.findById(commandId)
                .orElseThrow(() -> new RuntimeException("Command not found: " + commandId));
        return mapToCommandResponse(command);
    }

    private CommandExecutionResponse mapToCommandResponse(DeviceCommand command) {
        return CommandExecutionResponse.builder()
                .commandId(command.getId())
                .deviceId(command.getDeviceId())
                .commandType(command.getCommandType())
                .status(command.getStatus())
                .result(command.getResult())
                .errorMessage(command.getErrorMessage())
                .sentAt(command.getSentAt())
                .completedAt(command.getCompletedAt())
                .requestedBy(command.getRequestedBy())
                .build();
    }
}