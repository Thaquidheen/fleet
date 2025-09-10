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
    private UUID companyId;
    private String subscriptionPlan;
    private int currentUsers;
    private int maxUsers;
    private int availableSlots;
    private boolean canAddUser;
    private String message;

    // Explicit getters to fix compilation errors
    public int getCurrentUsers() { return currentUsers; }
    public int getMaxUsers() { return maxUsers; }
    public int getAvailableSlots() { return availableSlots; }
    public String getSubscriptionPlan() { return subscriptionPlan; }
    public boolean isCanAddUser() { return canAddUser; }
}