package com.fleetmanagement.deviceservice.external.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Vehicle Response DTO from Vehicle Service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VehicleResponse {

    private UUID id;
    private String vehicleName;
    private String licensePlate;
    private String vin;
    private String make;
    private String model;
    private Integer year;
    private String color;
    private String vehicleType;
    private String fuelType;
    private String status;

    private UUID companyId;
    private String companyName;

    // Current assignment
    private UUID currentDriverId;
    private String currentDriverName;

    // Vehicle specifications
    private Double engineCapacity;
    private Integer seatingCapacity;
    private Double fuelCapacity;
    private Double weight;
    private String transmission;

    // Insurance and registration
    private String insuranceProvider;
    private String insurancePolicyNumber;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime insuranceExpiryDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime registrationExpiryDate;

    // Maintenance information
    private Integer odometer;
    private Integer engineHours;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime lastMaintenanceDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime nextMaintenanceDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}