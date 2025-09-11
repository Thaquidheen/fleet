package com.fleetmanagement.vehicleservice.dto.request;

import com.fleetmanagement.vehicleservice.domain.enums.GroupType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVehicleGroupRequest {

    @NotBlank(message = "Group name is required")
    @Size(max = 100, message = "Group name cannot exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private UUID parentGroupId;

    // FIXED: Changed from String to GroupType enum
    private GroupType groupType;

    // FIXED: Added missing fields that were causing compilation errors
    private Integer sortOrder;

    @Min(value = 1, message = "Max vehicles must be at least 1")
    private Integer maxVehicles;

    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;

    private UUID managerId;

    private Map<String, Object> customFields;

    // Explicit getters for fields that were causing compilation errors
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Integer getMaxVehicles() { return maxVehicles; }
    public void setMaxVehicles(Integer maxVehicles) { this.maxVehicles = maxVehicles; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public UUID getManagerId() { return managerId; }
    public void setManagerId(UUID managerId) { this.managerId = managerId; }

    public Map<String, Object> getCustomFields() { return customFields; }
    public void setCustomFields(Map<String, Object> customFields) { this.customFields = customFields; }
}
