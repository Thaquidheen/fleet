package com.fleetmanagement.vehicleservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.UUID;

/**
 * Vehicle Group Hierarchy Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VehicleGroupHierarchyResponse {

    private UUID rootGroupId;
    private String rootGroupName;
    private List<VehicleGroupResponse> rootGroups;
    private int totalGroups;
    private int totalLevels;
    private int totalVehicles;
    private UUID companyId;
}
