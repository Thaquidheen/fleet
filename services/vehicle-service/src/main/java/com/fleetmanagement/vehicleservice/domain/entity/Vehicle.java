package com.fleetmanagement.vehicleservice.domain.entity;

import com.fleetmanagement.vehicleservice.domain.enums.VehicleStatus;
import com.fleetmanagement.vehicleservice.domain.enums.VehicleType;
import com.fleetmanagement.vehicleservice.domain.enums.VehicleCategory;
import com.fleetmanagement.vehicleservice.domain.enums.FuelType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.UUID;

/**
 * Vehicle Entity - FIXED with complete Lombok annotations
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
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @Builder.Default
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

    @Column(name = "current_location_lat", precision = 10, scale = 8)
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private BigDecimal currentLocationLat;

    @Column(name = "current_location_lng", precision = 11, scale = 8)
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private BigDecimal currentLocationLng;

    @Column(name = "last_location_update")
    private LocalDateTime lastLocationUpdate;



    @Column(name = "last_service_date")
    private LocalDate lastServiceDate;

    @Column(name = "next_service_due_date")
    private LocalDate nextServiceDueDate;

    @Column(name = "service_interval_months")
    @Builder.Default
    private Integer serviceIntervalMonths = 6; // Default 6 months

    @Column(name = "service_interval_mileage")
    @Builder.Default
    private Integer serviceIntervalMileage = 10000; // Default 10k miles

    @Column(name = "insurance_expiry_date")
    private LocalDate insuranceExpiryDate;

    @Column(name = "registration_expiry_date")
    private LocalDate registrationExpiryDate;

    @Column(length = 2000)
    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;

    // FIXED: Proper JSON mapping for custom fields
//    @Column(name = "custom_fields", columnDefinition = "jsonb")
//    @JdbcTypeCode(SqlTypes.JSON)
//    private Map<String, Object> customFields;

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
    public boolean isAssigned() {
        return currentDriverId != null;
    }

    public boolean isAvailableForAssignment() {
        return status == VehicleStatus.ACTIVE && currentDriverId == null;
    }

    public boolean canBeAssignedToDriver() {
        return status == VehicleStatus.ACTIVE;
    }
}