package com.fleetmanagement.deviceservice.event.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Device Command Timeout Event
 */
@Data
@Builder
public class DeviceCommandTimeoutEvent {
    private String eventId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private UUID commandId;
    private String deviceId;
    private UUID companyId;
    private String commandType;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;
    private Integer retryCount;
    private Boolean canRetry;
}