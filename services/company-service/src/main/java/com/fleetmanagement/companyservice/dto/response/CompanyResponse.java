package com.fleetmanagement.companyservice.dto.response;

import com.fleetmanagement.companyservice.domain.enums.CompanyStatus;
import com.fleetmanagement.companyservice.domain.enums.SubscriptionPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    // Subscription details
    private Integer maxUsers;
    private Integer maxVehicles;
    private Integer currentUserCount;
    private Integer currentVehicleCount;
    private LocalDate trialEndDate;

    // Contact person information
    private String contactPersonName;
    private String contactPersonTitle;
    private String contactPersonEmail;
    private String contactPersonPhone;

    // Usage statistics
    private Double userUtilizationPercentage;
    private Double vehicleUtilizationPercentage;
    private boolean canAddUser;
    private boolean canAddVehicle;

    // Audit fields
    private UUID createdBy;
    private UUID updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Computed fields for frontend
    private boolean isActive;
    private boolean isTrial;
    private boolean isExpired;
    private boolean isNearingExpiry; // Within 7 days of trial end
    private Integer daysUntilExpiry;
    private String subscriptionDisplayName;
    private String statusDisplayName;

    // Helper methods for computed fields
    public boolean isActive() {
        return status == CompanyStatus.ACTIVE;
    }

    public boolean isTrial() {
        return status == CompanyStatus.TRIAL;
    }

    public boolean isExpired() {
        return status == CompanyStatus.TRIAL && trialEndDate != null && trialEndDate.isBefore(LocalDate.now());
    }

    public boolean isNearingExpiry() {
        return status == CompanyStatus.TRIAL && trialEndDate != null &&
                trialEndDate.isBefore(LocalDate.now().plusDays(8)) && trialEndDate.isAfter(LocalDate.now());
    }

    public Integer getDaysUntilExpiry() {
        if (status == CompanyStatus.TRIAL && trialEndDate != null) {
            return (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), trialEndDate);
        }
        return null;
    }

    public String getSubscriptionDisplayName() {
        return subscriptionPlan != null ? subscriptionPlan.getDisplayName() : null;
    }

    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : null;
    }

    public Double getUserUtilizationPercentage() {
        if (maxUsers != null && maxUsers > 0 && currentUserCount != null) {
            return (currentUserCount.doubleValue() / maxUsers.doubleValue()) * 100.0;
        }
        return 0.0;
    }

    public Double getVehicleUtilizationPercentage() {
        if (maxVehicles != null && maxVehicles > 0 && currentVehicleCount != null) {
            return (currentVehicleCount.doubleValue() / maxVehicles.doubleValue()) * 100.0;
        }
        return 0.0;
    }

    public boolean isCanAddUser() {
        return currentUserCount != null && maxUsers != null && currentUserCount < maxUsers;
    }

    public boolean isCanAddVehicle() {
        return currentVehicleCount != null && maxVehicles != null && currentVehicleCount < maxVehicles;
    }
}