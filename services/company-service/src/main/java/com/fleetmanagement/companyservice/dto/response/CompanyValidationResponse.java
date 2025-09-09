package com.fleetmanagement.companyservice.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyValidationResponse {
    private boolean canAddUser;
    private int currentUsers;
    private int maxUsers;
    private int availableSlots;
    private String subscriptionPlan;
    private String message;
    private UUID companyId;
    private java.time.LocalDateTime validatedAt;
}