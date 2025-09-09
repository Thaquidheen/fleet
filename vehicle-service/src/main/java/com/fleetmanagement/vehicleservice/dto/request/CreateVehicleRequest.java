package com.fleetmanagement.vehicleservice.dto.request;

import com.fleetmanagement.vehicleservice.domain.enums.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

/**
 * Create Vehicle Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateVehicleRequest {

    @NotBlank(message = "Vehicle name is required")
    @Size(max = 255, message = "Vehicle name must not exceed 255 characters")
    private String name;

    @Size(min = 17, max = 17, message = "VIN must be exactly 17 characters")
    @Pattern(regexp = "^[A-HJ-NPR-Z0-9]{17}$",
            message = "VIN must contain only valid characters (no I, O, Q)")
    private String vin;

    @NotBlank(message = "License plate is required")
    @Size(max = 20, message = "License plate must not exceed 20 characters")
    private String licensePlate;

    @NotBlank(message = "Make is required")
    @Size(max = 100, message = "Make must not exceed 100 characters")
    private String make;

    @NotBlank(message = "Model is required")
    @Size(max = 100, message = "Model must not exceed 100 characters")
    private String model;

    @NotNull(message = "Year is required")
    @Min(value = 1900, message = "Year must be 1900 or later")
    @Max(value = 2030, message = "Year cannot be more than 2030")
    private Integer year;

    @Size(max = 50, message = "Color must not exceed 50 characters")
    private String color;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    private VehicleCategory vehicleCategory;

    @NotNull(message = "Fuel type is required")
    private FuelType fuelType;

    @Size(max = 20, message = "Engine size must not exceed 20 characters")
    private String engineSize;

    @Size(max = 20, message = "Transmission must not exceed 20 characters")
    private String transmission;

    @Min(value = 1, message = "Seating capacity must be at least 1")
    @Max(value = 100, message = "Seating capacity cannot exceed 100")
    private Integer seatingCapacity;

    @DecimalMin(value = "0.0", message = "Cargo capacity must be non-negative")
    private BigDecimal cargoCapacity;

    @DecimalMin(value = "0.0", message = "Gross weight must be non-negative")
    private BigDecimal grossWeight;

    @Min(value = 0, message = "Length must be non-negative")
    private Integer lengthMm;

    @Min(value = 0, message = "Width must be non-negative")
    private Integer widthMm;

    @Min(value = 0, message = "Height must be non-negative")
    private Integer heightMm;

    @Min(value = 0, message = "Wheelbase must be non-negative")
    private Integer wheelbaseMm;

    private LocalDate purchaseDate;

    @DecimalMin(value = "0.0", message = "Purchase price must be non-negative")
    private BigDecimal purchasePrice;

    @Min(value = 0, message = "Current mileage must be non-negative")
    private Integer currentMileage;

    @Size(max = 255, message = "Home location must not exceed 255 characters")
    private String homeLocation;

    @Size(max = 100, message = "Insurance provider must not exceed 100 characters")
    private String insuranceProvider;

    @Size(max = 50, message = "Insurance policy number must not exceed 50 characters")
    private String insurancePolicyNumber;

    private LocalDate insuranceExpiryDate;

    private LocalDate registrationExpiryDate;

    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;

    private Map<String, Object> customFields;

    // Group assignments
    private UUID primaryGroupId;

    private java.util.List<UUID> additionalGroupIds;
}
