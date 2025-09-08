package com.fleetmanagement.userservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverAssignmentRequest {

    @NotNull(message = "Driver ID is required")
    private UUID driverId;

    @NotNull(message = "Vehicle ID is required")
    private UUID vehicleId;

    private LocalDateTime assignmentStart;
    private LocalDateTime assignmentEnd;
    private String notes;
}