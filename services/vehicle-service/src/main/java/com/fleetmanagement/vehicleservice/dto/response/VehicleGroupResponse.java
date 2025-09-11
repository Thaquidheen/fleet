package com.fleetmanagement.vehicleservice.dto.response;

import com.fleetmanagement.vehicleservice.domain.enums.GroupType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
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
    private UUID companyId;
    private UUID parentGroupId;
    private String parentGroupName;
    private GroupType groupType;
    private Integer maxVehicles;
    private Integer currentVehicleCount;
    private String location;
    private UUID managerId;
    private String managerName;
    private Map<String, Object> customFields;
    private Integer sortOrder;
    private Boolean isActive;
    private Integer level;
    private Boolean hasChildren;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;
}