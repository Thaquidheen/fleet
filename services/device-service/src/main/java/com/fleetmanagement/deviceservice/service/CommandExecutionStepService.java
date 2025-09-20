package com.fleetmanagement.deviceservice.service;

import com.fleetmanagement.deviceservice.domain.entity.CommandExecutionStep;
import com.fleetmanagement.deviceservice.domain.entity.DeviceCommand;
import com.fleetmanagement.deviceservice.domain.enums.CommandStatus;
import com.fleetmanagement.deviceservice.dto.response.DeviceCommandResponse;
import com.fleetmanagement.deviceservice.repository.CommandExecutionStepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Command Execution Step Service
 * Manages step-by-step command execution tracking
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommandExecutionStepService {

    private final CommandExecutionStepRepository stepRepository;

    public List<CommandExecutionStep> initializeStandardSteps(DeviceCommand command) {
        log.debug("Initializing standard execution steps for command: {}", command.getId());

        List<CommandExecutionStep> steps = List.of(
                CommandExecutionStep.createValidationStep(command, 1),
                CommandExecutionStep.createDeviceStatusCheckStep(command, 2),
                CommandExecutionStep.createSendCommandStep(command, 3),
                CommandExecutionStep.createWaitAcknowledgmentStep(command, 4),
                CommandExecutionStep.createWaitExecutionStep(command, 5)
        );

        return stepRepository.saveAll(steps);
    }

    /**
     * Initialize custom steps for a command
     */
    public List<CommandExecutionStep> initializeCustomSteps(DeviceCommand command, List<CommandExecutionStep> customSteps) {
        log.debug("Initializing {} custom execution steps for command: {}", customSteps.size(), command.getId());

        // Set command reference and order
        for (int i = 0; i < customSteps.size(); i++) {
            CommandExecutionStep step = customSteps.get(i);
            step.setCommand(command);
            step.setStepOrder(i + 1);
        }

        return stepRepository.saveAll(customSteps);
    }

    /**
     * Get all steps for a command
     */
    public List<CommandExecutionStep> getCommandSteps(DeviceCommand command) {
        return stepRepository.findByCommandOrderByStepOrder(command);
    }

    /**
     * Get all steps for a command by ID
     */
    public List<CommandExecutionStep> getCommandSteps(UUID commandId) {
        return stepRepository.findByCommandIdOrderByStepOrder(commandId);
    }

    /**
     * Start the next pending step
     */
    public Optional<CommandExecutionStep> startNextStep(DeviceCommand command) {
        log.debug("Starting next step for command: {}", command.getId());

        Optional<CommandExecutionStep> nextStep = stepRepository.findNextPendingStep(command);
        if (nextStep.isPresent()) {
            CommandExecutionStep step = nextStep.get();
            step.start();
            stepRepository.save(step);
            log.debug("Started step: {} for command: {}", step.getStepName(), command.getId());
            return Optional.of(step);
        }

        log.debug("No pending steps found for command: {}", command.getId());
        return Optional.empty();
    }

    /**
     * Complete a step successfully
     */
    public CommandExecutionStep completeStep(UUID stepId, String result) {
        log.debug("Completing step: {} with result", stepId);

        CommandExecutionStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new IllegalArgumentException("Step not found: " + stepId));

        step.complete(result);
        CommandExecutionStep savedStep = stepRepository.save(step);

        log.debug("Completed step: {} for command: {}", step.getStepName(), step.getCommand().getId());
        return savedStep;
    }

    /**
     * Fail a step
     */
    public CommandExecutionStep failStep(UUID stepId, String errorMessage, String errorCode) {
        log.debug("Failing step: {} with error: {}", stepId, errorMessage);

        CommandExecutionStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new IllegalArgumentException("Step not found: " + stepId));

        step.fail(errorMessage, errorCode);
        CommandExecutionStep savedStep = stepRepository.save(step);

        log.debug("Failed step: {} for command: {}", step.getStepName(), step.getCommand().getId());
        return savedStep;
    }

    /**
     * Update step progress
     */
    public CommandExecutionStep updateStepProgress(UUID stepId, Integer progressPercentage) {
        log.debug("Updating step: {} progress to: {}%", stepId, progressPercentage);

        CommandExecutionStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new IllegalArgumentException("Step not found: " + stepId));

        step.updateProgress(progressPercentage);
        return stepRepository.save(step);
    }

    /**
     * Get current executing step
     */
    public Optional<CommandExecutionStep> getCurrentExecutingStep(DeviceCommand command) {
        return stepRepository.findCurrentExecutingStep(command);
    }

    /**
     * Calculate overall command progress
     */
    public Integer calculateCommandProgress(DeviceCommand command) {
        Double avgProgress = stepRepository.calculateCommandProgress(command);
        return avgProgress != null ? avgProgress.intValue() : 0;
    }

    /**
     * Get step statistics
     */
    public StepStatistics getStepStatistics(DeviceCommand command) {
        Object[] stats = stepRepository.getStepStatistics(command);
        if (stats != null && stats.length >= 5) {
            return StepStatistics.builder()
                    .totalSteps(((Number) stats[0]).intValue())
                    .completedSteps(((Number) stats[1]).intValue())
                    .failedSteps(((Number) stats[2]).intValue())
                    .executingSteps(((Number) stats[3]).intValue())
                    .pendingSteps(((Number) stats[4]).intValue())
                    .build();
        }
        return StepStatistics.builder().build();
    }

    /**
     * Check if all required steps are completed successfully
     */
    public boolean areAllRequiredStepsCompleted(DeviceCommand command) {
        List<CommandExecutionStep> steps = getCommandSteps(command);
        return steps.stream()
                .filter(CommandExecutionStep::getIsRequired)
                .allMatch(CommandExecutionStep::isSuccessful);
    }

    /**
     * Check if any required step has failed permanently
     */
    public boolean hasRequiredStepFailed(DeviceCommand command) {
        List<CommandExecutionStep> steps = getCommandSteps(command);
        return steps.stream()
                .filter(CommandExecutionStep::getIsRequired)
                .anyMatch(step -> step.getStepStatus() == CommandStatus.FAILED && !step.canRetry());
    }

    /**
     * Get failed steps that can be retried
     */
    public List<CommandExecutionStep> getRetryableFailedSteps(DeviceCommand command) {
        return stepRepository.findRetryableFailedSteps(command);
    }

    /**
     * Retry a failed step
     */
    public CommandExecutionStep retryStep(UUID stepId) {
        log.debug("Retrying step: {}", stepId);

        CommandExecutionStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new IllegalArgumentException("Step not found: " + stepId));

        if (!step.canRetry()) {
            throw new IllegalStateException("Step cannot be retried: " + stepId);
        }

        step.prepareForRetry();
        CommandExecutionStep savedStep = stepRepository.save(step);

        log.debug("Prepared step: {} for retry (attempt {})", step.getStepName(), step.getRetryCount());
        return savedStep;
    }

    /**
     * Convert entity steps to DTO steps
     */
    public DeviceCommandResponse.CommandExecutionStep[] convertToResponseSteps(List<CommandExecutionStep> entitySteps) {
        return entitySteps.stream()
                .map(this::convertToResponseStep)
                .toArray(DeviceCommandResponse.CommandExecutionStep[]::new);
    }

    /**
     * Convert single entity step to DTO step
     */
    public DeviceCommandResponse.CommandExecutionStep convertToResponseStep(CommandExecutionStep entityStep) {
        return DeviceCommandResponse.CommandExecutionStep.builder()
                .stepName(entityStep.getStepName())
                .stepDescription(entityStep.getStepDescription())
                .stepStatus(DeviceCommandResponse.CommandStatus.valueOf(entityStep.getStepStatus().name()))
                .startedAt(entityStep.getStartedAt())
                .completedAt(entityStep.getCompletedAt())
                .result(entityStep.getResult())
                .errorMessage(entityStep.getErrorMessage())
                .order(entityStep.getStepOrder())
                .build();
    }

    /**
     * Find and handle stuck steps (cleanup mechanism)
     */
    public List<CommandExecutionStep> findAndHandleStuckSteps(int timeoutMinutes) {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(timeoutMinutes);
        List<CommandExecutionStep> stuckSteps = stepRepository.findStuckSteps(threshold);

        log.warn("Found {} stuck steps older than {} minutes", stuckSteps.size(), timeoutMinutes);

        for (CommandExecutionStep step : stuckSteps) {
            step.fail("Step timed out - no completion received", "STEP_TIMEOUT");
            stepRepository.save(step);
            log.warn("Marked stuck step as failed: {} for command: {}",
                    step.getStepName(), step.getCommand().getId());
        }

        return stuckSteps;
    }

    /**
     * Cleanup old completed steps
     */
    public int cleanupOldSteps(int daysOld) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(daysOld);
        int deletedCount = stepRepository.deleteOldCompletedSteps(threshold);
        log.info("Cleaned up {} old command execution steps older than {} days", deletedCount, daysOld);
        return deletedCount;
    }

    /**
     * Step Statistics inner class
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class StepStatistics {
        private Integer totalSteps;
        private Integer completedSteps;
        private Integer failedSteps;
        private Integer executingSteps;
        private Integer pendingSteps;

        public Double getCompletionPercentage() {
            if (totalSteps == null || totalSteps == 0) return 0.0;
            return (completedSteps.doubleValue() / totalSteps.doubleValue()) * 100;
        }

        public Boolean isAllCompleted() {
            return totalSteps != null && completedSteps != null && totalSteps.equals(completedSteps);
        }

        public Boolean hasFailures() {
            return failedSteps != null && failedSteps > 0;
        }
    }
}