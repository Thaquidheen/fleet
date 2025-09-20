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
 * Device Sensor Repository
 */
@Repository
interface DeviceSensorRepository extends JpaRepository<DeviceSensor, UUID> {

    /**
     * Find sensors by device
     */
    List<DeviceSensor> findByDevice(Device device);

    /**
     * Find active sensors by device
     */
    List<DeviceSensor> findByDeviceAndIsActive(Device device, Boolean isActive);

    /**
     * Find sensors by device and sensor type
     */
    Optional<DeviceSensor> findByDeviceAndSensorType(Device device, SensorType sensorType);

    /**
     * Count active sensors for a device
     */
    long countByDeviceAndIsActive(Device device, Boolean isActive);

    /**
     * Find sensors with recent readings
     */
    @Query("SELECT ds FROM DeviceSensor ds WHERE ds.device.companyId = :companyId AND " +
            "ds.lastReadingAt > :threshold AND ds.isActive = true")
    List<DeviceSensor> findSensorsWithRecentReadings(@Param("companyId") UUID companyId,
                                                     @Param("threshold") LocalDateTime threshold);
}