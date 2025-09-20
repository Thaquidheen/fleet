

package com.fleetmanagement.deviceservice.repository;

import com.fleetmanagement.deviceservice.domain.entity.*;
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
 * Sensor Subscription Repository
 */
@Repository
interface SensorSubscriptionRepository extends JpaRepository<SensorSubscription, UUID> {

    /**
     * Find subscriptions by device sensor
     */
    List<SensorSubscription> findByDeviceSensor(DeviceSensor deviceSensor);

    /**
     * Find active subscriptions by device sensor
     */
    Optional<SensorSubscription> findByDeviceSensorAndIsActive(DeviceSensor deviceSensor, Boolean isActive);

    /**
     * Find subscriptions by company
     */
    Page<SensorSubscription> findByCompanyId(UUID companyId, Pageable pageable);

    /**
     * Find active subscriptions by company
     */
    List<SensorSubscription> findByCompanyIdAndIsActive(UUID companyId, Boolean isActive);

    /**
     * Find subscriptions ending soon
     */
    @Query("SELECT ss FROM SensorSubscription ss WHERE ss.isActive = true AND " +
            "ss.billingCycleEnd BETWEEN :startDate AND :endDate")
    List<SensorSubscription> findSubscriptionsEndingSoon(@Param("startDate") LocalDateTime startDate,
                                                         @Param("endDate") LocalDateTime endDate);

    /**
     * Calculate total monthly revenue for a company
     */
    @Query("SELECT SUM(ss.monthlyPrice) FROM SensorSubscription ss WHERE " +
            "ss.companyId = :companyId AND ss.isActive = true")
    Double calculateMonthlyRevenue(@Param("companyId") UUID companyId);

    /**
     * Find subscriptions for billing
     */
    @Query("SELECT ss FROM SensorSubscription ss WHERE ss.isActive = true AND " +
            "ss.billingCycleEnd <= :currentDate AND ss.autoRenewal = true")
    List<SensorSubscription> findSubscriptionsForBilling(@Param("currentDate") LocalDateTime currentDate);
}