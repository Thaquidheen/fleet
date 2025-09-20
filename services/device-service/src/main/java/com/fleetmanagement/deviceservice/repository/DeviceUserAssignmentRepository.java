

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
 * Device User Assignment Repository
 */
@Repository
interface DeviceUserAssignmentRepository extends JpaRepository<DeviceUserAssignment, UUID> {

    /**
     * Find assignments by device
     */
    List<DeviceUserAssignment> findByDevice(Device device);

    /**
     * Find active assignment for device
     */
    Optional<DeviceUserAssignment> findByDeviceAndStatus(Device device, AssignmentStatus status);

    /**
     * Find assignments by user
     */
    List<DeviceUserAssignment> findByUserId(UUID userId);

    /**
     * Find active assignment by user
     */
    Optional<DeviceUserAssignment> findByUserIdAndStatus(UUID userId, AssignmentStatus status);

    /**
     * Find assignments by company
     */
    Page<DeviceUserAssignment> findByCompanyId(UUID companyId, Pageable pageable);

    /**
     * Find active tracking assignments
     */
    List<DeviceUserAssignment> findByStatusAndTrackingEnabled(AssignmentStatus status, Boolean trackingEnabled);

    /**
     * Check if user has active device assignment
     */
    boolean existsByUserIdAndStatusAndUnassignedAtIsNull(UUID userId, AssignmentStatus status);
}