package com.fleetmanagement.deviceservice.domain.entity;

import com.fleetmanagement.deviceservice.domain.enums.CommandStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Command Execution Step Entity
 * Tracks individual steps in command execution process
 */
@Entity
@Table(name = "command_execution_steps", indexes = {
        @Index(name = "idx_command_execution_step_command", columnList = "command_id"),
        @Index(name = "idx_command_execution_step_order", columnList = "command_id, step_order"),
        @Index(name = "idx_command_execution_step_status", columnList = "step_status")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class CommandExecutionStep {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Device command this step belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "command_id", nullable = false)
    private DeviceCommand command;

    /**
     * Step name/identifier
     */
    @Column(name = "step_name", nullable = false, length = 100)
    private String stepName;

    /**
     * Human-readable step description
     */
    @Column(name = "step_description", length = 255)
    private String stepDescription;

    /**
     * Current status of this step
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "step_status", nullable = false)
    @Builder.Default
    private CommandStatus stepStatus = CommandStatus.PENDING;

    /**
     * Order of execution (1, 2, 3, etc.)
     */
    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    /**
     * When this step was started
     */
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    /**
     * When this step was completed
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * Step execution result
     */
    @Column(name = "result", columnDefinition = "TEXT")
    private String result;

    /**
     * Error message if step failed
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Error code for programmatic handling
     */
    @Column(name = "error_code", length = 50)
    private String errorCode;

    /**
     * Duration of step execution in milliseconds
     */
    @Column(name = "duration_ms")
    private Long durationMs;

    /**
     * Progress percentage for this step (0-100)
     */
    @Column(name = "progress_percentage")
    @Builder.Default
    private Integer progressPercentage = 0;

    /**
     * Whether this step is required for command success
     */
    @Column(name = "is_required", nullable = false)
    @Builder.Default
    private Boolean isRequired = true;

    /**
     * Whether this step can be retried
     */
    @Column(name = "is_retryable", nullable = false)
    @Builder.Default
    private Boolean isRetryable = true;

    /**
     * Number of retry attempts for this step
     */
    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    /**
     * Maximum retry attempts for this step
     */
    @Column(name = "max_retries", nullable = false)
    @Builder.Default
    private Integer maxRetries = 3;

    /**
     * Additional step metadata as JSON
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    /**
     * Audit fields
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Business methods

    /**
     * Start this step
     */
    public void start() {
        this.stepStatus = CommandStatus.EXECUTING;
        this.startedAt = LocalDateTime.now();
        this.progressPercentage = 0;
    }

    /**
     * Complete this step successfully
     */
    public void complete(String result) {
        this.stepStatus = CommandStatus.EXECUTED;
        this.completedAt = LocalDateTime.now();
        this.result = result;
        this.progressPercentage = 100;
        this.errorMessage = null;
        this.errorCode = null;

        if (startedAt != null) {
            this.durationMs = java.time.Duration.between(startedAt, completedAt).toMillis();
        }
    }

    /**
     * Fail this step
     */
    public void fail(String errorMessage, String errorCode) {
        this.stepStatus = CommandStatus.FAILED;
        this.completedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;

        if (startedAt != null) {
            this.durationMs = java.time.Duration.between(startedAt, completedAt).toMillis();
        }
    }

    /**
     * Update progress percentage
     */
    public void updateProgress(Integer progressPercentage) {
        if (progressPercentage >= 0 && progressPercentage <= 100) {
            this.progressPercentage = progressPercentage;
        }
    }

    /**
     * Check if step is completed (successfully or not)
     */
    public boolean isCompleted() {
        return stepStatus != null && (stepStatus == CommandStatus.EXECUTED || stepStatus == CommandStatus.FAILED);
    }

    /**
     * Check if step was successful
     */
    public boolean isSuccessful() {
        return stepStatus == CommandStatus.EXECUTED;
    }

    /**
     * Check if step can be retried
     */
    public boolean canRetry() {
        return isRetryable && retryCount < maxRetries && stepStatus == CommandStatus.FAILED;
    }

    /**
     * Increment retry count and reset for retry
     */
    public void prepareForRetry() {
        if (canRetry()) {
            this.retryCount++;
            this.stepStatus = CommandStatus.PENDING;
            this.startedAt = null;
            this.completedAt = null;
            this.progressPercentage = 0;
            this.durationMs = null;
            // Keep error messages for debugging
        }
    }

    /**
     * Get step duration
     */
    public java.time.Duration getDuration() {
        if (durationMs != null) {
            return java.time.Duration.ofMillis(durationMs);
        }
        if (startedAt != null && completedAt != null) {
            return java.time.Duration.between(startedAt, completedAt);
        }
        return null;
    }

    // Common step types as constants
    public static final String STEP_VALIDATE_COMMAND = "validate_command";
    public static final String STEP_CHECK_DEVICE_STATUS = "check_device_status";
    public static final String STEP_PREPARE_COMMAND = "prepare_command";
    public static final String STEP_SEND_TO_DEVICE = "send_to_device";
    public static final String STEP_WAIT_ACKNOWLEDGMENT = "wait_acknowledgment";
    public static final String STEP_WAIT_EXECUTION = "wait_execution";
    public static final String STEP_PROCESS_RESPONSE = "process_response";
    public static final String STEP_UPDATE_DEVICE_STATE = "update_device_state";
    public static final String STEP_NOTIFY_COMPLETION = "notify_completion";

    // Factory methods for common steps
    public static CommandExecutionStep createValidationStep(DeviceCommand command, Integer order) {
        return CommandExecutionStep.builder()
                .command(command)
                .stepName(STEP_VALIDATE_COMMAND)
                .stepDescription("Validate command parameters and permissions")
                .stepOrder(order)
                .isRequired(true)
                .isRetryable(false)
                .maxRetries(0)
                .build();
    }

    public static CommandExecutionStep createDeviceStatusCheckStep(DeviceCommand command, Integer order) {
        return CommandExecutionStep.builder()
                .command(command)
                .stepName(STEP_CHECK_DEVICE_STATUS)
                .stepDescription("Check if device is online and ready")
                .stepOrder(order)
                .isRequired(true)
                .isRetryable(true)
                .maxRetries(2)
                .build();
    }

    public static CommandExecutionStep createSendCommandStep(DeviceCommand command, Integer order) {
        return CommandExecutionStep.builder()
                .command(command)
                .stepName(STEP_SEND_TO_DEVICE)
                .stepDescription("Send command to device")
                .stepOrder(order)
                .isRequired(true)
                .isRetryable(true)
                .maxRetries(3)
                .build();
    }

    public static CommandExecutionStep createWaitAcknowledgmentStep(DeviceCommand command, Integer order) {
        return CommandExecutionStep.builder()
                .command(command)
                .stepName(STEP_WAIT_ACKNOWLEDGMENT)
                .stepDescription("Wait for device acknowledgment")
                .stepOrder(order)
                .isRequired(true)
                .isRetryable(false)
                .maxRetries(0)
                .build();
    }

    public static CommandExecutionStep createWaitExecutionStep(DeviceCommand command, Integer order) {
        return CommandExecutionStep.builder()
                .command(command)
                .stepName(STEP_WAIT_EXECUTION)
                .stepDescription("Wait for command execution")
                .stepOrder(order)
                .isRequired(true)
                .isRetryable(false)
                .maxRetries(0)
                .build();
    }
}