package com.fleetmanagement.vehicleservice.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;

public interface UserServiceClient {
    @GetMapping("/drivers/available")
    List<DriverResponse> getAvailableDrivers(@RequestParam UUID companyId);

    @GetMapping("/drivers/company/{companyId}")
    List<DriverResponse> getCompanyDrivers(@PathVariable UUID companyId);


    @GetMapping("/{userId}/validate-driver")
    DriverValidationResponse validateDriver(@PathVariable UUID userId, @RequestParam UUID companyId);

    // Add these missing methods to match the fallback implementation
    @PostMapping("/{driverId}/assignments/notify")
    void notifyDriverAssignment(@PathVariable UUID driverId, @RequestBody DriverAssignmentNotification notification);

    @DeleteMapping("/{driverId}/assignments/{vehicleId}")
    void notifyDriverUnassignment(@PathVariable UUID driverId, @PathVariable UUID vehicleId);




    class DriverResponse {
        private UUID id;
        private String username;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private boolean isAvailable;
        private UUID currentVehicleId;
        private String employeeId;
        private String role;
        private String status;
        private UUID companyId;
        private String licenseNumber;
        private java.time.LocalDate licenseExpiryDate;
        private boolean isActive;
        private boolean isAvailableForAssignment;

        // Default constructor
        public DriverResponse() {}

        // Getters and setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public boolean isAvailable() { return isAvailable; }
        public void setAvailable(boolean available) { isAvailable = available; }
        public UUID getCurrentVehicleId() { return currentVehicleId; }
        public void setCurrentVehicleId(UUID currentVehicleId) { this.currentVehicleId = currentVehicleId; }
        public String getEmployeeId() { return employeeId; }
        public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
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

    class DriverAssignmentNotification {
        private UUID vehicleId;
        private String vehicleName;
        private String licensePlate;
        private UUID companyId;
        private java.time.LocalDate assignmentStartDate;
        private java.time.LocalDate assignmentEndDate;
        private String assignmentType; // Added field

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
    }


    class DriverValidationResponse {
        private boolean valid;
        private boolean available;
        private String message;
        private String unavailabilityReason;
        private List<String> validationErrors;
        // getters and setters ...
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getUnavailabilityReason() { return unavailabilityReason; }
        public void setUnavailabilityReason(String unavailabilityReason) { this.unavailabilityReason = unavailabilityReason; }
        public List<String> getValidationErrors() { return validationErrors; }
        public void setValidationErrors(List<String> validationErrors) { this.validationErrors = validationErrors; }
    }
    }

