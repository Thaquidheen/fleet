// DriverValidationResponse.java
package com.fleetmanagement.userservice.dto.response;

import com.fleetmanagement.userservice.domain.enums.UserRole;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverValidationResponse {

    private boolean isValid;
    private boolean isAvailable;
    private boolean isActive;
    private UUID userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private UserRole role;
    private UUID currentVehicleId;
    private String message;
    private String reason;
    private boolean hasValidLicense;
    private boolean isEmailVerified;
    private boolean isAccountLocked;
}