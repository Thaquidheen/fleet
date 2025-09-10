package com.fleetmanagement.vehicleservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Vehicle Statistics Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VehicleStatisticsResponse {

    // Basic counts
    private int totalVehicles;

    private int activeVehicles;

    private int maintenanceVehicles;

    private int retiredVehicles;

    private int assignedVehicles;

    private int unassignedVehicles;

    // Vehicle type breakdown
    private Map<String, Integer> vehiclesByType;

    private Map<String, Integer> vehiclesByCategory;

    private Map<String, Integer> vehiclesByStatus;

    private Map<String, Integer> vehiclesByFuelType;

    // Financial statistics
    private BigDecimal totalFleetValue;

    private BigDecimal averageVehicleValue;

    private BigDecimal totalInsuranceCost;

    private BigDecimal monthlyInsuranceCost;

    // Maintenance and compliance
    private int vehiclesRequiringMaintenance;

    private int vehiclesWithExpiringInsurance;

    private int vehiclesWithExpiringRegistration;

    private int vehiclesWithOverdueMaintenance;

    // Utilization metrics
    private double utilizationRate;

    private double averageMileage;

    private int totalMileage;

    // Age statistics
    private double averageVehicleAge;

    private int oldestVehicleYear;

    private int newestVehicleYear;

    // Group statistics
    private int totalGroups;

    private Map<String, Integer> vehiclesByGroup;

    // Alerts and warnings
    private int totalAlerts;

    private int criticalAlerts;

    private int warningAlerts;

    // Time-based statistics
    private Map<String, Integer> vehiclesAddedByMonth;

    private Map<String, Integer> maintenancesByMonth;

    // Subscription limits
    private int maxAllowedVehicles;

    private int remainingVehicleSlots;

    private String subscriptionPlan;

    private boolean canAddMoreVehicles;
}