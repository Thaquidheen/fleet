

package com.fleetmanagement.deviceservice.repository;

import com.fleetmanagement.deviceservice.domain.entity.*;
import com.fleetmanagement.deviceservice.domain.enums.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


/**
 * Device Command Repository
 */
@Repository
public interface DeviceCommandRepository extends JpaRepository<DeviceCommand, UUID> {

    /**
     * Find commands by device
     */
    Page<DeviceCommand> findByDevice(Device device, Pageable pageable);

    /**
     * Find commands by status
     */
    List<DeviceCommand> findByStatus(CommandStatus status);

    /**
     * Find pending commands for processing
     */
    @Query("SELECT dc FROM DeviceCommand dc WHERE dc.status = 'PENDING' AND " +
            "(dc.expiresAt IS NULL OR dc.expiresAt > :currentTime) " +
            "ORDER BY dc.priority ASC, dc.createdAt ASC")
    List<DeviceCommand> findPendingCommands(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Find expired commands
     */
    @Query("SELECT dc FROM DeviceCommand dc WHERE dc.status IN ('PENDING', 'SENT') AND " +
            "dc.expiresAt IS NOT NULL AND dc.expiresAt <= :currentTime")
    List<DeviceCommand> findExpiredCommands(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Find commands for retry
     */
    @Query("SELECT dc FROM DeviceCommand dc WHERE dc.status = 'FAILED' AND " +
            "dc.retryCount < dc.maxRetries AND " +
            "(dc.expiresAt IS NULL OR dc.expiresAt > :currentTime)")
    List<DeviceCommand> findCommandsForRetry(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Count commands by device and status
     */
    long countByDeviceAndStatus(Device device, CommandStatus status);

    /**
     * Find commands by company
     */
    Page<DeviceCommand> findByCompanyId(UUID companyId, Pageable pageable);

    /**
     * Find recent commands by device
     */
    @Query("SELECT dc FROM DeviceCommand dc WHERE dc.device = :device AND " +
            "dc.createdAt > :threshold ORDER BY dc.createdAt DESC")
    List<DeviceCommand> findRecentCommandsByDevice(@Param("device") Device device,
                                                   @Param("threshold") LocalDateTime threshold);
}