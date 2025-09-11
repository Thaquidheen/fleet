package com.fleetmanagement.vehicleservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateVehicleGroupRequest {

    @Size(max = 100, message = "Group name must not exceed 100 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private UUID parentGroupId;

    @Min(value = 1, message = "Max vehicles must be at least 1")
    private Integer maxVehicles;

    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;

    private UUID managerId;

//    private Map<String, Object> customFields;

    private Integer sortOrder;

    // FIXED: Added missing isActive field that was causing compilation errors
    private Boolean isActive;

    // Explicit getter for isActive field
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}