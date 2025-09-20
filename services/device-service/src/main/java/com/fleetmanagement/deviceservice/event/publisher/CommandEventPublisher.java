package com.fleetmanagement.deviceservice.event.publisher;

import com.fleetmanagement.deviceservice.domain.entity.DeviceCommand;
import com.fleetmanagement.deviceservice.domain.entity.CommandExecutionStep;
import com.fleetmanagement.deviceservice.domain.enums.CommandStatus;
import com.fleetmanagement.deviceservice.event.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Command Event Publisher
 * Publishes command-related events to Kafka topics
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CommandEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publish command created event
     */
    public void publishCommandCreated(DeviceCommand command) {
        try {
            DeviceCommandCreatedEvent event = DeviceCommandCreatedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .commandId(command.getId())
                    .deviceId(command.getDevice().getDeviceId())
                    .companyId(command.getCompanyId())
                    .commandType(command.getCommandType())
                    .initiatedBy(command.getInitiatedBy())
                    .priority(command.getPriority())
                    .expiresAt(command.getExpiresAt())
                    .build();

            kafkaTemplate.send("device.command.created", command.getId().toString(), event);
            log.debug("Published command created event for command: {}", command.getId());

        } catch (Exception e) {
            log.error("Failed to publish command created event for command: {}", command.getId(), e);
        }
    }

    /**
     * Publish command sent event
     */
    public void publishCommandSent(DeviceCommand command) {
        try {
            DeviceCommandSentEvent event = DeviceCommandSentEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .commandId(command.getId())
                    .deviceId(command.getDevice().getDeviceId())
                    .companyId(command.getCompanyId())
                    .commandType(command.getCommandType())
                    .initiatedBy(command.getInitiatedBy())
                    .sentAt(command.getSentAt())
                    .build();

            kafkaTemplate.send("device.command.sent", command.getId().toString(), event);
            log.debug("Published command sent event for command: {}", command.getId());

        } catch (Exception e) {
            log.error("Failed to publish command sent event for command: {}", command.getId(), e);
        }
    }

    /**
     * Publish command acknowledged event
     */
    public void publishCommandAcknowledged(DeviceCommand command, String deviceResponse) {
        try {
            DeviceCommandAcknowledgedEvent event = DeviceCommandAcknowledgedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .commandId(command.getId())
                    .deviceId(command.getDevice().getDeviceId())
                    .companyId(command.getCompanyId())
                    .commandType(command.getCommandType())
                    .acknowledgedAt(command.getAcknowledgedAt())
                    .deviceResponse(deviceResponse)
                    .build();

            kafkaTemplate.send("device.command.acknowledged", command.getId().toString(), event);
            log.debug("Published command acknowledged event for command: {}", command.getId());

        } catch (Exception e) {
            log.error("Failed to publish command acknowledged event for command: {}", command.getId(), e);
        }
    }

    /**
     * Publish command executed event
     */
    public void publishCommandExecuted(DeviceCommand command, String executionResult) {
        try {
            DeviceCommandExecutedEvent event = DeviceCommandExecutedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .commandId(command.getId())
                    .deviceId(command.getDevice().getDeviceId())
                    .companyId(command.getCompanyId())
                    .commandType(command.getCommandType())
                    .executedAt(command.getExecutedAt())
                    .executionResult(executionResult)
                    .successful(true)
                    .build();

            kafkaTemplate.send("device.command.executed", command.getId().toString(), event);
            log.debug("Published command executed event for command: {}", command.getId());

        } catch (Exception e) {
            log.error("Failed to publish command executed event for command: {}", command.getId(), e);
        }
    }

    /**
     * Publish command failed event
     */
    public void publishCommandFailed(DeviceCommand command, String errorMessage, String errorCode) {
        try {
            DeviceCommandFailedEvent event = DeviceCommandFailedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .commandId(command.getId())
                    .deviceId(command.getDevice().getDeviceId())
                    .companyId(command.getCompanyId())
                    .commandType(command.getCommandType())
                    .errorMessage(errorMessage)
                    .errorCode(errorCode)
                    .retryCount(command.getRetryCount())
                    .canRetry(command.canRetry())
                    .build();

            kafkaTemplate.send("device.command.failed", command.getId().toString(), event);
            log.debug("Published command failed event for command: {}", command.getId());

        } catch (Exception e) {
            log.error("Failed to publish command failed event for command: {}", command.getId(), e);
        }
    }

    /**
     * Publish command timeout event
     */
    public void publishCommandTimeout(DeviceCommand command) {
        try {
            DeviceCommandTimeoutEvent event = DeviceCommandTimeoutEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .commandId(command.getId())
                    .deviceId(command.getDevice().getDeviceId())
                    .companyId(command.getCompanyId())
                    .commandType(command.getCommandType())
                    .expiresAt(command.getExpiresAt())
                    .retryCount(command.getRetryCount())
                    .canRetry(command.canRetry())
                    .build();

            kafkaTemplate.send("device.command.timeout", command.getId().toString(), event);
            log.debug("Published command timeout event for command: {}", command.getId());

        } catch (Exception e) {
            log.error("Failed to publish command timeout event for command: {}", command.getId(), e);
        }
    }

    /**
     * Publish command cancelled event
     */
    public void publishCommandCancelled(DeviceCommand command, UUID cancelledBy, String reason) {
        try {
            DeviceCommandCancelledEvent event = DeviceCommandCancelledEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .commandId(command.getId())
                    .deviceId(command.getDevice().getDeviceId())
                    .companyId(command.getCompanyId())
                    .commandType(command.getCommandType())
                    .cancelledBy(cancelledBy)
                    .reason(reason)
                    .build();

            kafkaTemplate.send("device.command.cancelled", command.getId().toString(), event);
            log.debug("Published command cancelled event for command: {}", command.getId());

        } catch (Exception e) {
            log.error("Failed to publish command cancelled event for command: {}", command.getId(), e);
        }
    }

    /**
     * Publish command retry event
     */
    public void publishCommandRetry(DeviceCommand command, String retryReason) {
        try {
            DeviceCommandRetryEvent event = DeviceCommandRetryEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .commandId(command.getId())
                    .deviceId(command.getDevice().getDeviceId())
                    .companyId(command.getCompanyId())
                    .commandType(command.getCommandType())
                    .retryCount(command.getRetryCount())
                    .maxRetries(command.getMaxRetries())
                    .retryReason(retryReason)
                    .build();

            kafkaTemplate.send("device.command.retry", command.getId().toString(), event);
            log.debug("Published command retry event for command: {} (attempt {})", command.getId(), command.getRetryCount());

        } catch (Exception e) {
            log.error("Failed to publish command retry event for command: {}", command.getId(), e);
        }
    }

    /**
     * Publish command status changed event
     */
    public void publishCommandStatusChanged(DeviceCommand command, CommandStatus previousStatus, CommandStatus newStatus) {
        try {
            DeviceCommandStatusChangedEvent event = DeviceCommandStatusChangedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .commandId(command.getId())
                    .deviceId(command.getDevice().getDeviceId())
                    .companyId(command.getCompanyId())
                    .commandType(command.getCommandType())
                    .previousStatus(previousStatus.name())
                    .newStatus(newStatus.name())
                    .build();

            kafkaTemplate.send("device.command.status.changed", command.getId().toString(), event);
            log.debug("Published command status changed event for command: {} from {} to {}",
                    command.getId(), previousStatus, newStatus);

        } catch (Exception e) {
            log.error("Failed to publish command status changed event for command: {}", command.getId(), e);
        }
    }

    /**
     * Publish command step started event
     */
    public void publishCommandStepStarted(CommandExecutionStep step) {
        try {
            CommandStepStartedEvent event = CommandStepStartedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .commandId(step.getCommand().getId())
                    .stepId(step.getId())
                    .deviceId(step.getCommand().getDevice().getDeviceId())
                    .companyId(step.getCommand().getCompanyId())
                    .stepName(step.getStepName())
                    .stepOrder(step.getStepOrder())
                    .startedAt(step.getStartedAt())
                    .build();

            kafkaTemplate.send("device.command.step.started", step.getId().toString(), event);
            log.debug("Published command step started event for step: {} of command: {}",
                    step.getStepName(), step.getCommand().getId());

        } catch (Exception e) {
            log.error("Failed to publish command step started event for step: {}", step.getId(), e);
        }
    }

    /**
     * Publish command step completed event
     */
    public void publishCommandStepCompleted(CommandExecutionStep step, String result) {
        try {
            CommandStepCompletedEvent event = CommandStepCompletedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .commandId(step.getCommand().getId())
                    .stepId(step.getId())
                    .deviceId(step.getCommand().getDevice().getDeviceId())
                    .companyId(step.getCommand().getCompanyId())
                    .stepName(step.getStepName())
                    .stepOrder(step.getStepOrder())
                    .completedAt(step.getCompletedAt())
                    .result(result)
                    .successful(true)
                    .durationMs(step.getDurationMs())
                    .build();

            kafkaTemplate.send("device.command.step.completed", step.getId().toString(), event);
            log.debug("Published command step completed event for step: {} of command: {}",
                    step.getStepName(), step.getCommand().getId());

        } catch (Exception e) {
            log.error("Failed to publish command step completed event for step: {}", step.getId(), e);
        }
    }

    /**
     * Publish command step failed event
     */
    public void publishCommandStepFailed(CommandExecutionStep step, String errorMessage, String errorCode) {
        try {
            CommandStepFailedEvent event = CommandStepFailedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .commandId(step.getCommand().getId())
                    .stepId(step.getId())
                    .deviceId(step.getCommand().getDevice().getDeviceId())
                    .companyId(step.getCommand().getCompanyId())
                    .stepName(step.getStepName())
                    .stepOrder(step.getStepOrder())
                    .errorMessage(errorMessage)
                    .errorCode(errorCode)
                    .retryCount(step.getRetryCount())
                    .canRetry(step.canRetry())
                    .build();

            kafkaTemplate.send("device.command.step.failed", step.getId().toString(), event);
            log.debug("Published command step failed event for step: {} of command: {}",
                    step.getStepName(), step.getCommand().getId());

        } catch (Exception e) {
            log.error("Failed to publish command step failed event for step: {}", step.getId(), e);
        }
    }

    /**
     * Publish command progress update event
     */
    public void publishCommandProgressUpdate(DeviceCommand command, Integer overallProgress,
                                             String currentStepName, Integer stepProgress) {
        try {
            CommandProgressUpdateEvent event = CommandProgressUpdateEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .commandId(command.getId())
                    .deviceId(command.getDevice().getDeviceId())
                    .companyId(command.getCompanyId())
                    .commandType(command.getCommandType())
                    .overallProgress(overallProgress)
                    .currentStepName(currentStepName)
                    .stepProgress(stepProgress)
                    .build();

            kafkaTemplate.send("device.command.progress", command.getId().toString(), event);
            log.debug("Published command progress update event for command: {} - {}% complete",
                    command.getId(), overallProgress);

        } catch (Exception e) {
            log.error("Failed to publish command progress update event for command: {}", command.getId(), e);
        }
    }

    /**
     * Publish bulk command operation event
     */
    public void publishBulkCommandOperation(String operationType, UUID companyId, UUID initiatedBy,
                                            int totalCommands, int successfulCommands, int failedCommands,
                                            Map<String, Object> metadata) {
        try {
            BulkCommandOperationEvent event = BulkCommandOperationEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .operationType(operationType)
                    .companyId(companyId)
                    .initiatedBy(initiatedBy)
                    .totalCommands(totalCommands)
                    .successfulCommands(successfulCommands)
                    .failedCommands(failedCommands)
                    .metadata(metadata)
                    .build();

            kafkaTemplate.send("device.command.bulk.operation", companyId.toString(), event);
            log.debug("Published bulk command operation event: {} - {}/{} successful",
                    operationType, successfulCommands, totalCommands);

        } catch (Exception e) {
            log.error("Failed to publish bulk command operation event for operation: {}", operationType, e);
        }
    }

    /**
     * Publish emergency command event
     */
    public void publishEmergencyCommand(DeviceCommand command, String emergencyType, String urgencyLevel) {
        try {
            EmergencyCommandEvent event = EmergencyCommandEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .commandId(command.getId())
                    .deviceId(command.getDevice().getDeviceId())
                    .companyId(command.getCompanyId())
                    .commandType(command.getCommandType())
                    .emergencyType(emergencyType)
                    .urgencyLevel(urgencyLevel)
                    .initiatedBy(command.getInitiatedBy())
                    .build();

            kafkaTemplate.send("device.command.emergency", command.getId().toString(), event);
            log.warn("Published emergency command event for command: {} - Type: {}, Urgency: {}",
                    command.getId(), emergencyType, urgencyLevel);

        } catch (Exception e) {
            log.error("Failed to publish emergency command event for command: {}", command.getId(), e);
        }
    }

    /**
     * Publish command batch event
     */
    public void publishCommandBatch(String batchId, UUID companyId, UUID initiatedBy,
                                    int commandCount, String batchType) {
        try {
            CommandBatchEvent event = CommandBatchEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .batchId(batchId)
                    .companyId(companyId)
                    .initiatedBy(initiatedBy)
                    .commandCount(commandCount)
                    .batchType(batchType)
                    .build();

            kafkaTemplate.send("device.command.batch", batchId, event);
            log.debug("Published command batch event: {} - {} commands of type {}",
                    batchId, commandCount, batchType);

        } catch (Exception e) {
            log.error("Failed to publish command batch event for batch: {}", batchId, e);
        }
    }
}