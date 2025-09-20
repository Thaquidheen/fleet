

package com.fleetmanagement.deviceservice.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceCommandRequest {

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotBlank(message = "Command type is required")
    private String commandType;

    private String commandParameters;

    @NotNull(message = "Initiated by user ID is required")
    private UUID initiatedBy;

    @Min(value = 1, message = "Priority must be between 1 and 5")
    @Max(value = 5, message = "Priority must be between 1 and 5")
    @Builder.Default
    private Integer priority = 3;

    @Min(value = 0, message = "Max retries must be non-negative")
    @Builder.Default
    private Integer maxRetries = 3;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;
}