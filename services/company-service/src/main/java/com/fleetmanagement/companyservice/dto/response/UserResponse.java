// UserResponse.java (for company service)
package com.fleetmanagement.companyservice.dto.response;

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
    private String fullName;
    private String phoneNumber;
    private String role;
    private String status;
    private UUID companyId;
    private String employeeId;
    private String department;
    private Boolean emailVerified;
    private LocalDateTime lastLogin;
    private String profileImageUrl;
    private String timezone;
    private String language;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}