

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
import java.util.Optional;
import java.util.UUID;

/**
 * Device Health Repository
 */
@Repository
interface DeviceHealthRepository extends JpaRepository<DeviceHealth, UUID> {

    /**
     * Find health records by device
     */
    Page<DeviceHealth> findByDevice(Device device, Pageable pageable);

    /**
     * Find latest health record for device
     */
    Optional<DeviceHealth> findFirstByDeviceOrderByRecordedAtDesc(Device device);

    /**
     * Find devices with critical health
     */
    List<DeviceHealth> findByHealthLevel(HealthLevel healthLevel);

    /**
     * Find devices needing attention
     */
    @Query("SELECT dh FROM DeviceHealth dh WHERE dh.healthLevel IN ('POOR', 'CRITICAL') AND " +
            "dh.recordedAt = (SELECT MAX(dh2.recordedAt) FROM DeviceHealth dh2 WHERE dh2.device = dh.device)")
    List<DeviceHealth> findDevicesNeedingAttention();

    /**
     * Find health records by company
     */
    @Query("SELECT dh FROM DeviceHealth dh WHERE dh.device.companyId = :companyId")
    Page<DeviceHealth> findByCompanyId(@Param("companyId") UUID companyId, Pageable pageable);

    /**
     * Get average health score for company
     */
    @Query("SELECT AVG(dh.healthScore) FROM DeviceHealth dh WHERE dh.device.companyId = :companyId AND " +
            "dh.recordedAt > :threshold")
    Double getAverageHealthScore(@Param("companyId") UUID companyId, @Param("threshold") LocalDateTime threshold);

    /**
     * Delete old health records
     */
    @Query("DELETE FROM DeviceHealth dh WHERE dh.recordedAt < :threshold")
    int deleteOldHealthRecords(@Param("threshold") LocalDateTime threshold);

    /**
     * Find devices with communication issues
     */
    @Query("SELECT dh FROM DeviceHealth dh WHERE dh.communicationFailures > :threshold AND " +
            "dh.recordedAt = (SELECT MAX(dh2.recordedAt) FROM DeviceHealth dh2 WHERE dh2.device = dh.device)")
    List<DeviceHealth> findDevicesWithCommunicationIssues(@Param("threshold") Integer threshold);
}