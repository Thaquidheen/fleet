package com.fleetmanagement.vehicleservice.dto.request;

import com.fleetmanagement.vehicleservice.domain.enums.GroupType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.util.UUID;

/**
 * Vehicle Group Search Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleGroupSearchRequest {

    private String name;
    private String description;
    private GroupType groupType;
    private UUID parentGroupId;
    private UUID managerId;
    private String location;
    private Boolean hasVehicles;
    private Boolean hasChildGroups;
    private Boolean isActive;

    // Pagination
    private int page = 0;
    private int size = 20;
    private String sortBy = "name";
    private String sortDirection = "asc";
}