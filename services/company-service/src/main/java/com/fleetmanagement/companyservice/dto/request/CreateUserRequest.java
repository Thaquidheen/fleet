// CreateUserRequest.java (for bulk operations)
package com.fleetmanagement.companyservice.dto.request;

import com.fleetmanagement.companyservice.domain.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    private String phoneNumber;
    private String password; // Optional for bulk operations
    private UserRole role;
    private String employeeId;
    private String department;
    private String timezone;
    private String language;
}
