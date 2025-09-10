package com.fleetmanagement.companyservice.dto.response;

import com.fleetmanagement.companyservice.domain.enums.CompanyStatus;
import com.fleetmanagement.companyservice.domain.enums.SubscriptionPlan;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponse {
    private UUID id;
    private String name;
    private String subdomain;
    private String industry;
    private String phone;
    private String email;
    private String website;
    private String address;
    private String logoUrl;
    private CompanyStatus status;
    private SubscriptionPlan subscriptionPlan;
    private String timezone;
    private String language;
    private String notes;
    private String contactPersonName;
    private String contactPersonTitle;
    private String contactPersonEmail;
    private String contactPersonPhone;
    private int currentUserCount;
    private int maxUsers;
    private int currentVehicleCount;
    private int maxVehicles;
    private LocalDate trialEndDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;
}