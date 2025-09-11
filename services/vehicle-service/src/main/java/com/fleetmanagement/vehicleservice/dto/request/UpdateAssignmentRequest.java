package com.fleetmanagement.vehicleservice.dto.request;

import com.fleetmanagement.vehicleservice.domain.enums.AssignmentStatus;
import com.fleetmanagement.vehicleservice.domain.enums.AssignmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

/**
 * Update Assignment Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAssignmentRequest {

    private LocalDate startDate;

    private LocalDate endDate;

    private AssignmentType assignmentType;

    private AssignmentStatus status;

    private LocalTime shiftStartTime;

    private LocalTime shiftEndTime;

    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;

//    private Map<String, Object> restrictions;
}