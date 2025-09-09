package com.fleetmanagement.companyservice.dto.request;

import com.fleetmanagement.companyservice.domain.enums.UserRole;
import com.fleetmanagement.companyservice.domain.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    private String phoneNumber;

    private UserRole role;
    private UserStatus status;

    private String employeeId;
    private String department;
    private String timezone;
    private String language;
    private String password; // For password updates
    private boolean active;
}