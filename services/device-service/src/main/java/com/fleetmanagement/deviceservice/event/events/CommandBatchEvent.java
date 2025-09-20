package com.fleetmanagement.deviceservice.event.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Command Batch Event
 */
@Data
@Builder
public class CommandBatchEvent {
    private String eventId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private String batchId;
    private UUID companyId;
    private UUID initiatedBy;
    private Integer commandCount;
    private String batchType;
}
