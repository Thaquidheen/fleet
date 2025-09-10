package com.fleetmanagement.vehicleservice.dto.response;

import com.fleetmanagement.vehicleservice.domain.enums.VehicleType;
import com.fleetmanagement.vehicleservice.domain.enums.VehicleCategory;
import com.fleetmanagement.vehicleservice.domain.enums.VehicleStatus;
import com.fleetmanagement.vehicleservice.domain.enums.FuelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Vehicle Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VehicleResponse {

    private UUID id;

    private String name;

    private String licensePlate;

    private String vin;

    private String make;

    private String model;

    private Integer year;

    private String color;

    private VehicleType vehicleType;

    private VehicleCategory vehicleCategory;

    private FuelType fuelType;

    private VehicleStatus status;

    private String engineSize;

    private String transmission;

    private Integer seatingCapacity;

    private BigDecimal cargoCapacity;

    private BigDecimal grossWeight;

    private Integer currentMileage;

    private String homeLocation;

    private BigDecimal purchasePrice;

    private LocalDate purchaseDate;

    private String insuranceProvider;

    private String insurancePolicyNumber;

    private LocalDate insuranceExpiryDate;

    private LocalDate registrationExpiryDate;

    private String notes;

    private Map<String, Object> customFields;

    private UUID companyId;

    private UUID currentDriverId;

    private String currentDriverName;

    private UUID vehicleGroupId;

    private String vehicleGroupName;

    private UUID createdBy;

    private UUID updatedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Additional computed fields
    private boolean isAssigned;

    private boolean hasMaintenanceDue;

    private boolean hasDocumentsExpiring;

    private int daysUntilInsuranceExpiry;

    private int daysUntilRegistrationExpiry;
}