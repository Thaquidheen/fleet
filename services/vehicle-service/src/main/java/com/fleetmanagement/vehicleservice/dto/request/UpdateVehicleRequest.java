package com.fleetmanagement.vehicleservice.dto.request;

import com.fleetmanagement.vehicleservice.domain.enums.VehicleCategory;
import com.fleetmanagement.vehicleservice.domain.enums.VehicleStatus;
import com.fleetmanagement.vehicleservice.domain.enums.FuelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Update Vehicle Request DTO
 *
 * This class represents the data required to update an existing vehicle.
 * All fields are optional - only provide the fields you want to update.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateVehicleRequest {

    @Size(max = 255, message = "Vehicle name must not exceed 255 characters")
    private String name;

    @Size(max = 20, message = "License plate must not exceed 20 characters")
    private String licensePlate;

    @Size(max = 50, message = "Color must not exceed 50 characters")
    private String color;

    private VehicleCategory vehicleCategory;

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

    private VehicleStatus status;

    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;

    private Map<String, Object> customFields;
}