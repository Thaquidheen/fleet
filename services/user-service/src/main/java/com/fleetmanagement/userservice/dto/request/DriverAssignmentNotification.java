package com.fleetmanagement.userservice.dto.request;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverAssignmentNotification {

    @NotNull(message = "Vehicle ID is required")
    private UUID vehicleId;

    private String vehicleName;
    private String licensePlate;
    private String vehicleType;

    @NotNull(message = "Company ID is required")
    private UUID companyId;

    private LocalDate assignmentStartDate;
    private LocalDate assignmentEndDate;
    private String assignmentType;
    private String assignmentStatus;
    private String notes;

    // Additional fields for notification
    private String notificationType; // ASSIGNED, UNASSIGNED, REASSIGNED
    private UUID assignedBy;
    private String assignedByName;
    private boolean isTemporary;
    private String priority; // HIGH, MEDIUM, LOW
}