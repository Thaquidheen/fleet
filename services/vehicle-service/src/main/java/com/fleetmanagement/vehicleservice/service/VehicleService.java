package com.fleetmanagement.vehicleservice.service;

import com.fleetmanagement.vehicleservice.client.CompanyServiceClient;
import com.fleetmanagement.vehicleservice.client.CompanyServiceClient.CanAddVehicleResponse;
import com.fleetmanagement.vehicleservice.domain.entity.Vehicle;
import com.fleetmanagement.vehicleservice.domain.enums.VehicleStatus;
import com.fleetmanagement.vehicleservice.domain.enums.VehicleType;
import com.fleetmanagement.vehicleservice.dto.request.CreateVehicleRequest;
import com.fleetmanagement.vehicleservice.dto.request.UpdateVehicleRequest;
import com.fleetmanagement.vehicleservice.dto.response.VehicleResponse;
import com.fleetmanagement.vehicleservice.dto.response.VehicleStatisticsResponse;
import com.fleetmanagement.vehicleservice.controller.VehicleController;
import com.fleetmanagement.vehicleservice.exception.VehicleLimitExceededException;
import com.fleetmanagement.vehicleservice.exception.VehicleNotFoundException;
import com.fleetmanagement.vehicleservice.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class VehicleService {

    private static final Logger logger = LoggerFactory.getLogger(VehicleService.class);

    private final VehicleRepository vehicleRepository;
    private final CompanyServiceClient companyServiceClient;

    @Autowired
    public VehicleService(VehicleRepository vehicleRepository,
                          CompanyServiceClient companyServiceClient) {
        this.vehicleRepository = vehicleRepository;
        this.companyServiceClient = companyServiceClient;
    }

    /**
     * Create vehicle with company limit validation
     */
    @Transactional
    @CacheEvict(value = "vehicles", allEntries = true)
    public VehicleResponse createVehicle(CreateVehicleRequest request, UUID companyId, UUID createdBy) {
        logger.info("Creating vehicle: {} for company: {}", request.getName(), companyId);

        // 1. VALIDATE COMPANY LIMITS FIRST
        validateVehicleCreationLimits(companyId);

        // 2. Validate uniqueness
        validateVehicleUniqueness(request.getLicensePlate(), request.getVin(), companyId);

        // 3. Create vehicle entity using builder
        Vehicle vehicle = Vehicle.builder()
                .name(request.getName())
                .licensePlate(request.getLicensePlate())
                .vin(request.getVin())
                .make(request.getMake())
                .model(request.getModel())
                .year(request.getYear())
                .color(request.getColor())
                .vehicleType(request.getVehicleType())
                .vehicleCategory(request.getVehicleCategory())
                .fuelType(request.getFuelType())
                .companyId(companyId)
                .status(VehicleStatus.ACTIVE)
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .build();

        // 4. Save vehicle
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        logger.info("Vehicle created successfully with ID: {}", savedVehicle.getId());

        // 5. INCREMENT COMPANY VEHICLE COUNT
        try {
            companyServiceClient.incrementVehicleCount(companyId);
            logger.info("Vehicle count incremented for company: {}", companyId);
        } catch (Exception e) {
            logger.error("Failed to increment vehicle count for company: {}", companyId, e);
        }

        return mapToResponse(savedVehicle);
    }

    /**
     * Get vehicles by company with pagination
     */
    @Cacheable(value = "companyVehicles", key = "#companyId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<VehicleResponse> getVehiclesByCompany(UUID companyId, Pageable pageable) {
        logger.debug("Getting vehicles for company: {}", companyId);

        // Use existing repository method that excludes retired vehicles
        Page<Vehicle> vehicles = vehicleRepository.findByCompanyIdAndFilters(companyId, null, null, pageable);
        return vehicles.map(this::mapToResponse);
    }

    /**
     * Get vehicle by ID
     */
    @Cacheable(value = "vehicleDetails", key = "#vehicleId")
    public VehicleResponse getVehicleById(UUID vehicleId, UUID companyId) {
        logger.debug("Getting vehicle by ID: {} for company: {}", vehicleId, companyId);

        Vehicle vehicle = vehicleRepository.findByIdAndCompanyId(vehicleId, companyId)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found with ID: " + vehicleId));

        return mapToResponse(vehicle);
    }

    /**
     * Update vehicle
     */
    @Transactional
    @CacheEvict(value = {"vehicles", "vehicleDetails"}, key = "#vehicleId")
    public VehicleResponse updateVehicle(UUID vehicleId, UpdateVehicleRequest request, UUID companyId, UUID updatedBy) {
        logger.info("Updating vehicle: {} for company: {}", vehicleId, companyId);

        Vehicle vehicle = vehicleRepository.findByIdAndCompanyId(vehicleId, companyId)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found with ID: " + vehicleId));

        // Update fields if provided
        if (request.getName() != null) {
            vehicle.setName(request.getName());
        }
        if (request.getLicensePlate() != null) {
            // Validate uniqueness for license plate
            if (!request.getLicensePlate().equals(vehicle.getLicensePlate()) &&
                    vehicleRepository.existsByLicensePlateAndCompanyId(request.getLicensePlate(), companyId)) {
                throw new IllegalArgumentException("Vehicle with license plate " + request.getLicensePlate() + " already exists");
            }
            vehicle.setLicensePlate(request.getLicensePlate());
        }
        if (request.getColor() != null) {
            vehicle.setColor(request.getColor());
        }
        if (request.getVehicleCategory() != null) {
            vehicle.setVehicleCategory(request.getVehicleCategory());
        }
        if (request.getFuelType() != null) {
            vehicle.setFuelType(request.getFuelType());
        }
        if (request.getCurrentMileage() != null) {
            vehicle.setCurrentMileage(request.getCurrentMileage());
        }
        if (request.getStatus() != null) {
            vehicle.setStatus(request.getStatus());
        }
        if (request.getNotes() != null) {
            vehicle.setNotes(request.getNotes());
        }

        vehicle.setUpdatedBy(updatedBy);
        vehicle.setUpdatedAt(LocalDateTime.now());

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return mapToResponse(savedVehicle);
    }

    /**
     * Delete vehicle
     */
    @Transactional
    @CacheEvict(value = {"vehicles", "vehicleDetails"}, allEntries = true)
    public void deleteVehicle(UUID vehicleId, UUID companyId, UUID deletedBy) {
        logger.info("Deleting vehicle: {} for company: {}", vehicleId, companyId);

        Vehicle vehicle = vehicleRepository.findByIdAndCompanyId(vehicleId, companyId)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found: " + vehicleId));

        // Set vehicle as retired
        vehicle.setStatus(VehicleStatus.RETIRED);
        vehicle.setUpdatedBy(deletedBy);
        vehicle.setUpdatedAt(LocalDateTime.now());

        vehicleRepository.save(vehicle);

        // DECREMENT COMPANY VEHICLE COUNT
        try {
            companyServiceClient.decrementVehicleCount(companyId);
            logger.info("Vehicle count decremented for company: {}", companyId);
        } catch (Exception e) {
            logger.error("Failed to decrement vehicle count for company: {}", companyId, e);
        }

        logger.info("Vehicle deleted successfully: {}", vehicleId);
    }

    /**
     * Get vehicle statistics for a company
     */
    public VehicleStatisticsResponse getVehicleStatistics(UUID companyId) {
        logger.debug("Getting vehicle statistics for company: {}", companyId);

        List<Vehicle> vehicles = vehicleRepository.findByCompanyId(companyId);

        Map<String, Integer> vehiclesByType = vehicles.stream()
                .collect(Collectors.groupingBy(
                        v -> v.getVehicleType().getDisplayName(),
                        Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                ));

        Map<String, Integer> vehiclesByStatus = vehicles.stream()
                .collect(Collectors.groupingBy(
                        v -> v.getStatus().getDisplayName(),
                        Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                ));

        int totalVehicles = vehicles.size();
        int activeVehicles = (int) vehicles.stream().filter(v -> v.getStatus() == VehicleStatus.ACTIVE).count();
        int assignedVehicles = (int) vehicles.stream().filter(Vehicle::isAssigned).count();

        return VehicleStatisticsResponse.builder()
                .totalVehicles(totalVehicles)
                .activeVehicles(activeVehicles)
                .assignedVehicles(assignedVehicles)
                .unassignedVehicles(activeVehicles - assignedVehicles)
                .vehiclesByType(vehiclesByType)
                .vehiclesByStatus(vehiclesByStatus)
                .build();
    }

    /**
     * Search vehicles with filters
     */
    public Page<VehicleResponse> searchVehicles(UUID companyId, String query, String vehicleType, String status, Pageable pageable) {
        logger.debug("Searching vehicles for company: {} with query: {}", companyId, query);

        VehicleType typeEnum = null;
        VehicleStatus statusEnum = null;

        try {
            if (vehicleType != null && !vehicleType.trim().isEmpty()) {
                typeEnum = VehicleType.valueOf(vehicleType.toUpperCase());
            }
            if (status != null && !status.trim().isEmpty()) {
                statusEnum = VehicleStatus.valueOf(status.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid enum value in search criteria: {}", e.getMessage());
        }

        Page<Vehicle> vehicles;

        if (query != null && !query.trim().isEmpty()) {
            vehicles = vehicleRepository.findByCompanyIdAndSearchCriteria(companyId, query, typeEnum, statusEnum, pageable);
        } else {
            vehicles = vehicleRepository.findByCompanyIdAndFilters(companyId, typeEnum, statusEnum, pageable);
        }

        return vehicles.map(this::mapToResponse);
    }

    /**
     * Validate vehicle creation against company limits
     */
    public VehicleController.VehicleCreationValidationResponse validateVehicleCreation(UUID companyId) {
        logger.debug("Validating vehicle creation for company: {}", companyId);

        VehicleController.VehicleCreationValidationResponse response = new VehicleController.VehicleCreationValidationResponse();

        try {
            CanAddVehicleResponse validation = companyServiceClient.canAddVehicle(companyId).getBody();

            if (validation != null) {
                response.setCanCreateVehicle(validation.isCanAdd());
                response.setReason(validation.getReason());
                response.setRemainingSlots(validation.getRemainingSlots());
                response.setCurrentVehicles(validation.getCurrentVehicles());
                response.setMaxVehicles(validation.getMaxVehicles());
                response.setSubscriptionPlan(validation.getSubscriptionPlan());
            } else {
                response.setCanCreateVehicle(false);
                response.setReason("Unable to validate vehicle limits");
                response.setRemainingSlots(0);
            }

        } catch (Exception e) {
            logger.error("Failed to validate vehicle creation for company: {}", companyId, e);
            response.setCanCreateVehicle(false);
            response.setReason("Validation service unavailable");
            response.setRemainingSlots(0);
        }

        return response;
    }

    /**
     * Validate vehicle creation against company limits (private method)
     */
    private void validateVehicleCreationLimits(UUID companyId) {
        try {
            logger.debug("Validating vehicle limits for company: {}", companyId);

            CanAddVehicleResponse validation = companyServiceClient.canAddVehicle(companyId).getBody();

            if (validation == null || !validation.isCanAdd()) {
                String message = validation != null ? validation.getReason() :
                        "Company has reached maximum vehicle limit";
                throw new VehicleLimitExceededException(message);
            }

            logger.info("Vehicle creation validated for company: {} (Remaining slots: {})",
                    companyId, validation.getRemainingSlots());

        } catch (VehicleLimitExceededException e) {
            throw e; // Re-throw limit exceptions
        } catch (Exception e) {
            logger.error("Failed to validate vehicle creation for company: {}", companyId, e);
            throw new VehicleLimitExceededException("Unable to validate company vehicle limits");
        }
    }

    /**
     * Validate vehicle uniqueness within company
     */
    private void validateVehicleUniqueness(String licensePlate, String vin, UUID companyId) {
        if (vehicleRepository.existsByLicensePlateAndCompanyId(licensePlate, companyId)) {
            throw new IllegalArgumentException("Vehicle with license plate " + licensePlate + " already exists");
        }

        if (vin != null && vehicleRepository.existsByVinAndCompanyId(vin, companyId)) {
            throw new IllegalArgumentException("Vehicle with VIN " + vin + " already exists");
        }
    }

    /**
     * Map entity to response DTO
     */
    private VehicleResponse mapToResponse(Vehicle vehicle) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .name(vehicle.getName())
                .licensePlate(vehicle.getLicensePlate())
                .vin(vehicle.getVin())
                .make(vehicle.getMake())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .color(vehicle.getColor())
                .vehicleType(vehicle.getVehicleType())
                .vehicleCategory(vehicle.getVehicleCategory())
                .fuelType(vehicle.getFuelType())
                .status(vehicle.getStatus())
                .companyId(vehicle.getCompanyId())
                .currentDriverId(vehicle.getCurrentDriverId())
                .currentMileage(vehicle.getCurrentMileage())
                .purchasePrice(vehicle.getPurchasePrice())
                .purchaseDate(vehicle.getPurchaseDate())
                .insuranceExpiryDate(vehicle.getInsuranceExpiryDate())
                .registrationExpiryDate(vehicle.getRegistrationExpiryDate())
                .notes(vehicle.getNotes())
                .createdBy(vehicle.getCreatedBy())
                .updatedBy(vehicle.getUpdatedBy())
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .isAssigned(vehicle.isAssigned())
                .build();
    }
}