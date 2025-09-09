package com.fleetmanagement.userservice.dto.response;

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
public class CompanyValidationResponse {

    private boolean canAddUser;
    private boolean isValid;
    private int currentUsers;
    private int maxUsers;
    private int availableSlots;
    private String subscriptionPlan;
    private String message;
    private UUID companyId;
    private LocalDateTime validatedAt;

    // Additional validation fields
    private boolean subscriptionActive;
    private boolean companyActive;
    private String validationStatus; // VALID, LIMIT_EXCEEDED, INACTIVE, SUSPENDED
}