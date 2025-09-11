package com.fleetmanagement.vehicleservice.domain.entity;

import com.fleetmanagement.vehicleservice.domain.enums.VehicleStatus;
import com.fleetmanagement.vehicleservice.domain.enums.VehicleType;
import com.fleetmanagement.vehicleservice.domain.enums.VehicleCategory;
import com.fleetmanagement.vehicleservice.domain.enums.FuelType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Vehicle Entity (Without Lombok)
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
public class Vehicle {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    @NotNull(message = "Company ID is required")
    private UUID companyId;

    @Column(nullable = false, length = 255)
    @NotBlank(message = "Vehicle name is required")
    @Size(max = 255, message = "Vehicle name must not exceed 255 characters")
    private String name;

    @Column(length = 17, unique = true)
    @Size(min = 17, max = 17, message = "VIN must be exactly 17 characters")
    @Pattern(regexp = "^[A-HJ-NPR-Z0-9]{17}$", message = "VIN must contain only valid characters (no I, O, Q)")
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleStatus status = VehicleStatus.ACTIVE;

    @Column(name = "current_driver_id")
    private UUID currentDriverId;

    @Column(name = "current_mileage")
    @Min(value = 0, message = "Current mileage must be non-negative")
    private Integer currentMileage;

    @Column(name = "purchase_price", precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "Purchase price must be non-negative")
    private BigDecimal purchasePrice;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "insurance_expiry_date")
    private LocalDate insuranceExpiryDate;

    @Column(name = "registration_expiry_date")
    private LocalDate registrationExpiryDate;

    @Column(length = 2000)
    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;

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
    private Long version = 0L;

    // Constructors
    public Vehicle() {}

    // Builder Pattern (Manual Implementation)
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Vehicle vehicle = new Vehicle();

        public Builder id(UUID id) { vehicle.id = id; return this; }
        public Builder companyId(UUID companyId) { vehicle.companyId = companyId; return this; }
        public Builder name(String name) { vehicle.name = name; return this; }
        public Builder vin(String vin) { vehicle.vin = vin; return this; }
        public Builder licensePlate(String licensePlate) { vehicle.licensePlate = licensePlate; return this; }
        public Builder make(String make) { vehicle.make = make; return this; }
        public Builder model(String model) { vehicle.model = model; return this; }
        public Builder year(Integer year) { vehicle.year = year; return this; }
        public Builder color(String color) { vehicle.color = color; return this; }
        public Builder vehicleType(VehicleType vehicleType) { vehicle.vehicleType = vehicleType; return this; }
        public Builder vehicleCategory(VehicleCategory vehicleCategory) { vehicle.vehicleCategory = vehicleCategory; return this; }
        public Builder fuelType(FuelType fuelType) { vehicle.fuelType = fuelType; return this; }
        public Builder status(VehicleStatus status) { vehicle.status = status; return this; }
        public Builder currentDriverId(UUID currentDriverId) { vehicle.currentDriverId = currentDriverId; return this; }
        public Builder currentMileage(Integer currentMileage) { vehicle.currentMileage = currentMileage; return this; }
        public Builder purchasePrice(BigDecimal purchasePrice) { vehicle.purchasePrice = purchasePrice; return this; }
        public Builder purchaseDate(LocalDate purchaseDate) { vehicle.purchaseDate = purchaseDate; return this; }
        public Builder insuranceExpiryDate(LocalDate insuranceExpiryDate) { vehicle.insuranceExpiryDate = insuranceExpiryDate; return this; }
        public Builder registrationExpiryDate(LocalDate registrationExpiryDate) { vehicle.registrationExpiryDate = registrationExpiryDate; return this; }
        public Builder notes(String notes) { vehicle.notes = notes; return this; }
        public Builder createdBy(UUID createdBy) { vehicle.createdBy = createdBy; return this; }
        public Builder updatedBy(UUID updatedBy) { vehicle.updatedBy = updatedBy; return this; }

        public Vehicle build() { return vehicle; }
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }

    public VehicleCategory getVehicleCategory() { return vehicleCategory; }
    public void setVehicleCategory(VehicleCategory vehicleCategory) { this.vehicleCategory = vehicleCategory; }

    public FuelType getFuelType() { return fuelType; }
    public void setFuelType(FuelType fuelType) { this.fuelType = fuelType; }

    public VehicleStatus getStatus() { return status; }
    public void setStatus(VehicleStatus status) { this.status = status; }

    public UUID getCurrentDriverId() { return currentDriverId; }
    public void setCurrentDriverId(UUID currentDriverId) { this.currentDriverId = currentDriverId; }

    public Integer getCurrentMileage() { return currentMileage; }
    public void setCurrentMileage(Integer currentMileage) { this.currentMileage = currentMileage; }

    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }

    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }

    public LocalDate getInsuranceExpiryDate() { return insuranceExpiryDate; }
    public void setInsuranceExpiryDate(LocalDate insuranceExpiryDate) { this.insuranceExpiryDate = insuranceExpiryDate; }

    public LocalDate getRegistrationExpiryDate() { return registrationExpiryDate; }
    public void setRegistrationExpiryDate(LocalDate registrationExpiryDate) { this.registrationExpiryDate = registrationExpiryDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }

    public UUID getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(UUID updatedBy) { this.updatedBy = updatedBy; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    // Business Logic Methods
    public boolean isAssigned() {
        return currentDriverId != null;
    }

    public boolean isAvailableForAssignment() {
        return status == VehicleStatus.ACTIVE && currentDriverId == null;
    }
}