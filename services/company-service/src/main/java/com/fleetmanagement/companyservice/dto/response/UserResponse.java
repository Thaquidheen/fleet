// UserResponse.java (for Company Service)
package com.fleetmanagement.companyservice.dto.response;

import com.fleetmanagement.companyservice.domain.enums.UserRole;
import com.fleetmanagement.companyservice.domain.enums.UserStatus;
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
public class UserResponse {
    private UUID id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private UserRole role;
    private UserStatus status;
    private UUID companyId;
    private String employeeId;
    private String department;
    private String timezone;
    private String language;
    private boolean emailVerified;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive;
    private int totalAssignments; // For drivers
    private LocalDateTime lastAssignmentDate; // For drivers
}