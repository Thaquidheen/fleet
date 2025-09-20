package com.fleetmanagement.deviceservice.repository;

import com.fleetmanagement.deviceservice.domain.entity.Device;
import com.fleetmanagement.deviceservice.domain.enums.DeviceStatus;
import com.fleetmanagement.deviceservice.domain.enums.DeviceType;
import com.fleetmanagement.deviceservice.domain.enums.ConnectionStatus;
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
 * Device Repository
 * Data access layer for Device entities
 */
@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID> {

    /**
     * Find device by device ID (IMEI)
     */
    Optional<Device> findByDeviceId(String deviceId);

    /**
     * Find device by Traccar ID
     */
    Optional<Device> findByTraccarId(Long traccarId);

    /**
     * Find all devices for a company
     */
    Page<Device> findByCompanyId(UUID companyId, Pageable pageable);

    /**
     * Find devices by company and status
     */
    Page<Device> findByCompanyIdAndStatus(UUID companyId, DeviceStatus status, Pageable pageable);

    /**
     * Find devices by company and device type
     */
    Page<Device> findByCompanyIdAndDeviceType(UUID companyId, DeviceType deviceType, Pageable pageable);

    /**
     * Count active devices for a company
     */
    long countByCompanyIdAndStatus(UUID companyId, DeviceStatus status);

    /**
     * Count mobile devices for a company
     */
    @Query("SELECT COUNT(d) FROM Device d WHERE d.companyId = :companyId AND d.deviceType = :deviceType")
    long countByCompanyIdAndDeviceType(@Param("companyId") UUID companyId, @Param("deviceType") DeviceType deviceType);

    /**
     * Find devices by connection status
     */
    List<Device> findByConnectionStatus(ConnectionStatus connectionStatus);

    /**
     * Find devices that haven't communicated recently
     */
    @Query("SELECT d FROM Device d WHERE d.lastCommunication < :threshold AND d.status = :status")
    List<Device> findDevicesWithoutRecentCommunication(
            @Param("threshold") LocalDateTime threshold,
            @Param("status") DeviceStatus status);

    /**
     * Find devices assigned to a specific vehicle
     */
    @Query("SELECT d FROM Device d JOIN d.vehicleAssignments va WHERE va.vehicleId = :vehicleId AND va.status = 'ASSIGNED'")
    List<Device> findByAssignedVehicleId(@Param("vehicleId") UUID vehicleId);

    /**
     * Find device assigned to a specific user (mobile devices)
     */
    @Query("SELECT d FROM Device d JOIN d.userAssignments ua WHERE ua.userId = :userId AND ua.status = 'ASSIGNED' AND d.deviceType = :deviceType")
    Optional<Device> findByAssignedUserIdAndDeviceType(@Param("userId") UUID userId, @Param("deviceType") DeviceType deviceType);

    /**
     * Find devices needing health check
     */
    @Query("SELECT d FROM Device d WHERE d.status = 'ACTIVE' AND " +
            "(d.lastCommunication IS NULL OR d.lastCommunication < :threshold)")
    List<Device> findDevicesNeedingHealthCheck(@Param("threshold") LocalDateTime threshold);

    /**
     * Search devices by name or device ID
     */
    @Query("SELECT d FROM Device d WHERE d.companyId = :companyId AND " +
            "(LOWER(d.deviceName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(d.deviceId) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Device> searchDevices(@Param("companyId") UUID companyId, @Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find devices with active sensor subscriptions
     */
    @Query("SELECT DISTINCT d FROM Device d JOIN d.sensors ds JOIN ds.subscriptions ss " +
            "WHERE d.companyId = :companyId AND ss.isActive = true")
    List<Device> findDevicesWithActiveSensorSubscriptions(@Param("companyId") UUID companyId);

    /**
     * Get device statistics for a company
     */
    @Query("SELECT " +
            "COUNT(d) as totalDevices, " +
            "SUM(CASE WHEN d.status = 'ACTIVE' THEN 1 ELSE 0 END) as activeDevices, " +
            "SUM(CASE WHEN d.connectionStatus = 'CONNECTED' THEN 1 ELSE 0 END) as connectedDevices, " +
            "SUM(CASE WHEN d.deviceType = 'MOBILE_PHONE' THEN 1 ELSE 0 END) as mobileDevices " +
            "FROM Device d WHERE d.companyId = :companyId")
    Object[] getDeviceStatistics(@Param("companyId") UUID companyId);

    /**
     * Find devices by multiple criteria
     */
    @Query("SELECT d FROM Device d WHERE " +
            "(:companyId IS NULL OR d.companyId = :companyId) AND " +
            "(:status IS NULL OR d.status = :status) AND " +
            "(:deviceType IS NULL OR d.deviceType = :deviceType) AND " +
            "(:connectionStatus IS NULL OR d.connectionStatus = :connectionStatus)")
    Page<Device> findDevicesByCriteria(
            @Param("companyId") UUID companyId,
            @Param("status") DeviceStatus status,
            @Param("deviceType") DeviceType deviceType,
            @Param("connectionStatus") ConnectionStatus connectionStatus,
            Pageable pageable);

    /**
     * Check if device ID already exists
     */
    boolean existsByDeviceId(String deviceId);

    /**
     * Check if Traccar ID already exists
     */
    boolean existsByTraccarId(Long traccarId);

    /**
     * Find devices for bulk operations
     */
    @Query("SELECT d FROM Device d WHERE d.id IN :deviceIds AND d.companyId = :companyId")
    List<Device> findByIdsAndCompanyId(@Param("deviceIds") List<UUID> deviceIds, @Param("companyId") UUID companyId);

    /**
     * Update connection status for multiple devices
     */
    @Query("UPDATE Device d SET d.connectionStatus = :status, d.lastCommunication = :timestamp " +
            "WHERE d.id IN :deviceIds")
    int updateConnectionStatusBatch(@Param("deviceIds") List<UUID> deviceIds,
                                    @Param("status") ConnectionStatus status,
                                    @Param("timestamp") LocalDateTime timestamp);

    /**
     * Find devices that need billing calculation
     */
    @Query("SELECT d FROM Device d JOIN d.sensors ds JOIN ds.subscriptions ss " +
            "WHERE ss.isActive = true AND ss.billingCycleEnd <= :currentDate")
    List<Device> findDevicesForBilling(@Param("currentDate") LocalDateTime currentDate);
}

