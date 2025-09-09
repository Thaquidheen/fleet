package com.fleetmanagement.vehicleservice.service.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Company Service Client
 *
 * Feign client for integrating with the Company Service to:
 * - Validate company subscription limits
 * - Check vehicle quotas
 * - Retrieve company information
 */
@FeignClient(
        name = "company-service",
        url = "${feign.client.config.company-service.url:http://localhost:8083/company-service}",
        fallback = CompanyServiceClientFallback.class
)
public interface CompanyServiceClient {

    /**
     * Get company information
     */
    @GetMapping("/api/companies/{companyId}")
    CompanyResponse getCompany(@PathVariable("companyId") UUID companyId);

    /**
     * Validate vehicle limit for company
     */
    @GetMapping("/api/companies/{companyId}/validate-vehicle-limit")
    VehicleLimitValidationResponse validateVehicleLimit(@PathVariable("companyId") UUID companyId);

    /**
     * Check if company can add vehicle
     */
    @GetMapping("/api/companies/{companyId}/can-add-vehicle")
    CanAddVehicleResponse canAddVehicle(@PathVariable("companyId") UUID companyId);

    /**
     * Increment vehicle count for company
     */
    @PostMapping("/api/companies/{companyId}/increment-vehicle-count")
    void incrementVehicleCount(@PathVariable("companyId") UUID companyId);

    /**
     * Decrement vehicle count for company
     */
    @PostMapping("/api/companies/{companyId}/decrement-vehicle-count")
    void decrementVehicleCount(@PathVariable("companyId") UUID companyId);

    /**
     * Get company subscription details
     */
    @GetMapping("/api/companies/{companyId}/subscription")
    CompanySubscriptionResponse getCompanySubscription(@PathVariable("companyId") UUID companyId);

    // Response DTOs for Company Service integration

    class CompanyResponse {
        private UUID id;
        private String name;
        private String status;
        private String subscriptionPlan;
        private Integer maxVehicles;
        private Integer currentVehicleCount;
        private boolean isActive;

        // Constructors, getters, and setters
        public CompanyResponse() {}

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getSubscriptionPlan() { return subscriptionPlan; }
        public void setSubscriptionPlan(String subscriptionPlan) { this.subscriptionPlan = subscriptionPlan; }
        public Integer getMaxVehicles() { return maxVehicles; }
        public void setMaxVehicles(Integer maxVehicles) { this.maxVehicles = maxVehicles; }
        public Integer getCurrentVehicleCount() { return currentVehicleCount; }
        public void setCurrentVehicleCount(Integer currentVehicleCount) { this.currentVehicleCount = currentVehicleCount; }
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
    }

    class VehicleLimitValidationResponse {
        private boolean isValid;
        private boolean canAddVehicle;
        private Integer currentCount;
        private Integer maxAllowed;
        private String message;

        // Constructors, getters, and setters
        public VehicleLimitValidationResponse() {}

        public boolean isValid() { return isValid; }
        public void setValid(boolean valid) { isValid = valid; }
        public boolean isCanAddVehicle() { return canAddVehicle; }
        public void setCanAddVehicle(boolean canAddVehicle) { this.canAddVehicle = canAddVehicle; }
        public Integer getCurrentCount() { return currentCount; }
        public void setCurrentCount(Integer currentCount) { this.currentCount = currentCount; }
        public Integer getMaxAllowed() { return maxAllowed; }
        public void setMaxAllowed(Integer maxAllowed) { this.maxAllowed = maxAllowed; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    class CanAddVehicleResponse {
        private boolean canAdd;
        private String reason;
        private Integer remainingSlots;

        // Constructors, getters, and setters
        public CanAddVehicleResponse() {}

        public boolean isCanAdd() { return canAdd; }
        public void setCanAdd(boolean canAdd) { this.canAdd = canAdd; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public Integer getRemainingSlots() { return remainingSlots; }
        public void setRemainingSlots(Integer remainingSlots) { this.remainingSlots = remainingSlots; }
    }

    class CompanySubscriptionResponse {
        private UUID companyId;
        private String subscriptionPlan;
        private String billingModel;
        private Integer maxVehicles;
        private Integer maxUsers;
        private Integer currentVehicleCount;
        private Integer currentUserCount;
        private boolean isActive;
        private java.time.LocalDate subscriptionStartDate;
        private java.time.LocalDate subscriptionEndDate;
        private java.time.LocalDate trialEndDate;

        // Constructors, getters, and setters
        public CompanySubscriptionResponse() {}

        public UUID getCompanyId() { return companyId; }
        public void setCompanyId(UUID companyId) { this.companyId = companyId; }
        public String getSubscriptionPlan() { return subscriptionPlan; }
        public void setSubscriptionPlan(String subscriptionPlan) { this.subscriptionPlan = subscriptionPlan; }
        public String getBillingModel() { return billingModel; }
        public void setBillingModel(String billingModel) { this.billingModel = billingModel; }
        public Integer getMaxVehicles() { return maxVehicles; }
        public void setMaxVehicles(Integer maxVehicles) { this.maxVehicles = maxVehicles; }
        public Integer getMaxUsers() { return maxUsers; }
        public void setMaxUsers(Integer maxUsers) { this.maxUsers = maxUsers; }
        public Integer getCurrentVehicleCount() { return currentVehicleCount; }
        public void setCurrentVehicleCount(Integer currentVehicleCount) { this.currentVehicleCount = currentVehicleCount; }
        public Integer getCurrentUserCount() { return currentUserCount; }
        public void setCurrentUserCount(Integer currentUserCount) { this.currentUserCount = currentUserCount; }
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
        public java.time.LocalDate getSubscriptionStartDate() { return subscriptionStartDate; }
        public void setSubscriptionStartDate(java.time.LocalDate subscriptionStartDate) { this.subscriptionStartDate = subscriptionStartDate; }
        public java.time.LocalDate getSubscriptionEndDate() { return subscriptionEndDate; }
        public void setSubscriptionEndDate(java.time.LocalDate subscriptionEndDate) { this.subscriptionEndDate = subscriptionEndDate; }
        public java.time.LocalDate getTrialEndDate() { return trialEndDate; }
        public void setTrialEndDate(java.time.LocalDate trialEndDate) { this.trialEndDate = trialEndDate; }
    }
}