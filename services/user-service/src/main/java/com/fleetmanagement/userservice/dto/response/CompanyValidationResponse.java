package com.fleetmanagement.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyValidationResponse {
    private boolean canAddUser;
    private int currentUsers;
    private int maxUsers;
    private String subscriptionPlan;
    private String companyType;
    private String message;
}