package com.fleetmanagement.vehicleservice.service;

import com.fleetmanagement.vehicleservice.domain.entity.Vehicle;
import com.fleetmanagement.vehicleservice.domain.enums.VehicleStatus;
import com.fleetmanagement.vehicleservice.domain.enums.VehicleType;
import com.fleetmanagement.vehicleservice.domain.enums.FuelType;
import com.fleetmanagement.vehicleservice.dto.request.*;
import com.fleetmanagement.vehicleservice.dto.response.*;
import com.fleetmanagement.vehicleservice.exception.*;
import com.fleetmanagement.vehicleservice.repository.VehicleRepository;
import com.fleetmanagement.vehicleservice.service.integration.CompanyServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Vehicle Service
 *
 * Core business logic service for vehicle management including:
 * - Vehicle CRUD operations
 * - Vehicle validation and business rules
 * - Fleet analytics and reporting
 * - Integration with company and user services
 */
@Service
@Transactional
public class VehicleService {

    private static final Logger logger = LoggerFactory.getLogger(VehicleService.class);

    private final VehicleRepository vehicleRepository;
    private final CompanyServiceClient companyServiceClient;
    private final CacheService cacheService;

    @Autowired
    public VehicleService(VehicleRepository vehicleRepository,
                          CompanyServiceClient companyServiceClient,
                          CacheService cacheService) {
        this.vehicleRepository = vehicleRepository;
        this.companyServiceClient = companyServiceClient;
        this.cacheService = cacheService;
    }

    /**
     * Create a new vehicle
     */
    public VehicleResponse createVehicle(CreateVehicleRequest request, UUID companyId, UUID createdBy) {
        logger.info("Creating new vehicle: {} for company: {}", request.getName(), companyId);

        // Validate company vehicle limits
        validateVehicleLimit(companyId);

        // Validate unique constraints
        validateVehicleUniqueness(request.getVin(), request.getLicensePlate(), companyId, null);

        // Create vehicle entity
        Vehicle vehicle = Vehicle.builder()
                .companyId(companyId)
                .name(request.getName())
                .vin(request.getVin())
                .licensePlate(request.getLicensePlate())
                .make(request.getMake())
                .model(request.getModel())
                .year(request.getYear())
                .color(request.getColor())
                .vehicleType(request.getVehicleType())
                .vehicleCategory(request.getVehicleCategory())
                .fuelType(request.getFuelType())
                .engineSize(request.getEngineSize())
                .transmission(request.getTransmission())
                .seatingCapacity(request.getSeatingCapacity())
                .cargoCapacity(request.getCargoCapacity())
                .grossWeight(request.getGrossWeight())
                .lengthMm(request.getLengthMm())
                .widthMm(request.getWidthMm())
                .heightMm(request.getHeightMm())
                .wheelbaseMm(request.getWheelbaseMm())
                .purchaseDate(request.getPurchaseDate())
                .purchasePrice(request.getPurchasePrice())
                .currentMileage(request.getCurrentMileage() != null ? request.getCurrentMileage() : 0)
                .homeLocation(request.getHomeLocation())
                .insuranceProvider(request.getInsuranceProvider())
                .insurancePolicyNumber(request.getInsurancePolicyNumber())
                .insuranceExpiryDate(request.getInsuranceExpiryDate())
                .registrationExpiryDate(request.getRegistrationExpiryDate())
                .notes(request.getNotes())
                .customFields(request.getCustomFields())
                .status(VehicleStatus.ACTIVE)
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .build();

        vehicle = vehicleRepository.save(vehicle);

        // Clear related caches
        clearVehicleCaches(companyId);

        logger.info("Successfully created vehicle: {} with ID: {}", vehicle.getName(), vehicle.getId());
        return mapToVehicleResponse(vehicle);
    }

    /**
     * Get vehicle by ID
     */
    @Cacheable(value = "vehicle-details", key = "#vehicleId")
    public VehicleResponse getVehicleById(UUID vehicleId, UUID companyId) {
        logger.debug("Fetching vehicle: {} for company: {}", vehicleId, companyId);

        Vehicle vehicle = vehicleRepository.findByIdAndCompanyId(vehicleId, companyId)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found: " + vehicleId));

        return mapToVehicleResponse(vehicle);
    }

    /**
     * Update vehicle
     */
    @CacheEvict(value = "vehicle-details", key = "#vehicleId")
    public VehicleResponse updateVehicle(UUID vehicleId, UpdateVehicleRequest request, UUID companyId, UUID updatedBy) {
        logger.info("Updating vehicle: {} for company: {}", vehicleId, companyId);

        Vehicle vehicle = vehicleRepository.findByIdAndCompanyId(vehicleId, companyId)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found: " + vehicleId));

        // Validate unique constraints if license plate is being updated
        if (StringUtils.hasText(request.getLicensePlate()) &&
                !request.getLicensePlate().equals(vehicle.getLicensePlate())) {
            validateVehicleUniqueness(null, request.getLicensePlate(), companyId, vehicleId);
        }

        // Update fields
        updateVehicleFields(vehicle, request, updatedBy);

        vehicle = vehicleRepository.save(vehicle);

        // Clear related caches
        clearVehicleCaches(companyId);

        logger.info("Successfully updated vehicle: {}", vehicleId);
        return mapToVehicleResponse(vehicle);
    }

    /**
     * Delete vehicle
     */
    @CacheEvict(value = "vehicle-details", key = "#vehicleId")
    public void deleteVehicle(UUID vehicleId, UUID companyId, UUID deletedBy) {
        logger.info("Deleting vehicle: {} for company: {}", vehicleId, companyId);

        Vehicle vehicle = vehicleRepository.findByIdAndCompanyId(vehicleId, companyId)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found: " + vehicleId));

        // Check if vehicle can be deleted (not assigned, no active maintenance, etc.)
        validateVehicleDeletion(vehicle);

        vehicleRepository.delete(vehicle);

        // Clear related caches
        clearVehicleCaches(companyId);

        logger.info("Successfully deleted vehicle: {}", vehicleId);
    }

    /**
     * Get vehicles by company with pagination
     */
    @Cacheable(value = "vehicle-lists", key = "#companyId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public PagedResponse<VehicleSummaryResponse> getVehiclesByCompany(UUID companyId, Pageable pageable) {
        logger.debug("Fetching vehicles for company: {} with pagination", companyId);

        Page<Vehicle> vehiclePage = vehicleRepository.findByCompanyId(companyId, pageable);

        List<VehicleSummaryResponse> vehicleSummaries = vehiclePage.getContent().stream()
                .map(this::mapToVehicleSummaryResponse)
                .collect(Collectors.toList());

        return PagedResponse.<VehicleSummaryResponse>builder()
                .content(vehicleSummaries)
                .page(vehiclePage.getNumber())
                .size(vehiclePage.getSize())
                .totalElements((int) vehiclePage.getTotalElements())
                .totalPages(vehiclePage.getTotalPages())
                .first(vehiclePage.isFirst())
                .last(vehiclePage.isLast())
                .empty(vehiclePage.isEmpty())
                .build();
    }

    /**
     * Search vehicles with advanced criteria
     */
    public PagedResponse<VehicleSummaryResponse> searchVehicles(VehicleSearchRequest searchRequest, UUID companyId, Pageable pageable) {
        logger.debug("Searching vehicles for company: {} with criteria", companyId);

        Specification<Vehicle> spec = createVehicleSpecification(searchRequest, companyId);
        Page<Vehicle> vehiclePage = vehicleRepository.findAll(spec, pageable);

        List<VehicleSummaryResponse> vehicleSummaries = vehiclePage.getContent().stream()
                .map(this::mapToVehicleSummaryResponse)
                .collect(Collectors.toList());

        return PagedResponse.<VehicleSummaryResponse>builder()
                .content(vehicleSummaries)
                .page(vehiclePage.getNumber())
                .size(vehiclePage.getSize())
                .totalElements((int) vehiclePage.getTotalElements())
                .totalPages(vehiclePage.getTotalPages())
                .first(vehiclePage.isFirst())
                .last(vehiclePage.isLast())
                .empty(vehiclePage.isEmpty())
                .build();
    }

    /**
     * Get vehicles by status
     */
    public List<VehicleSummaryResponse> getVehiclesByStatus(UUID companyId, VehicleStatus status) {
        logger.debug("Fetching vehicles by status: {} for company: {}", status, companyId);

        List<Vehicle> vehicles = vehicleRepository.findByCompanyIdAndStatus(companyId, status);

        return vehicles.stream()
                .map(this::mapToVehicleSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get available vehicles (not assigned)
     */
    public List<VehicleSummaryResponse> getAvailableVehicles(UUID companyId) {
        logger.debug("Fetching available vehicles for company: {}", companyId);

        List<Vehicle> vehicles = vehicleRepository.findByCompanyIdAndCurrentDriverIdIsNull(companyId);

        return vehicles.stream()
                .filter(vehicle -> vehicle.getStatus() == VehicleStatus.ACTIVE)
                .map(this::mapToVehicleSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get vehicles due for maintenance
     */
    public List<VehicleSummaryResponse> getVehiclesDueForMaintenance(UUID companyId) {
        logger.debug("Fetching vehicles due for maintenance for company: {}", companyId);

        List<Vehicle> vehicles = vehicleRepository.findVehiclesDueForMaintenance(companyId);

        return vehicles.stream()
                .map(this::mapToVehicleSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get vehicles with expiring documents
     */
    public List<VehicleSummaryResponse> getVehiclesWithExpiringInsurance(UUID companyId, int daysThreshold) {
        LocalDate endDate = LocalDate.now().plusDays(daysThreshold);
        List<Vehicle> vehicles = vehicleRepository.findVehiclesWithExpiringInsurance(companyId, endDate);

        return vehicles.stream()
                .map(this::mapToVehicleSummaryResponse)
                .collect(Collectors.toList());
    }

    public List<VehicleSummaryResponse> getVehiclesWithExpiringRegistration(UUID companyId, int daysThreshold) {
        LocalDate endDate = LocalDate.now().plusDays(daysThreshold);
        List<Vehicle> vehicles = vehicleRepository.findVehiclesWithExpiringRegistration(companyId, endDate);

        return vehicles.stream()
                .map(this::mapToVehicleSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update vehicle mileage
     */
    @CacheEvict(value = "vehicle-details", key = "#request.vehicleId")
    public VehicleResponse updateVehicleMileage(VehicleMileageUpdateRequest request, UUID companyId, UUID updatedBy) {
        logger.info("Updating mileage for vehicle: {} to {}", request.getVehicleId(), request.getNewMileage());

        Vehicle vehicle = vehicleRepository.findByIdAndCompanyId(request.getVehicleId(), companyId)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found: " + request.getVehicleId()));

        if (request.getNewMileage() < vehicle.getCurrentMileage()) {
            throw new VehicleValidationException("New mileage cannot be less than current mileage");
        }

        vehicle.setCurrentMileage(request.getNewMileage());
        vehicle.setUpdatedBy(updatedBy);

        vehicle = vehicleRepository.save(vehicle);

        logger.info("Successfully updated mileage for vehicle: {}", request.getVehicleId());
        return mapToVehicleResponse(vehicle);
    }

    /**
     * Update vehicle location
     */
    @CacheEvict(value = "vehicle-details", key = "#request.vehicleId")
    public VehicleResponse updateVehicleLocation(VehicleLocationUpdateRequest request, UUID companyId, UUID updatedBy) {
        logger.debug("Updating location for vehicle: {}", request.getVehicleId());

        Vehicle vehicle = vehicleRepository.findByIdAndCompanyId(request.getVehicleId(), companyId)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found: " + request.getVehicleId()));

        vehicle.setCurrentLocationLat(request.getLatitude());
        vehicle.setCurrentLocationLng(request.getLongitude());
        vehicle.setUpdatedBy(updatedBy);

        vehicle = vehicleRepository.save(vehicle);

        return mapToVehicleResponse(vehicle);
    }

    /**
     * Validate vehicle for operations
     */
    public VehicleValidationResponse validateVehicle(UUID vehicleId, UUID companyId) {
        logger.debug("Validating vehicle: {}", vehicleId);

        Vehicle vehicle = vehicleRepository.findByIdAndCompanyId(vehicleId, companyId)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found: " + vehicleId));

        VehicleValidationResponse.VehicleValidationResponseBuilder responseBuilder = VehicleValidationResponse.builder()
                .isValid(true)
                .errors(new ArrayList<>())
                .warnings(new ArrayList<>())
                .validationDetails(new HashMap<>());

        // VIN validation
        boolean vinValid = vehicle.getVin() != null && vehicle.getVin().matches("^[A-HJ-NPR-Z0-9]{17}$");
        responseBuilder.vinValid(vinValid);

        if (!vinValid && vehicle.getVin() != null) {
            responseBuilder.isValid(false);
            responseBuilder.errors(List.of("Invalid VIN format"));
        }

        // License plate validation
        boolean licensePlateValid = StringUtils.hasText(vehicle.getLicensePlate());
        responseBuilder.licensePlateValid(licensePlateValid);

        if (!licensePlateValid) {
            responseBuilder.isValid(false);
            responseBuilder.errors(List.of("License plate is required"));
        }

        // Insurance validation
        boolean insuranceValid = vehicle.getInsuranceExpiryDate() == null ||
                !vehicle.getInsuranceExpiryDate().isBefore(LocalDate.now());
        responseBuilder.insuranceValid(insuranceValid);

        if (!insuranceValid) {
            responseBuilder.warnings(List.of("Insurance has expired"));