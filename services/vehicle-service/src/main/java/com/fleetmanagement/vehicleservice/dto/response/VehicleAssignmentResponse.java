package com.fleetmanagement.vehicleservice.dto.response;

import com.fleetmanagement.vehicleservice.domain.enums.AssignmentType;
import com.fleetmanagement.vehicleservice.domain.enums.AssignmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

/**
 * Vehicle Assignment Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VehicleAssignmentResponse {

    private UUID id;

    private UUID vehicleId;

    private String vehicleName;

    private String vehicleLicensePlate;

    private String vehicleMake;

    private String vehicleModel;

    private UUID driverId;

    private String driverName;

    private String driverEmail;

    private String driverPhone;

    private String driverLicenseNumber;

    private AssignmentType assignmentType;

    private AssignmentStatus status;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalTime shiftStartTime;

    private LocalTime shiftEndTime;

    private String notes;

    private Map<String, Object> restrictions;

    private UUID companyId;

    private UUID assignedBy;

    private String assignedByName;

    private UUID updatedBy;

    private String updatedByName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Computed fields
    private boolean isActive;

    private boolean isOverdue;

    private boolean isExpiringSoon;

    private int daysRemaining;

    private int daysOverdue;

    private LocalDateTime lastCheckIn;

    private LocalDateTime lastCheckOut;

    private boolean isCheckedIn;

    // Usage statistics
    private int totalDaysAssigned;

    private int totalMilesDriven;

    private double averageDailyMiles;
}