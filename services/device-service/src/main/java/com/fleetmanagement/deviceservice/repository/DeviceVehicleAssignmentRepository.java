

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
 * Device Vehicle Assignment Repository
 */
@Repository
interface DeviceVehicleAssignmentRepository extends JpaRepository<DeviceVehicleAssignment, UUID> {

    /**
     * Find assignments by device
     */
    List<DeviceVehicleAssignment> findByDevice(Device device);

    /**
     * Find active assignment for device
     */
    Optional<DeviceVehicleAssignment> findByDeviceAndStatus(Device device, AssignmentStatus status);

    /**
     * Find assignments by vehicle
     */
    List<DeviceVehicleAssignment> findByVehicleId(UUID vehicleId);

    /**
     * Find active assignments by vehicle
     */
    List<DeviceVehicleAssignment> findByVehicleIdAndStatus(UUID vehicleId, AssignmentStatus status);

    /**
     * Find assignments by company
     */
    Page<DeviceVehicleAssignment> findByCompanyId(UUID companyId, Pageable pageable);

    /**
     * Check if device is currently assigned to any vehicle
     */
    boolean existsByDeviceAndStatusAndUnassignedAtIsNull(Device device, AssignmentStatus status);

    /**
     * Find assignments needing attention
     */
    @Query("SELECT dva FROM DeviceVehicleAssignment dva WHERE dva.status = 'ASSIGNED' AND " +
            "dva.device.connectionStatus = 'DISCONNECTED' AND " +
            "dva.device.lastCommunication < :threshold")
    List<DeviceVehicleAssignment> findAssignmentsWithDisconnectedDevices(@Param("threshold") LocalDateTime threshold);
}