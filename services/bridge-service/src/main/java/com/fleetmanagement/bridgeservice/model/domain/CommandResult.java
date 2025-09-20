// ===== CommandResult.java =====
package com.fleetmanagement.bridgeservice.model.domain;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class CommandResult {

    private UUID commandId;
    private UUID deviceId;
    private Long traccarDeviceId;
    private UUID companyId;

    // Command details
    private String commandType;
    private String commandData;
    private String description;

    // Execution details
    private String status;
    private String result;
    private String errorMessage;

    // Timestamps
    private Instant sentTime;
    private Instant executedTime;
    private Instant acknowledgedTime;

    // Metadata
    private String protocol;
    private Integer retryCount;
    private Boolean successful;
}