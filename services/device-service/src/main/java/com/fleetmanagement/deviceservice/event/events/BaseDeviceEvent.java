package com.fleetmanagement.deviceservice.event.events;

import com.fleetmanagement.deviceservice.domain.enums.DeviceBrand;
import com.fleetmanagement.deviceservice.domain.enums.DeviceStatus;
import com.fleetmanagement.deviceservice.domain.enums.DeviceType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base Device Event
 */
@Data
@Builder
public abstract class BaseDeviceEvent {
    private String eventId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private String deviceId;
}