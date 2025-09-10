package com.fleetmanagement.vehicleservice.dto.response;

import com.fleetmanagement.vehicleservice.domain.enums.GroupType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Vehicle Group Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VehicleGroupResponse {

    private UUID id;

    private String name;

    private String description;

    private UUID parentGroupId;

    private String parentGroupName;

    private GroupType groupType;

    private Integer maxVehicles;

    private Integer currentVehicleCount;

    private String location;

    private UUID managerId;

    private String managerName;

    private String managerEmail;

    private Map<String, Object> customFields;

    private Integer sortOrder;

    private UUID companyId;

    private UUID createdBy;

    private UUID updatedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Hierarchical information
    private List<VehicleGroupResponse> childGroups;

    private int totalChildGroups;

    private int totalVehiclesInHierarchy;

    // Vehicle assignments
    private List<VehicleResponse> vehicles;

    private boolean hasVehicles;

    private boolean hasChildGroups;

    private boolean canAddVehicles;

    private boolean canAddChildGroups;

    // Computed fields
    private int level; // Depth in hierarchy (0 = root level)

    private String fullPath; // Complete path from root: "Dept A > Region B > Group C"

    private boolean isRoot;

    private boolean isLeaf;

    // Statistics
    private int activeVehicles;

    private int maintenanceVehicles;

    private double utilizationRate;
}