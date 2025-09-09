package com.fleetmanagement.vehicleservice.service.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * User Service Client
 *
 * Feign client for integrating with the User Service to:
 * - Validate drivers
 * - Get driver information
 * - Check driver availability
 */
@FeignClient(
        name = "user-service",
        url = "${feign.client.config.user-service.url:http://localhost:8082/user-service}",
        fallback = UserServiceClientFallback.class
)
public interface UserServiceClient {

    /**
     * Get driver information
     */
    @GetMapping("/api/users/{driverId}")
    DriverResponse getDriver(@PathVariable("driverId") UUID driverId);

    /**
     * Validate if user is a driver and is active
     */
    @GetMapping("/api/users/{driverId}/validate-driver")
    DriverValidationResponse validateDriver(@PathVariable("driverId") UUID driverId,
                                            @RequestParam("companyId") UUID companyId);

    /**
     * Get available drivers (not currently assigned)
     */
    @GetMapping("/api/users/drivers/available")
    List<DriverResponse> getAvailableDrivers(@RequestParam("companyId") UUID companyId);

    /**
     * Notify user service of driver assignment
     */
    @PostMapping("/api/users/{driverId}/assign-vehicle")
    void notifyDriverAssignment(@PathVariable("driverId") UUID driverId,
                                @RequestBody DriverAssignmentNotification notification);

    /**
     * Notify user service of driver unassignment
     */
    @PostMapping("/api/users/{driverId}/unassign-vehicle")
    void notifyDriverUnassignment(@PathVariable("driverId") UUID driverId,
                                  @RequestParam("companyId") UUID companyId);

    // Response DTOs for User Service integration

    class DriverResponse {
        private UUID id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String fullName;
        private String phoneNumber;
        private String role;
        private String status;
        private UUID companyId;
        private String licenseNumber;
        private java.time.LocalDate licenseExpiryDate;
        private boolean isActive;
        private boolean isAvailableForAssignment;

        // Constructors, getters, and setters
        public DriverResponse() {}

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public UUID getCompanyId() { return companyId; }
        public void setCompanyId(UUID companyId) { this.companyId = companyId; }
        public String getLicenseNumber() { return licenseNumber; }
        public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
        public java.time.LocalDate getLicenseExpiryDate() { return licenseExpiryDate; }
        public void setLicenseExpiryDate(java.time.LocalDate licenseExpiryDate) { this.licenseExpiryDate = licenseExpiryDate; }
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
        public boolean isAvailableForAssignment() { return isAvailableForAssignment; }
        public void setAvailableForAssignment(boolean availableForAssignment) { isAvailableForAssignment = availableForAssignment; }
    }

    class DriverValidationResponse {
        private boolean isValid;
        private boolean isDriver;
        private boolean isActive;
        private boolean belongsToCompany;
        private String message;
        private List<String> validationErrors;

        // Constructors, getters, and setters
        public DriverValidationResponse() {}

        public boolean isValid() { return isValid; }
        public void setValid(boolean valid) { isValid = valid; }
        public boolean isDriver() { return isDriver; }
        public void setDriver(boolean driver) { isDriver = driver; }
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
        public boolean isBelongsToCompany() { return belongsToCompany; }
        public void setBelongsToCompany(boolean belongsToCompany) { this.belongsToCompany = belongsToCompany; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public List<String> getValidationErrors() { return validationErrors; }
        public void setValidationErrors(List<String> validationErrors) { this.validationErrors = validationErrors; }
    }

    class DriverAvailabilityResponse {
        private boolean isAvailable;
        private boolean isCurrentlyAssigned;
        private UUID currentVehicleId;
        private String unavailabilityReason;
        private java.time.LocalDateTime availableFrom;

        // Constructors, getters, and setters
        public DriverAvailabilityResponse() {}

        public boolean isAvailable() { return isAvailable; }
        public void setAvailable(boolean available) { isAvailable = available; }
        public boolean isCurrentlyAssigned() { return isCurrentlyAssigned; }
        public void setCurrentlyAssigned(boolean currentlyAssigned) { isCurrentlyAssigned = currentlyAssigned; }
        public UUID getCurrentVehicleId() { return currentVehicleId; }
        public void setCurrentVehicleId(UUID currentVehicleId) { this.currentVehicleId = currentVehicleId; }
        public String getUnavailabilityReason() { return unavailabilityReason; }
        public void setUnavailabilityReason(String unavailabilityReason) { this.unavailabilityReason = unavailabilityReason; }
        public java.time.LocalDateTime getAvailableFrom() { return availableFrom; }
        public void setAvailableFrom(java.time.LocalDateTime availableFrom) { this.availableFrom = availableFrom; }
    }

    class DriverAssignmentNotification {
        private UUID vehicleId;
        private String vehicleName;
        private String licensePlate;
        private UUID companyId;
        private java.time.LocalDate assignmentStartDate;
        private java.time.LocalDate assignmentEndDate;
        private String assignmentType;
        private String notes;

        // Constructors, getters, and setters
        public DriverAssignmentNotification() {}

        public UUID getVehicleId() { return vehicleId; }
        public void setVehicleId(UUID vehicleId) { this.vehicleId = vehicleId; }
        public String getVehicleName() { return vehicleName; }
        public void setVehicleName(String vehicleName) { this.vehicleName = vehicleName; }
        public String getLicensePlate() { return licensePlate; }
        public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
        public UUID getCompanyId() { return companyId; }
        public void setCompanyId(UUID companyId) { this.companyId = companyId; }
        public java.time.LocalDate getAssignmentStartDate() { return assignmentStartDate; }
        public void setAssignmentStartDate(java.time.LocalDate assignmentStartDate) { this.assignmentStartDate = assignmentStartDate; }
        public java.time.LocalDate getAssignmentEndDate() { return assignmentEndDate; }
        public void setAssignmentEndDate(java.time.LocalDate assignmentEndDate) { this.assignmentEndDate = assignmentEndDate; }
        public String getAssignmentType() { return assignmentType; }
        public void setAssignmentType(String assignmentType) { this.assignmentType = assignmentType; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
}

/**
 * Check if driver is available for assignment
 */
@GetMapping("/api/users/{driverId}/availability")
DriverAvailabilityResponse checkDriverAvailability(@PathVariable("driverId") UUID driverId,
                                                   @RequestParam("companyId") UUID companyId);

/**
 * Get all drivers for a company
 */
@GetMapping("/api/users/drivers")
List<DriverResponse> getDriversByCompany(@RequestParam("companyId") UUID