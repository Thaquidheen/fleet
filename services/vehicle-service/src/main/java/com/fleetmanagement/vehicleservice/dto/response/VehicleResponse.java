// ===== VehicleResponse.java =====
package com.fleetmanagement.vehicleservice.dto.response;

import com.fleetmanagement.vehicleservice.domain.enums.VehicleType;
import com.fleetmanagement.vehicleservice.domain.enums.VehicleCategory;
import com.fleetmanagement.vehicleservice.domain.enums.VehicleStatus;
import com.fleetmanagement.vehicleservice.domain.enums.FuelType;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

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
    private boolean isAssigned;
    private boolean hasMaintenanceDue;
    private boolean hasDocumentsExpiring;
    private int daysUntilInsuranceExpiry;
    private int daysUntilRegistrationExpiry;

    // Constructor
    public VehicleResponse() {}

    // Builder Pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private VehicleResponse response = new VehicleResponse();

        public Builder id(UUID id) { response.id = id; return this; }
        public Builder name(String name) { response.name = name; return this; }
        public Builder licensePlate(String licensePlate) { response.licensePlate = licensePlate; return this; }
        public Builder vin(String vin) { response.vin = vin; return this; }
        public Builder make(String make) { response.make = make; return this; }
        public Builder model(String model) { response.model = model; return this; }
        public Builder year(Integer year) { response.year = year; return this; }
        public Builder color(String color) { response.color = color; return this; }
        public Builder vehicleType(VehicleType vehicleType) { response.vehicleType = vehicleType; return this; }
        public Builder vehicleCategory(VehicleCategory vehicleCategory) { response.vehicleCategory = vehicleCategory; return this; }
        public Builder fuelType(FuelType fuelType) { response.fuelType = fuelType; return this; }
        public Builder status(VehicleStatus status) { response.status = status; return this; }
        public Builder engineSize(String engineSize) { response.engineSize = engineSize; return this; }
        public Builder transmission(String transmission) { response.transmission = transmission; return this; }
        public Builder seatingCapacity(Integer seatingCapacity) { response.seatingCapacity = seatingCapacity; return this; }
        public Builder cargoCapacity(BigDecimal cargoCapacity) { response.cargoCapacity = cargoCapacity; return this; }
        public Builder grossWeight(BigDecimal grossWeight) { response.grossWeight = grossWeight; return this; }
        public Builder currentMileage(Integer currentMileage) { response.currentMileage = currentMileage; return this; }
        public Builder homeLocation(String homeLocation) { response.homeLocation = homeLocation; return this; }
        public Builder purchasePrice(BigDecimal purchasePrice) { response.purchasePrice = purchasePrice; return this; }
        public Builder purchaseDate(LocalDate purchaseDate) { response.purchaseDate = purchaseDate; return this; }
        public Builder insuranceProvider(String insuranceProvider) { response.insuranceProvider = insuranceProvider; return this; }
        public Builder insurancePolicyNumber(String insurancePolicyNumber) { response.insurancePolicyNumber = insurancePolicyNumber; return this; }
        public Builder insuranceExpiryDate(LocalDate insuranceExpiryDate) { response.insuranceExpiryDate = insuranceExpiryDate; return this; }
        public Builder registrationExpiryDate(LocalDate registrationExpiryDate) { response.registrationExpiryDate = registrationExpiryDate; return this; }
        public Builder notes(String notes) { response.notes = notes; return this; }
        public Builder customFields(Map<String, Object> customFields) { response.customFields = customFields; return this; }
        public Builder companyId(UUID companyId) { response.companyId = companyId; return this; }
        public Builder currentDriverId(UUID currentDriverId) { response.currentDriverId = currentDriverId; return this; }
        public Builder currentDriverName(String currentDriverName) { response.currentDriverName = currentDriverName; return this; }
        public Builder vehicleGroupId(UUID vehicleGroupId) { response.vehicleGroupId = vehicleGroupId; return this; }
        public Builder vehicleGroupName(String vehicleGroupName) { response.vehicleGroupName = vehicleGroupName; return this; }
        public Builder createdBy(UUID createdBy) { response.createdBy = createdBy; return this; }
        public Builder updatedBy(UUID updatedBy) { response.updatedBy = updatedBy; return this; }
        public Builder createdAt(LocalDateTime createdAt) { response.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { response.updatedAt = updatedAt; return this; }
        public Builder isAssigned(boolean isAssigned) { response.isAssigned = isAssigned; return this; }
        public Builder hasMaintenanceDue(boolean hasMaintenanceDue) { response.hasMaintenanceDue = hasMaintenanceDue; return this; }
        public Builder hasDocumentsExpiring(boolean hasDocumentsExpiring) { response.hasDocumentsExpiring = hasDocumentsExpiring; return this; }
        public Builder daysUntilInsuranceExpiry(int daysUntilInsuranceExpiry) { response.daysUntilInsuranceExpiry = daysUntilInsuranceExpiry; return this; }
        public Builder daysUntilRegistrationExpiry(int daysUntilRegistrationExpiry) { response.daysUntilRegistrationExpiry = daysUntilRegistrationExpiry; return this; }

        public VehicleResponse build() { return response; }
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }

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

    public String getEngineSize() { return engineSize; }
    public void setEngineSize(String engineSize) { this.engineSize = engineSize; }

    public String getTransmission() { return transmission; }
    public void setTransmission(String transmission) { this.transmission = transmission; }

    public Integer getSeatingCapacity() { return seatingCapacity; }
    public void setSeatingCapacity(Integer seatingCapacity) { this.seatingCapacity = seatingCapacity; }

    public BigDecimal getCargoCapacity() { return cargoCapacity; }
    public void setCargoCapacity(BigDecimal cargoCapacity) { this.cargoCapacity = cargoCapacity; }

    public BigDecimal getGrossWeight() { return grossWeight; }
    public void setGrossWeight(BigDecimal grossWeight) { this.grossWeight = grossWeight; }

    public Integer getCurrentMileage() { return currentMileage; }
    public void setCurrentMileage(Integer currentMileage) { this.currentMileage = currentMileage; }

    public String getHomeLocation() { return homeLocation; }
    public void setHomeLocation(String homeLocation) { this.homeLocation = homeLocation; }

    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }

    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }

    public String getInsuranceProvider() { return insuranceProvider; }
    public void setInsuranceProvider(String insuranceProvider) { this.insuranceProvider = insuranceProvider; }

    public String getInsurancePolicyNumber() { return insurancePolicyNumber; }
    public void setInsurancePolicyNumber(String insurancePolicyNumber) { this.insurancePolicyNumber = insurancePolicyNumber; }

    public LocalDate getInsuranceExpiryDate() { return insuranceExpiryDate; }
    public void setInsuranceExpiryDate(LocalDate insuranceExpiryDate) { this.insuranceExpiryDate = insuranceExpiryDate; }

    public LocalDate getRegistrationExpiryDate() { return registrationExpiryDate; }
    public void setRegistrationExpiryDate(LocalDate registrationExpiryDate) { this.registrationExpiryDate = registrationExpiryDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Map<String, Object> getCustomFields() { return customFields; }
    public void setCustomFields(Map<String, Object> customFields) { this.customFields = customFields; }

    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }

    public UUID getCurrentDriverId() { return currentDriverId; }
    public void setCurrentDriverId(UUID currentDriverId) { this.currentDriverId = currentDriverId; }

    public String getCurrentDriverName() { return currentDriverName; }
    public void setCurrentDriverName(String currentDriverName) { this.currentDriverName = currentDriverName; }

    public UUID getVehicleGroupId() { return vehicleGroupId; }
    public void setVehicleGroupId(UUID vehicleGroupId) { this.vehicleGroupId = vehicleGroupId; }

    public String getVehicleGroupName() { return vehicleGroupName; }
    public void setVehicleGroupName(String vehicleGroupName) { this.vehicleGroupName = vehicleGroupName; }

    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }

    public UUID getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(UUID updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isAssigned() { return isAssigned; }
    public void setAssigned(boolean assigned) { isAssigned = assigned; }

    public boolean isHasMaintenanceDue() { return hasMaintenanceDue; }
    public void setHasMaintenanceDue(boolean hasMaintenanceDue) { this.hasMaintenanceDue = hasMaintenanceDue; }

    public boolean isHasDocumentsExpiring() { return hasDocumentsExpiring; }
    public void setHasDocumentsExpiring(boolean hasDocumentsExpiring) { this.hasDocumentsExpiring = hasDocumentsExpiring; }

    public int getDaysUntilInsuranceExpiry() { return daysUntilInsuranceExpiry; }
    public void setDaysUntilInsuranceExpiry(int daysUntilInsuranceExpiry) { this.daysUntilInsuranceExpiry = daysUntilInsuranceExpiry; }

    public int getDaysUntilRegistrationExpiry() { return daysUntilRegistrationExpiry; }
    public void setDaysUntilRegistrationExpiry(int daysUntilRegistrationExpiry) { this.daysUntilRegistrationExpiry = daysUntilRegistrationExpiry; }
}