package com.fleetmanagement.userservice.dto.response;

import com.fleetmanagement.userservice.domain.enums.UserStatus;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponse {

    private UUID id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private UserStatus status;
    private UUID companyId;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private boolean isAvailable;
    private UUID currentVehicleId;
    private String currentVehicleName;
    private Integer totalAssignments;
    private LocalDateTime lastAssignmentDate;
    private Double averageRating;
    private Integer totalTrips;
    private String licenseNumber;
    private LocalDateTime licenseExpiryDate;
}