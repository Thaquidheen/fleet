package com.fleetmanagement.vehicleservice.domain.entity;

import com.fleetmanagement.vehicleservice.domain.enums.VehicleStatus;
import com.fleetmanagement.vehicleservice.domain.enums.VehicleType;
import com.fleetmanagement.vehicleservice.domain.enums.VehicleCategory;
import com.fleetmanagement.vehicleservice.domain.enums.FuelType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Vehicle Entity
 *
 * Represents a vehicle in the fleet management system.
 * Supports multi-tenant operation with company-scoped data.
 */
@Entity
@Table(name = "vehicles", indexes = {
        @Index(name = "idx_vehicles_company_id", columnList = "company_id"),
        @Index(name = "idx_vehicles_status", columnList = "status"),
        @Index(name = "idx_vehicles_type", columnList = "vehicle_type"),
        @Index(name = "idx_vehicles_current_driver", columnList = "current_driver_id"),
        @Index(name = "idx_vehicles_vin", columnList = "vin"),
        @Index(name = "idx_vehicles_license_plate", columnList = "license_plate")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    @NotNull(message = "Company ID is required")
    private UUID companyId;

    // Basic Information
    @Column(nullable = false, length = 255)
    @NotBlank(message = "Vehicle name is required")
    @Size(max = 255, message = "Vehicle name must not exceed 255 characters")
    private String name;

    @Column(length = 17, unique = true)
    @Size(min = 17, max = 17, message = "VIN must be exactly 17 characters")
    @Pattern(regexp = "^[A-HJ-NPR-Z0-9]{17}$",
            message = "VIN must contain only valid characters (no I, O, Q)")
    private String vin;

    @Column(name = "license_plate", nullable = false, length = 20)
    @NotBlank(message = "License plate is required")
    @Size(max = 20, message = "License plate must not exceed 20 characters")
    private String licensePlate;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Make is required")
    @Size(max = 100, message = "Make must not exceed 100 characters")
    private String make;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Model is required")
    @Size(max = 100, message = "Model must not exceed 100 characters")
    private String model;

    @Column(nullable = false)
    @NotNull(message = "Year is required")
    @Min(value = 1900, message = "Year must be 1900 or later")
    @Max(value = 2030, message = "Year cannot be more than 2030")
    private Integer year;

    @Column(length = 50)
    @Size(max = 50, message = "Color must not exceed 50 characters")
    private String color;

    // Vehicle Classification
    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false)
    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_category")
    private VehicleCategory vehicleCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", nullable = false)
    @NotNull(message = "Fuel type is required")
    private FuelType fuelType;

    // Specifications
    @Column(name = "engine_size", length = 20)
    @Size(max = 20, message = "Engine size must not exceed 20 characters")
    private String engineSize;

    @Column(length = 20)
    @Size(max = 20, message = "Transmission must not exceed 20 characters")
    private String transmission;

    @Column(name = "seating_capacity")
    @Min(value = 1, message = "Seating capacity must be at least 1")
    @Max(value = 100, message = "Seating capacity cannot exceed 100")
    private Integer seatingCapacity;

    @Column(name = "cargo_capacity", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Cargo capacity must be non-negative")
    private BigDecimal cargoCapacity;

    @Column(name = "gross_weight", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Gross weight must be non-negative")
    private BigDecimal grossWeight;

    // Dimensions
    @Column(name = "length_mm")
    @Min(value = 0, message = "Length must be non-negative")
    private Integer lengthMm;

    @Column(name = "width_mm")
    @Min(value = 0, message = "Width must be non-negative")
    private Integer widthMm;

    @Column(name = "height_mm")
    @Min(value = 0, message = "Height must be non-negative")
    private Integer heightMm;

    @Column(name = "wheelbase_mm")
    @Min(value = 0, message = "Wheelbase must be non-negative")
    private Integer wheelbaseMm;

    // Status and Lifecycle
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private VehicleStatus status = VehicleStatus.ACTIVE;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "purchase_price", precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "Purchase price must be non-negative")
    private BigDecimal purchasePrice;

    @Column(name = "current_mileage")
    @Min(value = 0, message = "Current mileage must be non-negative")
    @Builder.Default
    private Integer currentMileage = 0;

    // Location and Assignment
    @Column(name = "current_location_lat", precision = 10, scale = 8)
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private BigDecimal currentLocationLat;

    @Column(name = "current_location_lng", precision = 11, scale = 8)
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private BigDecimal currentLocationLng;

    @Column(name = "home_location", length = 255)
    @Size(max = 255, message = "Home location must not exceed 255 characters")
    private String homeLocation;

    @Column(name = "current_driver_id")
    private UUID currentDriverId;

    // Maintenance
    @Column(name = "last_service_date")
    private LocalDate lastServiceDate;

    @Column(name = "next_service_due_date")
    private LocalDate nextServiceDueDate;

    @Column(name = "next_service_due_mileage")
    @Min(value = 0, message = "Next service due mileage must be non-negative")
    private Integer nextServiceDueMileage;

    // Insurance and Registration
    @Column(name = "insurance_provider", length = 100)
    @Size(max = 100, message = "Insurance provider must not exceed 100 characters")
    private String insuranceProvider;

    @Column(name = "insurance_policy_number", length = 50)
    @Size(max = 50, message = "Insurance policy number must not exceed 50 characters")
    private String insurancePolicyNumber;

    @Column(name = "insurance_expiry_date")
    private LocalDate insuranceExpiryDate;

    @Column(name = "registration_expiry_date")
    private LocalDate registrationExpiryDate;

    // Additional Information
    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "custom_fields", columnDefinition = "jsonb")
    private Map<String, Object> customFields;

    // Audit Fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    // Business Logic Methods

    /**
     * Check if the vehicle is currently assigned to a driver
     */
    public boolean isAssigned() {
        return currentDriverId != null;
    }

    /**
     * Check if the vehicle is active and available for assignment
     */
    public boolean isAvailableForAssignment() {
        return status == VehicleStatus.ACTIVE && currentDriverId == null;
    }

    /**
     * Check if the vehicle is due for maintenance
     */
    public boolean isDueForMaintenance() {
        if (nextServiceDueDate != null && nextServiceDueDate.isBefore(LocalDate.now())) {
            return true;
        }
        if (nextServiceDueMileage != null && currentMileage >= nextServiceDueMileage) {
            return true;
        }
        return false;
    }

    /**
     * Check if insurance is expired or expiring soon
     */
    public boolean isInsuranceExpiring(int daysThreshold) {
        if (insuranceExpiryDate == null) {
            return false;
        }
        return insuranceExpiryDate.isBefore(LocalDate.now().plusDays(daysThreshold));
    }

    /**
     * Check if registration is expired or expiring soon
     */
    public boolean isRegistrationExpiring(int daysThreshold) {
        if (registrationExpiryDate == null) {
            return false;
        }
        return registrationExpiryDate.isBefore(LocalDate.now().plusDays(daysThreshold));
    }

    /**
     * Get the vehicle age in years
     */
    public int getAgeInYears() {
        return LocalDate.now().getYear() - year;
    }

    /**
     * Calculate estimated value based on depreciation
     */
    public BigDecimal getEstimatedCurrentValue() {
        if (purchasePrice == null) {
            return BigDecimal.ZERO;
        }

        int age = getAgeInYears();
        double depreciationRate = 0.15; // 15% per year (configurable)
        double currentValueMultiplier = Math.pow(1 - depreciationRate, age);

        return purchasePrice.multiply(BigDecimal.valueOf(currentValueMultiplier));
    }

    /**
     * Update mileage and check for service due
     */
    public void updateMileage(int newMileage) {
        if (newMileage < this.currentMileage) {
            throw new IllegalArgumentException("New mileage cannot be less than current mileage");
        }
        this.currentMileage = newMileage;
    }

    /**
     * Assign driver to vehicle
     */
    public void assignDriver(UUID driverId) {
        if (status != VehicleStatus.ACTIVE) {
            throw new IllegalStateException("Cannot assign driver to inactive vehicle");
        }
        this.currentDriverId = driverId;
    }

    /**
     * Unassign driver from vehicle
     */
    public void unassignDriver() {
        this.currentDriverId = null;
    }

    /**
     * Update vehicle location
     */
    public void updateLocation(BigDecimal latitude, BigDecimal longitude) {
        this.currentLocationLat = latitude;
        this.currentLocationLng = longitude;
    }

    /**
     * Set vehicle to maintenance status
     */
    public void setToMaintenance() {
        this.status = VehicleStatus.MAINTENANCE;
        // Unassign driver when going to maintenance
        this.currentDriverId = null;
    }

    /**
     * Return vehicle from maintenance to active
     */
    public void returnFromMaintenance() {
        this.status = VehicleStatus.ACTIVE;
        this.lastServiceDate = LocalDate.now();
    }

    /**
     * Retire vehicle
     */
    public void retire() {
        this.status = VehicleStatus.RETIRED;
        this.currentDriverId = null;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}