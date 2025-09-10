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
    private UUID companyId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private UserRole role;
    private UserStatus status;
    private boolean active;
    private boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}