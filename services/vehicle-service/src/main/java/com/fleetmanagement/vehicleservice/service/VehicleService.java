package com.fleetmanagement.vehicleservice.service;

import com.fleetmanagement.vehicleservice.client.CompanyServiceClient;
import com.fleetmanagement.vehicleservice.client.CompanyServiceClient.CompanyValidationResponse;
import com.fleetmanagement.vehicleservice.client.CompanyServiceClient.CanAddVehicleResponse;
import com.fleetmanagement.vehicleservice.domain.entity.Vehicle;
import com.fleetmanagement.vehicleservice.domain.enums.VehicleStatus;
import com.fleetmanagement.vehicleservice.dto.request.CreateVehicleRequest;
import com.fleetmanagement.vehicleservice.dto.response.VehicleResponse;
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
import java.util.UUID;

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

        // 3. Create vehicle entity
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
            // Note: This doesn't fail the transaction, but should be monitored
        }

        return mapToResponse(savedVehicle);
    }

    /**
     * Validate vehicle creation against company limits
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
            // In case of service failure, we could either:
            // 1. Fail fast (current approach)
            // 2. Allow creation with a warning
            throw new VehicleLimitExceededException("Unable to validate company vehicle limits");
        }
    }

    /**
     * Delete vehicle with company count synchronization
     */
    @Transactional
    @CacheEvict(value = {"vehicles", "vehicle"}, allEntries = true)
    public void deleteVehicle(UUID vehicleId, UUID companyId, UUID deletedBy) {
        logger.info("Deleting vehicle: {} for company: {}", vehicleId, companyId);

        Vehicle vehicle = vehicleRepository.findByIdAndCompanyId(vehicleId, companyId)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found: " + vehicleId));

        // Set vehicle as deleted/inactive
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
     * Get vehicles by company with caching
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "vehicles", key = "#companyId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<VehicleResponse> getVehiclesByCompany(UUID companyId, Pageable pageable) {
        logger.debug("Retrieving vehicles for company: {}", companyId);

        Page<Vehicle> vehicles = vehicleRepository.findByCompanyIdAndStatusNot(
                companyId, VehicleStatus.RETIRED, pageable);

        return vehicles.map(this::mapToResponse);
    }

    /**
     * Get vehicle by ID with company validation
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "vehicle", key = "#vehicleId")
    public VehicleResponse getVehicleById(UUID vehicleId, UUID companyId) {
        logger.debug("Retrieving vehicle: {} for company: {}", vehicleId, companyId);

        Vehicle vehicle = vehicleRepository.findByIdAndCompanyId(vehicleId, companyId)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found: " + vehicleId));

        return mapToResponse(vehicle);
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
                .status(vehicle.getStatus())
                .companyId(vehicle.getCompanyId())
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .build();
    }
}