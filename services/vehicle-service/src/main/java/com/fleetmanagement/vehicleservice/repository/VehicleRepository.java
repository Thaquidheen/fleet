package com.fleetmanagement.vehicleservice.repository;

import com.fleetmanagement.vehicleservice.domain.entity.Vehicle;
import com.fleetmanagement.vehicleservice.domain.enums.VehicleStatus;
import com.fleetmanagement.vehicleservice.domain.enums.VehicleType;
import com.fleetmanagement.vehicleservice.domain.enums.FuelType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Vehicle Repository Interface
 *
 * Provides data access methods for Vehicle entities with:
 * - Multi-tenant support (company-scoped queries)
 * - Complex search and filtering capabilities
 * - Performance-optimized queries
 * - Custom business logic queries
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID>, JpaSpecificationExecutor<Vehicle> {

    // Basic find methods
    Optional<Vehicle> findByIdAndCompanyId(UUID id, UUID companyId);

    Optional<Vehicle> findByVin(String vin);

    Optional<Vehicle> findByVinAndCompanyId(String vin, UUID companyId);

    Optional<Vehicle> findByLicensePlate(String licensePlate);

    Optional<Vehicle> findByLicensePlateAndCompanyId(String licensePlate, UUID companyId);

    // Existence checks
    boolean existsByVin(String vin);

    boolean existsByVinAndCompanyId(String vin, UUID companyId);

    boolean existsByLicensePlate(String licensePlate);

    boolean existsByLicensePlateAndCompanyId(String licensePlate, UUID companyId);

    boolean existsByVinAndIdNot(String vin, UUID id);

    boolean existsByLicensePlateAndIdNot(String licensePlate, UUID id);

    // Company-specific queries
    List<Vehicle> findByCompanyId(UUID companyId);

    Page<Vehicle> findByCompanyId(UUID companyId, Pageable pageable);

    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.companyId = :companyId")
    long countByCompanyId(@Param("companyId") UUID companyId);

    // Status-based queries
    List<Vehicle> findByCompanyIdAndStatus(UUID companyId, VehicleStatus status);

    Page<Vehicle> findByCompanyIdAndStatus(UUID companyId, VehicleStatus status, Pageable pageable);

    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.companyId = :companyId AND v.status = :status")
    long countByCompanyIdAndStatus(@Param("companyId") UUID companyId, @Param("status") VehicleStatus status);

    // Type-based queries
    List<Vehicle> findByCompanyIdAndVehicleType(UUID companyId, VehicleType vehicleType);

    Page<Vehicle> findByCompanyIdAndVehicleType(UUID companyId, VehicleType vehicleType, Pageable pageable);

    List<Vehicle> findByCompanyIdAndFuelType(UUID companyId, FuelType fuelType);

    // Assignment-related queries
    List<Vehicle> findByCompanyIdAndCurrentDriverId(UUID companyId, UUID driverId);

    List<Vehicle> findByCompanyIdAndCurrentDriverIdIsNull(UUID companyId);

    Page<Vehicle> findByCompanyIdAndCurrentDriverIdIsNull(UUID companyId, Pageable pageable);

    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.companyId = :companyId AND v.currentDriverId IS NOT NULL")
    long countAssignedVehiclesByCompanyId(@Param("companyId") UUID companyId);

    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.companyId = :companyId AND v.currentDriverId IS NULL")
    long countUnassignedVehiclesByCompanyId(@Param("companyId") UUID companyId);

    // Maintenance-related queries
    @Query("SELECT v FROM Vehicle v WHERE v.companyId = :companyId AND " +
            "(v.nextServiceDueDate <= CURRENT_DATE OR " +
            "(v.nextServiceDueMileage IS NOT NULL AND v.currentMileage >= v.nextServiceDueMileage))")
    List<Vehicle> findVehiclesDueForMaintenance(@Param("companyId") UUID companyId);

    @Query("SELECT v FROM Vehicle v WHERE v.companyId = :companyId AND " +
            "v.nextServiceDueDate BETWEEN CURRENT_DATE AND :endDate")
    List<Vehicle> findVehiclesDueForMaintenanceInPeriod(@Param("companyId") UUID companyId,
                                                        @Param("endDate") LocalDate endDate);

    // Insurance and registration expiry queries
    @Query("SELECT v FROM Vehicle v WHERE v.companyId = :companyId AND " +
            "v.insuranceExpiryDate BETWEEN CURRENT_DATE AND :endDate")
    List<Vehicle> findVehiclesWithExpiringInsurance(@Param("companyId") UUID companyId,
                                                    @Param("endDate") LocalDate endDate);

    @Query("SELECT v FROM Vehicle v WHERE v.companyId = :companyId AND " +
            "v.registrationExpiryDate BETWEEN CURRENT_DATE AND :endDate")
    List<Vehicle> findVehiclesWithExpiringRegistration(@Param("companyId") UUID companyId,
                                                       @Param("endDate") LocalDate endDate);

    // Location-based queries
    @Query("SELECT v FROM Vehicle v WHERE v.companyId = :companyId AND " +
            "v.currentLocationLat IS NOT NULL AND v.currentLocationLng IS NOT NULL")
    List<Vehicle> findVehiclesWithLocation(@Param("companyId") UUID companyId);

    @Query(value = "SELECT * FROM vehicles v WHERE v.company_id = :companyId AND " +
            "v.current_location_lat IS NOT NULL AND v.current_location_lng IS NOT NULL AND " +
            "ST_DWithin(ST_Point(v.current_location_lng, v.current_location_lat)::geography, " +
            "ST_Point(:longitude, :latitude)::geography, :radiusMeters)",
            nativeQuery = true)
    List<Vehicle> findVehiclesNearLocation(@Param("companyId") UUID companyId,
                                           @Param("latitude") BigDecimal latitude,
                                           @Param("longitude") BigDecimal longitude,
                                           @Param("radiusMeters") double radiusMeters);

    // Analytics and reporting queries
    @Query("SELECT v.vehicleType, COUNT(v) FROM Vehicle v WHERE v.companyId = :companyId GROUP BY v.vehicleType")
    List<Object[]> getVehicleCountByType(@Param("companyId") UUID companyId);

    @Query("SELECT v.status, COUNT(v) FROM Vehicle v WHERE v.companyId = :companyId GROUP BY v.status")
    List<Object[]> getVehicleCountByStatus(@Param("companyId") UUID companyId);

    @Query("SELECT v.fuelType, COUNT(v) FROM Vehicle v WHERE v.companyId = :companyId GROUP BY v.fuelType")
    List<Object[]> getVehicleCountByFuelType(@Param("companyId") UUID companyId);

    // Age and mileage analytics
    @Query("SELECT AVG(v.currentMileage) FROM Vehicle v WHERE v.companyId = :companyId AND v.status = 'ACTIVE'")
    Double getAverageMileageByCompanyId(@Param("companyId") UUID companyId);

    @Query("SELECT AVG(EXTRACT(YEAR FROM CURRENT_DATE) - v.year) FROM Vehicle v WHERE v.companyId = :companyId AND v.status = 'ACTIVE'")
    Double getAverageAgeByCompanyId(@Param("companyId") UUID companyId);

    @Query("SELECT SUM(v.currentMileage) FROM Vehicle v WHERE v.companyId = :companyId AND v.status = 'ACTIVE'")
    Long getTotalMileageByCompanyId(@Param("companyId") UUID companyId);

    // Search queries
    @Query("SELECT v FROM Vehicle v WHERE v.companyId = :companyId AND " +
            "(LOWER(v.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(v.make) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(v.model) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(v.licensePlate) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "v.vin LIKE UPPER(CONCAT('%', :searchTerm, '%')))")
    Page<Vehicle> searchVehicles(@Param("companyId") UUID companyId,
                                 @Param("searchTerm") String searchTerm,
                                 Pageable pageable);

    // Advanced search with multiple criteria
    @Query("SELECT v FROM Vehicle v WHERE v.companyId = :companyId " +
            "AND (:status IS NULL OR v.status = :status) " +
            "AND (:vehicleType IS NULL OR v.vehicleType = :vehicleType) " +
            "AND (:fuelType IS NULL OR v.fuelType = :fuelType) " +
            "AND (:assignedOnly IS NULL OR " +
            "     (:assignedOnly = true AND v.currentDriverId IS NOT NULL) OR " +
            "     (:assignedOnly = false AND v.currentDriverId IS NULL)) " +
            "AND (:yearFrom IS NULL OR v.year >= :yearFrom) " +
            "AND (:yearTo IS NULL OR v.year <= :yearTo)")
    Page<Vehicle> findVehiclesWithCriteria(@Param("companyId") UUID companyId,
                                           @Param("status") VehicleStatus status,
                                           @Param("vehicleType") VehicleType vehicleType,
                                           @Param("fuelType") FuelType fuelType,
                                           @Param("assignedOnly") Boolean assignedOnly,
                                           @Param("yearFrom") Integer yearFrom,
                                           @Param("yearTo") Integer yearTo,
                                           Pageable pageable);

    // Batch operations
    @Modifying
    @Query("UPDATE Vehicle v SET v.status = :newStatus, v.updatedBy = :updatedBy, v.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE v.companyId = :companyId AND v.id IN :vehicleIds")
    int updateVehicleStatusBatch(@Param("companyId") UUID companyId,
                                 @Param("vehicleIds") List<UUID> vehicleIds,
                                 @Param("newStatus") VehicleStatus newStatus,
                                 @Param("updatedBy") UUID updatedBy);

    @Modifying
    @Query("UPDATE Vehicle v SET v.currentDriverId = :driverId, v.updatedBy = :updatedBy, v.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE v.companyId = :companyId AND v.id = :vehicleId AND v.currentDriverId IS NULL")
    int assignDriverToVehicle(@Param("companyId") UUID companyId,
                              @Param("vehicleId") UUID vehicleId,
                              @Param("driverId") UUID driverId,
                              @Param("updatedBy") UUID updatedBy);

    @Modifying
    @Query("UPDATE Vehicle v SET v.currentDriverId = NULL, v.updatedBy = :updatedBy, v.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE v.companyId = :companyId AND v.currentDriverId = :driverId")
    int unassignDriverFromAllVehicles(@Param("companyId") UUID companyId,
                                      @Param("driverId") UUID driverId,
                                      @Param("updatedBy") UUID updatedBy);

    // Mileage updates
    @Modifying
    @Query("UPDATE Vehicle v SET v.currentMileage = :newMileage, v.updatedBy = :updatedBy, v.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE v.companyId = :companyId AND v.id = :vehicleId AND v.currentMileage <= :newMileage")
    int updateVehicleMileage(@Param("companyId") UUID companyId,
                             @Param("vehicleId") UUID vehicleId,
                             @Param("newMileage") Integer newMileage,
                             @Param("updatedBy") UUID updatedBy);

    // Location updates
    @Modifying
    @Query("UPDATE Vehicle v SET v.currentLocationLat = :latitude, v.currentLocationLng = :longitude, " +
            "v.updatedBy = :updatedBy, v.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE v.companyId = :companyId AND v.id = :vehicleId")
    int updateVehicleLocation(@Param("companyId") UUID companyId,
                              @Param("vehicleId") UUID vehicleId,
                              @Param("latitude") BigDecimal latitude,
                              @Param("longitude") BigDecimal longitude,
                              @Param("updatedBy") UUID updatedBy);

    // Fleet utilization metrics
    @Query("SELECT " +
            "COUNT(CASE WHEN v.status = 'ACTIVE' THEN 1 END) as activeCount, " +
            "COUNT(CASE WHEN v.status = 'ACTIVE' AND v.currentDriverId IS NOT NULL THEN 1 END) as assignedCount, " +
            "COUNT(CASE WHEN v.status = 'MAINTENANCE' THEN 1 END) as maintenanceCount, " +
            "COUNT(v) as totalCount " +
            "FROM Vehicle v WHERE v.companyId = :companyId")
    Object[] getFleetUtilizationMetrics(@Param("companyId") UUID companyId);

    // Custom validation queries
    @Query("SELECT COUNT(v) > 0 FROM Vehicle v WHERE v.companyId = :companyId AND v.currentDriverId = :driverId")
    boolean isDriverAssignedToAnyVehicle(@Param("companyId") UUID companyId, @Param("driverId") UUID driverId);

    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.companyId = :companyId AND v.status IN ('ACTIVE', 'MAINTENANCE')")
    long countActiveAndMaintenanceVehicles(@Param("companyId") UUID companyId);

    // Date range queries for reporting
    @Query("SELECT v FROM Vehicle v WHERE v.companyId = :companyId AND v.createdAt BETWEEN :startDate AND :endDate")
    List<Vehicle> findVehiclesCreatedInPeriod(@Param("companyId") UUID companyId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    @Query("SELECT v FROM Vehicle v WHERE v.companyId = :companyId AND v.purchaseDate BETWEEN :startDate AND :endDate")
    List<Vehicle> findVehiclesPurchasedInPeriod(@Param("companyId") UUID companyId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);
}