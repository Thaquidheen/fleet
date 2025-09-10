package com.fleetmanagement.vehicleservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@FeignClient(name = "company-service", url = "${app.services.company-service.url:http://localhost:8083}",
        path = "/api/companies", fallback = CompanyServiceClientFallback.class)
public interface CompanyServiceClient {

    @GetMapping("/{companyId}/validate")
    ResponseEntity<CompanyValidationResponse> validateCompanyLimits(@PathVariable("companyId") UUID companyId);

    @PostMapping("/{companyId}/vehicles/increment")
    ResponseEntity<Void> incrementVehicleCount(@PathVariable("companyId") UUID companyId);

    @PostMapping("/{companyId}/vehicles/decrement")
    ResponseEntity<Void> decrementVehicleCount(@PathVariable("companyId") UUID companyId);

    @GetMapping("/{companyId}/vehicles/can-add")
    ResponseEntity<CanAddVehicleResponse> canAddVehicle(@PathVariable("companyId") UUID companyId);

    @GetMapping("/{companyId}/subscription")
    ResponseEntity<CompanySubscriptionResponse> getCompanySubscription(@PathVariable("companyId") UUID companyId);

    @GetMapping("/{companyId}")
    ResponseEntity<CompanyResponse> getCompanyById(@PathVariable("companyId") UUID companyId);

    // DTOs for Company Service Communication
    class CompanyValidationResponse {
        private UUID companyId;
        private String subscriptionPlan;
        private int currentVehicles;
        private int maxVehicles;
        private int availableSlots;
        private boolean canAddVehicle;
        private String message;

        // Constructors, getters, and setters
        public CompanyValidationResponse() {}

        public UUID getCompanyId() { return companyId; }
        public void setCompanyId(UUID companyId) { this.companyId = companyId; }
        public String getSubscriptionPlan() { return subscriptionPlan; }
        public void setSubscriptionPlan(String subscriptionPlan) { this.subscriptionPlan = subscriptionPlan; }
        public int getCurrentVehicles() { return currentVehicles; }
        public void setCurrentVehicles(int currentVehicles) { this.currentVehicles = currentVehicles; }
        public int getMaxVehicles() { return maxVehicles; }
        public void setMaxVehicles(int maxVehicles) { this.maxVehicles = maxVehicles; }
        public int getAvailableSlots() { return availableSlots; }
        public void setAvailableSlots(int availableSlots) { this.availableSlots = availableSlots; }
        public boolean isCanAddVehicle() { return canAddVehicle; }
        public void setCanAddVehicle(boolean canAddVehicle) { this.canAddVehicle = canAddVehicle; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    class CanAddVehicleResponse {
        private boolean canAdd;
        private String reason;
        private int remainingSlots;
        private String subscriptionPlan;

        // Constructors, getters, and setters
        public CanAddVehicleResponse() {}

        public boolean isCanAdd() { return canAdd; }
        public void setCanAdd(boolean canAdd) { this.canAdd = canAdd; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public int getRemainingSlots() { return remainingSlots; }
        public void setRemainingSlots(int remainingSlots) { this.remainingSlots = remainingSlots; }
        public String getSubscriptionPlan() { return subscriptionPlan; }
        public void setSubscriptionPlan(String subscriptionPlan) { this.subscriptionPlan = subscriptionPlan; }
    }

    class CompanySubscriptionResponse {
        private UUID companyId;
        private String subscriptionPlan;
        private int maxVehicles;
        private int currentVehicleCount;
        private boolean isActive;

        // Constructors, getters, and setters
        public CompanySubscriptionResponse() {}

        public UUID getCompanyId() { return companyId; }
        public void setCompanyId(UUID companyId) { this.companyId = companyId; }
        public String getSubscriptionPlan() { return subscriptionPlan; }
        public void setSubscriptionPlan(String subscriptionPlan) { this.subscriptionPlan = subscriptionPlan; }
        public int getMaxVehicles() { return maxVehicles; }
        public void setMaxVehicles(int maxVehicles) { this.maxVehicles = maxVehicles; }
        public int getCurrentVehicleCount() { return currentVehicleCount; }
        public void setCurrentVehicleCount(int currentVehicleCount) { this.currentVehicleCount = currentVehicleCount; }
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
    }

    class CompanyResponse {
        private UUID id;
        private String name;
        private String subscriptionPlan;
        private String status;

        // Constructors, getters, and setters
        public CompanyResponse() {}

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSubscriptionPlan() { return subscriptionPlan; }
        public void setSubscriptionPlan(String subscriptionPlan) { this.subscriptionPlan = subscriptionPlan; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}