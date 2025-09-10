package com.fleetmanagement.vehicleservice.dto.request;

import com.fleetmanagement.vehicleservice.domain.enums.AssignmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

/**
 * Assign Driver Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignDriverRequest {

    @NotNull(message = "Vehicle ID is required")
    private UUID vehicleId;

    @NotNull(message = "Driver ID is required")
    private UUID driverId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull(message = "Assignment type is required")
    private AssignmentType assignmentType;

    private LocalTime shiftStartTime;

    private LocalTime shiftEndTime;

    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;

    private Map<String, Object> restrictions;
}