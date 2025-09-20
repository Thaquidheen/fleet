package com.fleetmanagement.deviceservice.domain.entity;

import com.fleetmanagement.deviceservice.domain.enums.CommandStatus;
import com.fleetmanagement.deviceservice.domain.enums.HealthLevel;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Device Command Entity
 * Represents commands sent to devices
 */
@Entity
@Table(name = "device_commands", indexes = {
        @Index(name = "idx_device_command_device", columnList = "device_id"),
        @Index(name = "idx_device_command_status", columnList = "status"),
        @Index(name = "idx_device_command_type", columnList = "command_type"),
        @Index(name = "idx_device_command_created", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class DeviceCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Device this command is for
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    /**
     * Command type (e.g., "position", "restart", "configure")
     */
    @Column(name = "command_type", nullable = false, length = 50)
    private String commandType;

    /**
     * Command parameters as JSON
     */
    @Column(name = "command_parameters", columnDefinition = "TEXT")
    private String commandParameters;

    /**
     * Current command status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private CommandStatus status = CommandStatus.PENDING;

    /**
     * User who initiated the command
     */
    @Column(name = "initiated_by", nullable = false)
    private UUID initiatedBy;

    /**
     * Company that owns the device
     */
    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    /**
     * When the command was sent to device
     */
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    /**
     * When the device acknowledged the command
     */
    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    /**
     * When the command was executed
     */
    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    /**
     * Command timeout (when it expires)
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * Device response to the command
     */
    @Column(name = "device_response", columnDefinition = "TEXT")
    private String deviceResponse;

    /**
     * Error message if command failed
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Number of retry attempts
     */
    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    /**
     * Maximum retry attempts allowed
     */
    @Column(name = "max_retries", nullable = false)
    @Builder.Default
    private Integer maxRetries = 3;

    /**
     * Priority level (1 = highest, 5 = lowest)
     */
    @Column(name = "priority", nullable = false)
    @Builder.Default
    private Integer priority = 3;

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
     * Check if command is pending execution
     */
    public boolean isPending() {
        return status == CommandStatus.PENDING;
    }

    /**
     * Check if command has expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if command can be retried
     */
    public boolean canRetry() {
        return retryCount < maxRetries && !isExpired();
    }

    /**
     * Mark command as sent
     */
    public void markAsSent() {
        this.status = CommandStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    /**
     * Mark command as acknowledged by device
     */
    public void markAsAcknowledged(String deviceResponse) {
        this.status = CommandStatus.ACKNOWLEDGED;
        this.acknowledgedAt = LocalDateTime.now();
        this.deviceResponse = deviceResponse;
    }

    /**
     * Mark command as successfully executed
     */
    public void markAsExecuted(String deviceResponse) {
        this.status = CommandStatus.EXECUTED;
        this.executedAt = LocalDateTime.now();
        this.deviceResponse = deviceResponse;
    }

    /**
     * Mark command as failed
     */
    public void markAsFailed(String errorMessage) {
        this.status = CommandStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    /**
     * Mark command as timed out
     */
    public void markAsTimeout() {
        this.status = CommandStatus.TIMEOUT;
        this.errorMessage = "Command timed out";
    }

    /**
     * Increment retry count
     */
    public void incrementRetryCount() {
        this.retryCount++;
        this.status = CommandStatus.PENDING;
    }
}
