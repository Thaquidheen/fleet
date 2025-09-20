package com.fleetmanagement.deviceservice.external.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;


/**
 * User Response DTO from User Service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private UUID id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String role;
    private Boolean isActive;

    private UUID companyId;
    private String companyName;

    // Driver-specific information
    private String licenseNumber;
    private String licenseClass;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime licenseExpiryDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime dateOfBirth;

    // Employment information
    private String employeeId;
    private String department;
    private String position;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime hireDate;

    // Contact information
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String address;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastLoginAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
