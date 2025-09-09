package com.fleetmanagement.vehicleservice.repository;

import com.fleetmanagement.vehicleservice.domain.entity.VehicleAssignment;
import com.fleetmanagement.vehicleservice.domain.enums.AssignmentStatus;
import com.fleetmanagement.vehicleservice.domain.enums.AssignmentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Vehicle Assignment Repository Interface
 *
 * Provides data access methods for VehicleAssignment entities with:
 * - Driver-vehicle assignment management
 * - Conflict detection and validation
 * - Time-based queries and filtering
 * - Assignment history tracking
 */
@Repository
public interface VehicleAssignmentRepository extends JpaRepository<VehicleAssignment, UUID>, JpaSpecificationExecutor<VehicleAssignment> {

    // Basic find methods
    Optional<VehicleAssignment> findByIdAndCompanyId(UUID id, UUID companyId);

    // Current assignment queries
    Optional<VehicleAssignment> findByVehicleIdAndStatusAndCompanyId(UUID vehicleId, AssignmentStatus status, UUID companyId);

    Optional<VehicleAssignment> findByDriverIdAndStatusAndCompanyId(UUID driverId, AssignmentStatus status, UUID companyId);

    List<VehicleAssignment> findByDriverIdAndCompanyId(UUID driverId, UUID companyId);

    List<VehicleAssignment> findByVehicleIdAndCompanyId(UUID vehicleId, UUID companyId);

    // Active assignment queries
    @Query("SELECT va FROM VehicleAssignment va WHERE va.vehicleId = :vehicleId AND va.companyId = :companyId " +
            "AND va.status IN ('ASSIGNED', 'TEMPORARY') " +
            "AND va.startDate <= CURRENT_DATE " +
            "AND (va.endDate IS NULL OR va.endDate >= CURRENT_DATE)")
    List<VehicleAssignment> findActiveAssignmentsByVehicle(@Param("vehicleId") UUID vehicleId, @Param("companyId") UUID companyId);

    @Query("SELECT va FROM VehicleAssignment va WHERE va.driverId = :driverId AND va.companyId = :companyId " +
            "AND va.status IN ('ASSIGNED', 'TEMPORARY') " +
            "AND va.startDate <= CURRENT_DATE " +
            "AND (va.endDate IS NULL OR va.endDate >= CURRENT_DATE)")
    List<VehicleAssignment> findActiveAssignmentsByDriver(@Param("driverId") UUID driverId, @Param("companyId") UUID companyId);

    // Company-specific queries
    List<VehicleAssignment> findByCompanyId(UUID companyId);

    Page<VehicleAssignment> findByCompanyId(UUID companyId, Pageable pageable);

    List<VehicleAssignment> findByCompanyIdAndStatus(UUID companyId, AssignmentStatus status);

    Page<VehicleAssignment> findByCompanyIdAndStatus(UUID companyId, AssignmentStatus status, Pageable pageable);

    // Status-based queries
    @Query("SELECT COUNT(va) FROM VehicleAssignment va WHERE va.companyId = :companyId AND va.status = :status")
    long countByCompanyIdAndStatus(@Param("companyId") UUID companyId, @Param("status") AssignmentStatus status);

    @Query("SELECT COUNT(DISTINCT va.vehicleId) FROM VehicleAssignment va WHERE va.companyId = :companyId " +
            "AND va.status IN ('ASSIGNED', 'TEMPORARY')")
    long countAssignedVehiclesByCompany(@Param("companyId") UUID companyId);

    @Query("SELECT COUNT(DISTINCT va.driverId) FROM VehicleAssignment va WHERE va.companyId = :companyId " +
            "AND va.status IN ('ASSIGNED', 'TEMPORARY')")
    long countActiveDriversByCompany(@Param("companyId") UUID companyId);

    // Assignment type queries
    List<VehicleAssignment> findByCompanyIdAndAssignmentType(UUID companyId, AssignmentType assignmentType);

    @Query("SELECT COUNT(va) FROM VehicleAssignment va WHERE va.companyId = :companyId AND va.assignmentType = :type")
    long countByCompanyIdAndAssignmentType(@Param("companyId") UUID companyId, @Param("type") AssignmentType type);

    // Date range queries
    @Query("SELECT va FROM VehicleAssignment va WHERE va.companyId = :companyId " +
            "AND va.startDate BETWEEN :startDate AND :endDate")
    List<VehicleAssignment> findAssignmentsStartingInPeriod(@Param("companyId") UUID companyId,
                                                            @Param("startDate") LocalDate startDate,
                                                            @Param("endDate") LocalDate endDate);

    @Query("SELECT va FROM VehicleAssignment va WHERE va.companyId = :companyId " +
            "AND va.endDate BETWEEN :startDate AND :endDate")
    List<VehicleAssignment> findAssignmentsEndingInPeriod(@Param("companyId") UUID companyId,
                                                          @Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);

    @Query("SELECT va FROM VehicleAssignment va WHERE va.companyId = :companyId " +
            "AND va.startDate <= :endDate " +
            "AND (va.endDate IS NULL OR va.endDate >= :startDate)")
    List<VehicleAssignment> findAssignmentsInPeriod(@Param("companyId") UUID companyId,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    // Expiring assignments
    @Query("SELECT va FROM VehicleAssignment va WHERE va.companyId = :companyId " +
            "AND va.status IN ('ASSIGNED', 'TEMPORARY') " +
            "AND va.endDate BETWEEN CURRENT_DATE AND :endDate")
    List<VehicleAssignment> findExpiringAssignments(@Param("companyId") UUID companyId, @Param("endDate") LocalDate endDate);

    @Query("SELECT va FROM VehicleAssignment va WHERE va.companyId = :companyId " +
            "AND va.status IN ('ASSIGNED', 'TEMPORARY') " +
            "AND va.endDate < CURRENT_DATE")
    List<VehicleAssignment> findOverdueAssignments(@Param("companyId") UUID companyId);

    // Conflict detection queries
    @Query("SELECT COUNT(va) > 0 FROM VehicleAssignment va WHERE va.vehicleId = :vehicleId " +
            "AND va.companyId = :companyId AND va.status IN ('ASSIGNED', 'TEMPORARY') " +
            "AND (:assignmentId IS NULL OR va.id != :assignmentId) " +
            "AND va.startDate <= :endDate " +
            "AND (va.endDate IS NULL OR va.endDate >= :startDate)")
    boolean hasVehicleConflict(@Param("vehicleId") UUID vehicleId,
                               @Param("companyId") UUID companyId,
                               @Param("startDate") LocalDate startDate,
                               @Param("endDate") LocalDate endDate,
                               @Param("assignmentId") UUID assignmentId);

    @Query("SELECT COUNT(va) > 0 FROM VehicleAssignment va WHERE va.driverId = :driverId " +
            "AND va.companyId = :companyId AND va.status IN ('ASSIGNED', 'TEMPORARY') " +
            "AND (:assignmentId IS NULL OR va.id != :assignmentId) " +
            "AND va.startDate <= :endDate " +
            "AND (va.endDate IS NULL OR va.endDate >= :startDate)")
    boolean hasDriverConflict(@Param("driverId") UUID driverId,
                              @Param("companyId") UUID companyId,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate,
                              @Param("assignmentId") UUID assignmentId);

    @Query("SELECT va FROM VehicleAssignment va WHERE va.vehicleId = :vehicleId " +
            "AND va.companyId = :companyId AND va.status IN ('ASSIGNED', 'TEMPORARY') " +
            "AND (:assignmentId IS NULL OR va.id != :assignmentId) " +
            "AND va.startDate <= :endDate " +
            "AND (va.endDate IS NULL OR va.endDate >= :startDate)")
    List<VehicleAssignment> findConflictingVehicleAssignments(@Param("vehicleId") UUID vehicleId,
                                                              @Param("companyId") UUID companyId,
                                                              @Param("startDate") LocalDate startDate,
                                                              @Param("endDate") LocalDate endDate,
                                                              @Param("assignmentId") UUID assignmentId);

    @Query("SELECT va FROM VehicleAssignment va WHERE va.driverId = :driverId " +
            "AND va.companyId = :companyId AND va.status IN ('ASSIGNED', 'TEMPORARY') " +
            "AND (:assignmentId IS NULL OR va.id != :assignmentId) " +
            "AND va.startDate <= :endDate " +
            "AND (va.endDate IS NULL OR va.endDate >= :startDate)")
    List<VehicleAssignment> findConflictingDriverAssignments(@Param("driverId") UUID driverId,
                                                             @Param("companyId") UUID companyId,
                                                             @Param("startDate") LocalDate startDate,
                                                             @Param("endDate") LocalDate endDate,
                                                             @Param("assignmentId") UUID assignmentId);

    // Check-in/Check-out queries
    @Query("SELECT va FROM VehicleAssignment va WHERE va.companyId = :companyId " +
            "AND va.lastCheckinTime IS NOT NULL " +
            "AND (va.lastCheckoutTime IS NULL OR va.lastCheckinTime > va.lastCheckoutTime)")
    List<VehicleAssignment> findCurrentlyCheckedInAssignments(@Param("companyId") UUID companyId);

    @Query("SELECT COUNT(va) FROM VehicleAssignment va WHERE va.companyId = :companyId " +
            "AND va.lastCheckinTime IS NOT NULL " +
            "AND (va.lastCheckoutTime IS NULL OR va.lastCheckinTime > va.lastCheckoutTime)")
    long countCurrentlyCheckedInAssignments(@Param("companyId") UUID companyId);

    @Query("SELECT va FROM VehicleAssignment va WHERE va.driverId = :driverId AND va.companyId = :companyId " +
            "AND va.lastCheckinTime IS NOT NULL " +
            "AND (va.lastCheckoutTime IS NULL OR va.lastCheckinTime > va.lastCheckoutTime)")
    Optional<VehicleAssignment> findCurrentCheckedInAssignmentByDriver(@Param("driverId") UUID driverId,
                                                                       @Param("companyId") UUID companyId);

    // Analytics and reporting queries
    @Query("SELECT va.assignmentType, COUNT(va) FROM VehicleAssignment va WHERE va.companyId = :companyId " +
            "AND va.status IN ('ASSIGNED', 'TEMPORARY') GROUP BY va.assignmentType")
    List<Object[]> getAssignmentCountByType(@Param("companyId") UUID companyId);

    @Query("SELECT va.status, COUNT(va) FROM VehicleAssignment va WHERE va.companyId = :companyId GROUP BY va.status")
    List<Object[]> getAssignmentCountByStatus(@Param("companyId") UUID companyId);

    @Query("SELECT DATE(va.assignedDate), COUNT(va) FROM VehicleAssignment va WHERE va.companyId = :companyId " +
            "AND va.assignedDate >= :startDate GROUP BY DATE(va.assignedDate) ORDER BY DATE(va.assignedDate)")
    List<Object[]> getAssignmentCountByDate(@Param("companyId") UUID companyId, @Param("startDate") LocalDateTime startDate);

    // Duration and utilization queries
    @Query("SELECT AVG(DATEDIFF(COALESCE(va.endDate, CURRENT_DATE), va.startDate)) FROM VehicleAssignment va " +
            "WHERE va.companyId = :companyId AND va.status = 'ASSIGNED'")
    Double getAverageAssignmentDuration(@Param("companyId") UUID companyId);

    @Query("SELECT va.driverId, COUNT(va) as assignmentCount FROM VehicleAssignment va " +
            "WHERE va.companyId = :companyId AND va.status IN ('ASSIGNED', 'TEMPORARY') " +
            "GROUP BY va.driverId ORDER BY assignmentCount DESC")
    List<Object[]> getDriverAssignmentCounts(@Param("companyId") UUID companyId);

    // Search queries
    @Query("SELECT va FROM VehicleAssignment va " +
            "JOIN va.vehicle v " +
            "WHERE va.companyId = :companyId " +
            "AND (LOWER(v.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(v.licensePlate) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(v.make) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(v.model) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<VehicleAssignment> searchAssignmentsByVehicle(@Param("companyId") UUID companyId,
                                                       @Param("searchTerm") String searchTerm,
                                                       Pageable pageable);

    // Batch operations
    @Modifying
    @Query("UPDATE VehicleAssignment va SET va.status = :newStatus, va.updatedBy = :updatedBy, va.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE va.companyId = :companyId AND va.id IN :assignmentIds")
    int updateAssignmentStatusBatch(@Param("companyId") UUID companyId,
                                    @Param("assignmentIds") List<UUID> assignmentIds,
                                    @Param("newStatus") AssignmentStatus newStatus,
                                    @Param("updatedBy") UUID updatedBy);

    @Modifying
    @Query("UPDATE VehicleAssignment va SET va.status = 'EXPIRED', va.endDate = CURRENT_DATE, " +
            "va.updatedBy = :updatedBy, va.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE va.companyId = :companyId AND va.driverId = :driverId AND va.status IN ('ASSIGNED', 'TEMPORARY')")
    int terminateAllDriverAssignments(@Param("companyId") UUID companyId,
                                      @Param("driverId") UUID driverId,
                                      @Param("updatedBy") UUID updatedBy);

    @Modifying
    @Query("UPDATE VehicleAssignment va SET va.status = 'EXPIRED', va.endDate = CURRENT_DATE, " +
            "va.updatedBy = :updatedBy, va.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE va.companyId = :companyId AND va.vehicleId = :vehicleId AND va.status IN ('ASSIGNED', 'TEMPORARY')")
    int terminateAllVehicleAssignments(@Param("companyId") UUID companyId,
                                       @Param("vehicleId") UUID vehicleId,
                                       @Param("updatedBy") UUID updatedBy);

    // Auto-expire assignments
    @Modifying
    @Query("UPDATE VehicleAssignment va SET va.status = 'EXPIRED', va.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE va.companyId = :companyId AND va.status IN ('ASSIGNED', 'TEMPORARY') " +
            "AND va.endDate < CURRENT_DATE")
    int expireOverdueAssignments(@Param("companyId") UUID companyId);

    // Check-in/Check-out operations
    @Modifying
    @Query("UPDATE VehicleAssignment va SET va.lastCheckinTime = :checkinTime, " +
            "va.checkinLocationLat = :latitude, va.checkinLocationLng = :longitude, " +
            "va.updatedBy = :updatedBy, va.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE va.id = :assignmentId AND va.companyId = :companyId")
    int updateCheckinInfo(@Param("assignmentId") UUID assignmentId,
                          @Param("companyId") UUID companyId,
                          @Param("checkinTime") LocalDateTime checkinTime,
                          @Param("latitude") java.math.BigDecimal latitude,
                          @Param("longitude") java.math.BigDecimal longitude,
                          @Param("updatedBy") UUID updatedBy);

    @Modifying
    @Query("UPDATE VehicleAssignment va SET va.lastCheckoutTime = :checkoutTime, " +
            "va.updatedBy = :updatedBy, va.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE va.id = :assignmentId AND va.companyId = :companyId")
    int updateCheckoutInfo(@Param("assignmentId") UUID assignmentId,
                           @Param("companyId") UUID companyId,
                           @Param("checkoutTime") LocalDateTime checkoutTime,
                           @Param("updatedBy") UUID updatedBy);

    // Advanced filtering
    @Query("SELECT va FROM VehicleAssignment va WHERE va.companyId = :companyId " +
            "AND (:status IS NULL OR va.status = :status) " +
            "AND (:assignmentType IS NULL OR va.assignmentType = :assignmentType) " +
            "AND (:vehicleId IS NULL OR va.vehicleId = :vehicleId) " +
            "AND (:driverId IS NULL OR va.driverId = :driverId) " +
            "AND (:startDateFrom IS NULL OR va.startDate >= :startDateFrom) " +
            "AND (:startDateTo IS NULL OR va.startDate <= :startDateTo) " +
            "AND (:endDateFrom IS NULL OR va.endDate >= :endDateFrom) " +
            "AND (:endDateTo IS NULL OR va.endDate <= :endDateTo)")
    Page<VehicleAssignment> findAssignmentsWithCriteria(@Param("companyId") UUID companyId,
                                                        @Param("status") AssignmentStatus status,
                                                        @Param("assignmentType") AssignmentType assignmentType,
                                                        @Param("vehicleId") UUID vehicleId,
                                                        @Param("driverId") UUID driverId,
                                                        @Param("startDateFrom") LocalDate startDateFrom,
                                                        @Param("startDateTo") LocalDate startDateTo,
                                                        @Param("endDateFrom") LocalDate endDateFrom,
                                                        @Param("endDateTo") LocalDate endDateTo,
                                                        Pageable pageable);

    // Validation queries
    @Query("SELECT CASE WHEN COUNT(va) > 0 THEN true ELSE false END FROM VehicleAssignment va " +
            "WHERE va.vehicleId = :vehicleId AND va.companyId = :companyId AND va.status IN ('ASSIGNED', 'TEMPORARY')")
    boolean isVehicleCurrentlyAssigned(@Param("vehicleId") UUID vehicleId, @Param("companyId") UUID companyId);

    @Query("SELECT CASE WHEN COUNT(va) > 0 THEN true ELSE false END FROM VehicleAssignment va " +
            "WHERE va.driverId = :driverId AND va.companyId = :companyId AND va.status IN ('ASSIGNED', 'TEMPORARY')")
    boolean isDriverCurrentlyAssigned(@Param("driverId") UUID driverId, @Param("companyId") UUID companyId);

    // Assignment history
    @Query("SELECT va FROM VehicleAssignment va WHERE va.vehicleId = :vehicleId AND va.companyId = :companyId " +
            "ORDER BY va.assignedDate DESC")
    List<VehicleAssignment> findVehicleAssignmentHistory(@Param("vehicleId") UUID vehicleId, @Param("companyId") UUID companyId);

    @Query("SELECT va FROM VehicleAssignment va WHERE va.driverId = :driverId AND va.companyId = :companyId " +
            "ORDER BY va.assignedDate DESC")
    List<VehicleAssignment> findDriverAssignmentHistory(@Param("driverId") UUID driverId, @Param("companyId") UUID companyId);

    @Query("SELECT va FROM VehicleAssignment va WHERE va.vehicleId = :vehicleId AND va.companyId = :companyId " +
            "ORDER BY va.assignedDate DESC")
    Page<VehicleAssignment> findVehicleAssignmentHistory(@Param("vehicleId") UUID vehicleId,
                                                         @Param("companyId") UUID companyId,
                                                         Pageable pageable);

    @Query("SELECT va FROM VehicleAssignment va WHERE va.driverId = :driverId AND va.companyId = :companyId " +
            "ORDER BY va.assignedDate DESC")
    Page<VehicleAssignment> findDriverAssignmentHistory(@Param("driverId") UUID driverId,
                                                        @Param("companyId") UUID companyId,
                                                        Pageable pageable);
}