package com.fleetmanagement.deviceservice.event.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Bulk Command Operation Event
 */
@Data
@Builder
public class BulkCommandOperationEvent {
    private String eventId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private String operationType;
    private UUID companyId;
    private UUID initiatedBy;
    private Integer totalCommands;
    private Integer successfulCommands;
    private Integer failedCommands;
    private Map<String, Object> metadata;
}