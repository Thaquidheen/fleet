package com.fleetmanagement.vehicleservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@FeignClient(
        name = "company-service",
        path = "/api/companies",
        fallback = CompanyServiceClientFallback.class
)
public interface CompanyServiceClient {

    @GetMapping("/{companyId}/vehicle-limits/validate")
    ResponseEntity<CanAddVehicleResponse> canAddVehicle(@PathVariable UUID companyId);

    @PostMapping("/{companyId}/vehicles/increment")
    ResponseEntity<Void> incrementVehicleCount(@PathVariable UUID companyId);

    @PostMapping("/{companyId}/vehicles/decrement")
    ResponseEntity<Void> decrementVehicleCount(@PathVariable UUID companyId);

    @GetMapping("/{companyId}/validation")
    ResponseEntity<CompanyValidationResponse> validateCompanyLimits(@PathVariable UUID companyId);

    @GetMapping("/{companyId}/subscription")
    ResponseEntity<CompanySubscriptionResponse> getCompanySubscription(@PathVariable UUID companyId);

    @GetMapping("/{companyId}")
    ResponseEntity<CompanyResponse> getCompanyById(@PathVariable UUID companyId);

    // ===== INNER DTO CLASSES =====

    class CanAddVehicleResponse {
        private boolean canAdd;
        private String reason;
        private int currentVehicles;
        private int maxVehicles;
        private int remainingSlots;

        public CanAddVehicleResponse() {}

        public boolean isCanAdd() { return canAdd; }
        public void setCanAdd(boolean canAdd) { this.canAdd = canAdd; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }

        public int getCurrentVehicles() { return currentVehicles; }
        public void setCurrentVehicles(int currentVehicles) { this.currentVehicles = currentVehicles; }

        public int getMaxVehicles() { return maxVehicles; }
        public void setMaxVehicles(int maxVehicles) { this.maxVehicles = maxVehicles; }

        public int getRemainingSlots() { return remainingSlots; }
        public void setRemainingSlots(int remainingSlots) { this.remainingSlots = remainingSlots; }
    }

    class CompanyValidationResponse {
        private UUID companyId;
        private boolean canAddVehicle;
        private String message;
        private int currentVehicleCount;
        private int maxVehicleLimit;
        private String subscriptionPlan;

        public CompanyValidationResponse() {}

        public UUID getCompanyId() { return companyId; }
        public void setCompanyId(UUID companyId) { this.companyId = companyId; }

        public boolean isCanAddVehicle() { return canAddVehicle; }
        public void setCanAddVehicle(boolean canAddVehicle) { this.canAddVehicle = canAddVehicle; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public int getCurrentVehicleCount() { return currentVehicleCount; }
        public void setCurrentVehicleCount(int currentVehicleCount) { this.currentVehicleCount = currentVehicleCount; }

        public int getMaxVehicleLimit() { return maxVehicleLimit; }
        public void setMaxVehicleLimit(int maxVehicleLimit) { this.maxVehicleLimit = maxVehicleLimit; }

        public String getSubscriptionPlan() { return subscriptionPlan; }
        public void setSubscriptionPlan(String subscriptionPlan) { this.subscriptionPlan = subscriptionPlan; }
    }

    class CompanySubscriptionResponse {
        private UUID companyId;
        private String subscriptionPlan;
        private String billingModel;
        private int maxVehicles;
        private int maxUsers;
        private int currentVehicleCount;
        private int currentUserCount;
        private boolean active;
        private java.time.LocalDate subscriptionStartDate;
        private java.time.LocalDate subscriptionEndDate;

        public CompanySubscriptionResponse() {}

        public UUID getCompanyId() { return companyId; }
        public void setCompanyId(UUID companyId) { this.companyId = companyId; }

        public String getSubscriptionPlan() { return subscriptionPlan; }
        public void setSubscriptionPlan(String subscriptionPlan) { this.subscriptionPlan = subscriptionPlan; }

        public String getBillingModel() { return billingModel; }
        public void setBillingModel(String billingModel) { this.billingModel = billingModel; }

        public int getMaxVehicles() { return maxVehicles; }
        public void setMaxVehicles(int maxVehicles) { this.maxVehicles = maxVehicles; }

        public int getMaxUsers() { return maxUsers; }
        public void setMaxUsers(int maxUsers) { this.maxUsers = maxUsers; }

        public int getCurrentVehicleCount() { return currentVehicleCount; }
        public void setCurrentVehicleCount(int currentVehicleCount) { this.currentVehicleCount = currentVehicleCount; }

        public int getCurrentUserCount() { return currentUserCount; }
        public void setCurrentUserCount(int currentUserCount) { this.currentUserCount = currentUserCount; }

        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }

        public java.time.LocalDate getSubscriptionStartDate() { return subscriptionStartDate; }
        public void setSubscriptionStartDate(java.time.LocalDate subscriptionStartDate) { this.subscriptionStartDate = subscriptionStartDate; }

        public java.time.LocalDate getSubscriptionEndDate() { return subscriptionEndDate; }
        public void setSubscriptionEndDate(java.time.LocalDate subscriptionEndDate) { this.subscriptionEndDate = subscriptionEndDate; }
    }

    class CompanyResponse {
        private UUID id;
        private String name;
        private String status;
        private String subscriptionPlan;
        private int maxVehicles;
        private int currentVehicleCount;
        private boolean active;

        public CompanyResponse() {}

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getSubscriptionPlan() { return subscriptionPlan; }
        public void setSubscriptionPlan(String subscriptionPlan) { this.subscriptionPlan = subscriptionPlan; }

        public int getMaxVehicles() { return maxVehicles; }
        public void setMaxVehicles(int maxVehicles) { this.maxVehicles = maxVehicles; }

        public int getCurrentVehicleCount() { return currentVehicleCount; }
        public void setCurrentVehicleCount(int currentVehicleCount) { this.currentVehicleCount = currentVehicleCount; }

        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }
}