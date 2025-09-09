package com.fleetmanagement.companyservice.dto.request;

import com.fleetmanagement.companyservice.domain.enums.UserRole;
import com.fleetmanagement.companyservice.domain.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @Email(message = "Invalid email format")
    private String email;

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private UserRole role;
    private UserStatus status;
    private String employeeId;
    private String department;
    private String timezone;
    private String language;
    private boolean resetPassword; // If true, reset password and send email
}