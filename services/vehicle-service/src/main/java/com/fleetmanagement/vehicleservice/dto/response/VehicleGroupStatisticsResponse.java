package com.fleetmanagement.vehicleservice.dto.response;

import com.fleetmanagement.vehicleservice.domain.enums.GroupType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Map;


/**
 * Vehicle Group Statistics Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VehicleGroupStatisticsResponse {
    private UUID companyId;
    private int totalActiveGroups;
    private int totalGroups;
    private int rootGroups;
    private int maxDepth;
    private int totalVehicles;
    private int assignedVehicles;
    private int unassignedVehicles;
    private Map<String, Integer> vehiclesByGroupType;
    private Map<String, Integer> vehiclesByGroup;
    private double averageVehiclesPerGroup;
    private double utilizationRate;
    private int groupsWithVehicles;
    private int emptyGroups;
    private Map<GroupType, Integer> groupsByType;        // Enables .groupsByType(...)

    private LocalDateTime generatedAt;
}