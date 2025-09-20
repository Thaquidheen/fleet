package com.fleetmanagement.deviceservice.repository;

import com.fleetmanagement.deviceservice.domain.entity.CommandExecutionStep;
import com.fleetmanagement.deviceservice.domain.entity.DeviceCommand;
import com.fleetmanagement.deviceservice.domain.enums.CommandStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Command Execution Step Repository
 * Data access layer for CommandExecutionStep entities
 */
@Repository
public interface CommandExecutionStepRepository extends JpaRepository<CommandExecutionStep, UUID> {

    /**
     * Find all steps for a command, ordered by step order
     */
    List<CommandExecutionStep> findByCommandOrderByStepOrder(DeviceCommand command);

    /**
     * Find steps by command ID, ordered by step order
     */
    @Query("SELECT ces FROM CommandExecutionStep ces WHERE ces.command.id = :commandId ORDER BY ces.stepOrder")
    List<CommandExecutionStep> findByCommandIdOrderByStepOrder(@Param("commandId") UUID commandId);

    /**
     * Find steps by command and status
     */
    List<CommandExecutionStep> findByCommandAndStepStatus(DeviceCommand command, CommandStatus status);

    /**
     * Find current executing step for a command
     */
    @Query("SELECT ces FROM CommandExecutionStep ces WHERE ces.command = :command AND ces.stepStatus = 'EXECUTING' ORDER BY ces.stepOrder")
    Optional<CommandExecutionStep> findCurrentExecutingStep(@Param("command") DeviceCommand command);

    /**
     * Find next pending step for a command
     */
    @Query("SELECT ces FROM CommandExecutionStep ces WHERE ces.command = :command AND ces.stepStatus = 'PENDING' ORDER BY ces.stepOrder LIMIT 1")
    Optional<CommandExecutionStep> findNextPendingStep(@Param("command") DeviceCommand command);

    /**
     * Find failed steps that can be retried
     */
    @Query("SELECT ces FROM CommandExecutionStep ces WHERE ces.command = :command AND ces.stepStatus = 'FAILED' AND ces.isRetryable = true AND ces.retryCount < ces.maxRetries ORDER BY ces.stepOrder")
    List<CommandExecutionStep> findRetryableFailedSteps(@Param("command") DeviceCommand command);

    /**
     * Count completed steps for a command
     */
    @Query("SELECT COUNT(ces) FROM CommandExecutionStep ces WHERE ces.command = :command AND ces.stepStatus IN ('EXECUTED', 'FAILED')")
    Long countCompletedSteps(@Param("command") DeviceCommand command);

    /**
     * Count total steps for a command
     */
    Long countByCommand(DeviceCommand command);

    /**
     * Find steps by step name
     */
    List<CommandExecutionStep> findByStepName(String stepName);

    /**
     * Find steps by step name and status
     */
    List<CommandExecutionStep> findByStepNameAndStepStatus(String stepName, CommandStatus status);

    /**
     * Find long-running steps (executing for more than specified duration)
     */
    @Query("SELECT ces FROM CommandExecutionStep ces WHERE ces.stepStatus = 'EXECUTING' AND ces.startedAt < :threshold")
    List<CommandExecutionStep> findLongRunningSteps(@Param("threshold") LocalDateTime threshold);

    /**
     * Find steps that started but never completed
     */
    @Query("SELECT ces FROM CommandExecutionStep ces WHERE ces.stepStatus = 'EXECUTING' AND ces.startedAt IS NOT NULL AND ces.completedAt IS NULL AND ces.startedAt < :threshold")
    List<CommandExecutionStep> findStuckSteps(@Param("threshold") LocalDateTime threshold);

    /**
     * Get step statistics for a command
     */
    @Query("SELECT " +
            "COUNT(ces) as totalSteps, " +
            "SUM(CASE WHEN ces.stepStatus = 'EXECUTED' THEN 1 ELSE 0 END) as completedSteps, " +
            "SUM(CASE WHEN ces.stepStatus = 'FAILED' THEN 1 ELSE 0 END) as failedSteps, " +
            "SUM(CASE WHEN ces.stepStatus = 'EXECUTING' THEN 1 ELSE 0 END) as executingSteps, " +
            "SUM(CASE WHEN ces.stepStatus = 'PENDING' THEN 1 ELSE 0 END) as pendingSteps " +
            "FROM CommandExecutionStep ces WHERE ces.command = :command")
    Object[] getStepStatistics(@Param("command") DeviceCommand command);

    /**
     * Calculate overall progress for a command
     */
    @Query("SELECT AVG(ces.progressPercentage) FROM CommandExecutionStep ces WHERE ces.command = :command")
    Double calculateCommandProgress(@Param("command") DeviceCommand command);

    /**
     * Find steps with specific error code
     */
    List<CommandExecutionStep> findByErrorCode(String errorCode);

    /**
     * Find steps that took longer than expected
     */
    @Query("SELECT ces FROM CommandExecutionStep ces WHERE ces.durationMs > :maxDurationMs AND ces.stepStatus = 'EXECUTED'")
    List<CommandExecutionStep> findSlowSteps(@Param("maxDurationMs") Long maxDurationMs);

    /**
     * Delete old completed steps
     */
    @Query("DELETE FROM CommandExecutionStep ces WHERE ces.stepStatus IN ('EXECUTED', 'FAILED') AND ces.completedAt < :threshold")
    int deleteOldCompletedSteps(@Param("threshold") LocalDateTime threshold);

    /**
     * Find steps by command device ID
     */
    @Query("SELECT ces FROM CommandExecutionStep ces WHERE ces.command.device.deviceId = :deviceId ORDER BY ces.command.createdAt DESC, ces.stepOrder")
    List<CommandExecutionStep> findByDeviceId(@Param("deviceId") String deviceId);

    /**
     * Find recent failed steps for analysis
     */
    @Query("SELECT ces FROM CommandExecutionStep ces WHERE ces.stepStatus = 'FAILED' AND ces.completedAt > :since ORDER BY ces.completedAt DESC")
    List<CommandExecutionStep> findRecentFailedSteps(@Param("since") LocalDateTime since);

    /**
     * Find steps that need attention (failed required steps)
     */
    @Query("SELECT ces FROM CommandExecutionStep ces WHERE ces.stepStatus = 'FAILED' AND ces.isRequired = true AND ces.retryCount >= ces.maxRetries")
    List<CommandExecutionStep> findStepsNeedingAttention();
}