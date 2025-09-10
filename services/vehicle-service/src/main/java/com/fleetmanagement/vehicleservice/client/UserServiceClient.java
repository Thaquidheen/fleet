package com.fleetmanagement.vehicleservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "user-service", url = "${app.services.user-service.url:http://localhost:8082}",
        path = "/api/users", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {

    @GetMapping("/{userId}/validate-driver")
    ResponseEntity<DriverValidationResponse> validateDriver(
            @PathVariable("userId") UUID userId,
            @RequestParam("companyId") UUID companyId);

    @GetMapping("/drivers/available")
    ResponseEntity<List<DriverResponse>> getAvailableDrivers(
            @RequestParam("companyId") UUID companyId);

    @PostMapping("/{userId}/assign-vehicle")
    ResponseEntity<Void> notifyDriverAssignment(
            @PathVariable("userId") UUID userId,
            @RequestBody DriverAssignmentNotification notification);

    @PostMapping("/{userId}/unassign-vehicle")
    ResponseEntity<Void> notifyDriverUnassignment(
            @PathVariable("userId") UUID userId,
            @RequestParam("vehicleId") UUID vehicleId);

    @GetMapping("/{userId}")
    ResponseEntity<UserResponse> getUserById(@PathVariable("userId") UUID userId);

    @GetMapping("/company/{companyId}/drivers")
    ResponseEntity<List<DriverResponse>> getCompanyDrivers(@PathVariable("companyId") UUID companyId);

    // DTOs for User Service Communication
    class DriverValidationResponse {
        private boolean isValid;
        private boolean isAvailable;
        private boolean isActiveDriver;
        private String unavailabilityReason;
        private UUID currentVehicleId;
        private java.time.LocalDateTime availableFrom;
        private List<String> validationErrors;

        // Constructors, getters, and setters
        public DriverValidationResponse() {}

        public boolean isValid() { return isValid; }
        public void setValid(boolean valid) { isValid = valid; }
        public boolean isAvailable() { return isAvailable; }
        public void setAvailable(boolean available) { isAvailable = available; }
        public boolean isActiveDriver() { return isActiveDriver; }
        public void setActiveDriver(boolean activeDriver) { isActiveDriver = activeDriver; }
        public String getUnavailabilityReason() { return unavailabilityReason; }
        public void setUnavailabilityReason(String unavailabilityReason) { this.unavailabilityReason = unavailabilityReason; }
        public UUID getCurrentVehicleId() { return currentVehicleId; }
        public void setCurrentVehicleId(UUID currentVehicleId) { this.currentVehicleId = currentVehicleId; }
        public java.time.LocalDateTime getAvailableFrom() { return availableFrom; }
        public void setAvailableFrom(java.time.LocalDateTime availableFrom) { this.availableFrom = availableFrom; }
        public List<String> getValidationErrors() { return validationErrors; }
        public void setValidationErrors(List<String> validationErrors) { this.validationErrors = validationErrors; }
    }

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

        // Constructors, getters, and setters
        public DriverResponse() {}

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
    }

    class UserResponse {
        private UUID id;
        private String username;
        private String firstName;
        private String lastName;
        private String role;
        private UUID companyId;

        // Constructors, getters, and setters
        public UserResponse() {}

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public UUID getCompanyId() { return companyId; }
        public void setCompanyId(UUID companyId) { this.companyId = companyId; }
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
