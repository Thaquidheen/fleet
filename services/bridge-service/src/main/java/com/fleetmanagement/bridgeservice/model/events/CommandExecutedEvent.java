package com.fleetmanagement.bridgeservice.model.events;

import com.fleetmanagement.bridgeservice.model.domain.CommandResult;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class CommandExecutedEvent {

    private String eventId;
    private String eventType;
    private Instant timestamp;

    // Device information
    private UUID deviceId;
    private Long traccarDeviceId;
    private UUID companyId;
    private String deviceName;

    // Command result
    private CommandResult commandResult;

    // Event metadata
    private String source;
    private String version;

    public static CommandExecutedEvent from(CommandResult commandResult) {
        return CommandExecutedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("device.command.executed")
                .timestamp(Instant.now())
                .deviceId(commandResult.getDeviceId())
                .traccarDeviceId(commandResult.getTraccarDeviceId())
                .companyId(commandResult.getCompanyId())
                .commandResult(commandResult)
                .source("bridge-service")
                .version("1.0")
                .build();
    }
}