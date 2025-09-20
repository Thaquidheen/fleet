
package com.fleetmanagement.deviceservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * Device Command Response DTO
 * Response containing command execution details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceCommandResponse {

    /**
     * Unique command identifier
     */
    private UUID commandId;

    /**
     * Device ID that received the command
     */
    private String deviceId;

    /**
     * Device name for display
     */
    private String deviceName;

    /**
     * Company ID that owns the device
     */
    private UUID companyId;

    /**
     * Type of command executed
     */
    private String commandType;

    /**
     * Command parameters that were sent
     */
    private Map<String, Object> commandParameters;

    /**
     * Current status of the command
     */
    private CommandStatus status;

    /**
     * User who initiated the command
     */
    private UUID initiatedBy;

    /**
     * Name of user who initiated the command
     */
    private String initiatedByName;

    /**
     * Command priority level
     */
    private Integer priority;

    /**
     * When the command was created
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * When the command was sent to device
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime sentAt;

    /**
     * When the device acknowledged the command
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime acknowledgedAt;

    /**
     * When the command was executed by device
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime executedAt;

    /**
     * When the command expires
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;

    /**
     * Last status update timestamp
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * Response from the device
     */
    private String deviceResponse;

    /**
     * Parsed device response data
     */
    private Map<String, Object> deviceResponseData;

    /**
     * Error message if command failed
     */
    private String errorMessage;

    /**
     * Detailed error information
     */
    private String errorDetails;

    /**
     * Error code for programmatic handling
     */
    private String errorCode;

    /**
     * Current retry attempt number
     */
    private Integer retryCount;

    /**
     * Maximum retry attempts allowed
     */
    private Integer maxRetries;

    /**
     * Whether command can be retried
     */
    private Boolean canRetry;

    /**
     * Whether command has expired
     */
    private Boolean isExpired;

    /**
     * Whether command completed successfully
     */
    private Boolean isSuccessful;

    /**
     * Command execution duration in milliseconds
     */
    private Long executionDurationMs;

    /**
     * Time until expiration in seconds
     */
    private Long timeToExpirationSeconds;

    /**
     * Description or notes about the command
     */
    private String description;

    /**
     * Tags associated with the command
     */
    private String[] tags;

    /**
     * Additional metadata
     */
    private Map<String, Object> metadata;

    /**
     * Command execution progress (0-100)
     */
    private Integer progressPercentage;

    /**
     * Substeps or phases of command execution
     */
    private CommandExecutionStep[] executionSteps;

}