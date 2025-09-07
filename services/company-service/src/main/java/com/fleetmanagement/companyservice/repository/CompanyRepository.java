package com.fleetmanagement.companyservice.repository;

import com.fleetmanagement.companyservice.domain.entity.Company;
import com.fleetmanagement.companyservice.domain.enums.CompanyStatus;
import com.fleetmanagement.companyservice.domain.enums.SubscriptionPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {

    // Basic find methods
    Optional<Company> findByName(String name);

    Optional<Company> findBySubdomain(String subdomain);

    Optional<Company> findByEmail(String email);

    // Existence checks
    boolean existsByName(String name);

    boolean existsBySubdomain(String subdomain);

    boolean existsByEmail(String email);

    // Status-based queries
    List<Company> findByStatus(CompanyStatus status);

    Page<Company> findByStatus(CompanyStatus status, Pageable pageable);

    // Subscription-based queries
    List<Company> findBySubscriptionPlan(SubscriptionPlan plan);

    Page<Company> findBySubscriptionPlan(SubscriptionPlan plan, Pageable pageable);

    // Trial companies
    @Query("SELECT c FROM Company c WHERE c.status = 'TRIAL' AND c.trialEndDate <= :date")
    List<Company> findExpiredTrialCompanies(@Param("date") LocalDate date);

    @Query("SELECT c FROM Company c WHERE c.status = 'TRIAL' AND c.trialEndDate BETWEEN :startDate AND :endDate")
    List<Company> findTrialCompaniesExpiringBetween(@Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    // Active companies
    @Query("SELECT c FROM Company c WHERE c.status = 'ACTIVE'")
    List<Company> findActiveCompanies();

    @Query("SELECT c FROM Company c WHERE c.status = 'ACTIVE'")
    Page<Company> findActiveCompanies(Pageable pageable);

    // Search queries
    @Query("SELECT c FROM Company c WHERE " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.industry) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Company> searchCompanies(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Industry-based queries
    List<Company> findByIndustry(String industry);

    Page<Company> findByIndustry(String industry, Pageable pageable);

    // Multi-condition queries
    @Query("SELECT c FROM Company c WHERE c.status = :status AND c.subscriptionPlan = :plan")
    List<Company> findByStatusAndSubscriptionPlan(@Param("status") CompanyStatus status,
                                                  @Param("plan") SubscriptionPlan plan);

    // Statistics queries
    @Query("SELECT COUNT(c) FROM Company c WHERE c.status = :status")
    long countByStatus(@Param("status") CompanyStatus status);

    @Query("SELECT COUNT(c) FROM Company c WHERE c.subscriptionPlan = :plan")
    long countBySubscriptionPlan(@Param("plan") SubscriptionPlan plan);

    @Query("SELECT c.industry, COUNT(c) FROM Company c GROUP BY c.industry")
    List<Object[]> countCompaniesByIndustry();

    @Query("SELECT c.subscriptionPlan, COUNT(c) FROM Company c GROUP BY c.subscriptionPlan")
    List<Object[]> countCompaniesBySubscriptionPlan();

    // Update operations
    @Modifying
    @Query("UPDATE Company c SET c.status = :status WHERE c.id = :companyId")
    void updateCompanyStatus(@Param("companyId") UUID companyId, @Param("status") CompanyStatus status);

    @Modifying
    @Query("UPDATE Company c SET c.subscriptionPlan = :plan, c.maxUsers = :maxUsers, c.maxVehicles = :maxVehicles WHERE c.id = :companyId")
    void updateSubscriptionPlan(@Param("companyId") UUID companyId,
                                @Param("plan") SubscriptionPlan plan,
                                @Param("maxUsers") Integer maxUsers,
                                @Param("maxVehicles") Integer maxVehicles);

    @Modifying
    @Query("UPDATE Company c SET c.currentUserCount = c.currentUserCount + 1 WHERE c.id = :companyId")
    void incrementUserCount(@Param("companyId") UUID companyId);

    @Modifying
    @Query("UPDATE Company c SET c.currentUserCount = c.currentUserCount - 1 WHERE c.id = :companyId AND c.currentUserCount > 0")
    void decrementUserCount(@Param("companyId") UUID companyId);

    @Modifying
    @Query("UPDATE Company c SET c.currentVehicleCount = c.currentVehicleCount + 1 WHERE c.id = :companyId")
    void incrementVehicleCount(@Param("companyId") UUID companyId);

    @Modifying
    @Query("UPDATE Company c SET c.currentVehicleCount = c.currentVehicleCount - 1 WHERE c.id = :companyId AND c.currentVehicleCount > 0")
    void decrementVehicleCount(@Param("companyId") UUID companyId);

    // Custom validation queries
    @Query("SELECT CASE WHEN c.currentUserCount < c.maxUsers THEN true ELSE false END FROM Company c WHERE c.id = :companyId")
    boolean canAddUser(@Param("companyId") UUID companyId);

    @Query("SELECT CASE WHEN c.currentVehicleCount < c.maxVehicles THEN true ELSE false END FROM Company c WHERE c.id = :companyId")
    boolean canAddVehicle(@Param("companyId") UUID companyId);

    // Timezone and localization
    List<Company> findByTimezone(String timezone);

    List<Company> findByLanguage(String language);

    // Date range queries
    @Query("SELECT c FROM Company c WHERE c.createdAt BETWEEN :startDate AND :endDate")
    List<Company> findCompaniesCreatedBetween(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    // Custom business queries
    @Query("SELECT c FROM Company c WHERE c.status = 'ACTIVE' AND c.currentUserCount > :minUsers")
    List<Company> findActiveCompaniesWithMinUsers(@Param("minUsers") Integer minUsers);

    @Query("SELECT c FROM Company c WHERE c.status = 'ACTIVE' AND c.currentVehicleCount > :minVehicles")
    List<Company> findActiveCompaniesWithMinVehicles(@Param("minVehicles") Integer minVehicles);
}