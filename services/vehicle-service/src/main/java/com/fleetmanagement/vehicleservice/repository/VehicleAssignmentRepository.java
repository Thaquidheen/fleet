package com.fleetmanagement.vehicleservice.repository;

import com.fleetmanagement.vehicleservice.domain.entity.VehicleAssignment;
import com.fleetmanagement.vehicleservice.domain.enums.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleAssignmentRepository extends JpaRepository<VehicleAssignment, UUID> {

    // Basic queries
    Optional<VehicleAssignment> findByIdAndCompanyId(UUID id, UUID companyId);

    List<VehicleAssignment> findByDriverIdAndCompanyIdOrderByStartDateDesc(UUID driverId, UUID companyId);

    List<VehicleAssignment> findByVehicleIdAndCompanyIdOrderByStartDateDesc(UUID vehicleId, UUID companyId);

    // Conflict detection queries
    @Query("SELECT va FROM VehicleAssignment va WHERE va.driverId = :driverId " +
            "AND va.status IN ('ACTIVE', 'CHECKED_IN') " +
            "AND ((va.startDate <= :endDate AND va.endDate >= :startDate) OR " +
            "(va.startDate <= :startDate AND (va.endDate IS NULL OR va.endDate >= :endDate)))")
    List<VehicleAssignment> findOverlappingDriverAssignments(@Param("driverId") UUID driverId,
                                                             @Param("startDate") LocalDate startDate,
                                                             @Param("endDate") LocalDate endDate);

    @Query("SELECT va FROM VehicleAssignment va WHERE va.vehicleId = :vehicleId " +
            "AND va.status IN ('ACTIVE', 'CHECKED_IN') " +
            "AND ((va.startDate <= :endDate AND va.endDate >= :startDate) OR " +
            "(va.startDate <= :startDate AND (va.endDate IS NULL OR va.endDate >= :endDate)))")
    List<VehicleAssignment> findOverlappingVehicleAssignments(@Param("vehicleId") UUID vehicleId,
                                                              @Param("startDate") LocalDate startDate,
                                                              @Param("endDate") LocalDate endDate);

    // Active assignments
    @Query("SELECT va FROM VehicleAssignment va WHERE va.companyId = :companyId " +
            "AND va.status IN ('ACTIVE', 'CHECKED_IN') " +
            "AND va.startDate <= CURRENT_DATE " +
            "AND (va.endDate IS NULL OR va.endDate >= CURRENT_DATE)")
    List<VehicleAssignment> findActiveAssignmentsByCompany(@Param("companyId") UUID companyId);

    // Statistics queries
    @Query("SELECT COUNT(va) FROM VehicleAssignment va WHERE va.companyId = :companyId " +
            "AND va.status = 'ACTIVE'")
    long countActiveAssignmentsByCompany(@Param("companyId") UUID companyId);

    @Query("SELECT COUNT(DISTINCT va.driverId) FROM VehicleAssignment va WHERE va.companyId = :companyId " +
            "AND va.status IN ('ACTIVE', 'CHECKED_IN')")
    long countActiveDriversByCompany(@Param("companyId") UUID companyId);

    @Query("SELECT COUNT(DISTINCT va.vehicleId) FROM VehicleAssignment va WHERE va.companyId = :companyId " +
            "AND va.status IN ('ACTIVE', 'CHECKED_IN')")
    long countActiveVehiclesByCompany(@Param("companyId") UUID companyId);
}