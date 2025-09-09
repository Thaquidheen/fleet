package com.fleetmanagement.vehicleservice.service;

import com.fleetmanagement.vehicleservice.domain.entity.Vehicle;
import com.fleetmanagement.vehicleservice.domain.entity.VehicleAssignment;
import com.fleetmanagement.vehicleservice.domain.enums.AssignmentStatus;
import com.fleetmanagement.vehicleservice.domain.enums.AssignmentType;
import com.fleetmanagement.vehicleservice.dto.request.*;
import com.fleetmanagement.vehicleservice.dto.response.*;
import com.fleetmanagement.vehicleservice.exception.*;
import com.fleetmanagement.vehicleservice.repository.VehicleAssignmentRepository;
import com.fleetmanagement.vehicleservice.repository.VehicleRepository;
import com.fleetmanagement.vehicleservice.service.integration.UserServiceClient;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Vehicle Assignment Service
 *
 * Manages driver-vehicle assignments with conflict detection,
 * check-in/check-out functionality, and assignment history tracking.
 */
@Service
@Transactional
public class VehicleAssignmentService {

    private static final Logger logger = LoggerFactory.getLogger(VehicleAssignmentService.class);

    private final VehicleAssignmentRepository assignmentRepository;
    private final VehicleRepository vehicleRepository;
    private final UserServiceClient userServiceClient;
    private final CacheService cacheService;

    @Autowired
    public VehicleAssignmentService(VehicleAssignmentRepository assignmentRepository,
                                    VehicleRepository vehicleRepository,
                                    UserServiceClient userServiceClient,
                                    CacheService cacheService) {
        this.assignmentRepository = assignmentRepository;
        this.vehicleRepository = vehicleRepository;
        this.userServiceClient = userServiceClient;
        this.cacheService = cacheService;
    }

    /**
     * Assign driver to vehicle
     */
    public VehicleAssignmentResponse assignDriverToVehicle(AssignDriverRequest request, UUID companyId, UUID assignedBy) {
        logger.info("Assigning driver {} to vehicle {} for company: {}",
                request.getDriverId(), request.getVehicleId(), companyId);

        // Validate vehicle exists and is available
        Vehicle vehicle = vehicleRepository.findByIdAndCompanyId(request.getVehicleId(), companyId)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found: " + request.getVehicleId()));

        // Validate driver exists (call user service)
        validateDriver(request.getDriverId(), companyId);

        // Validate assignment dates
        validateAssignmentDates(request.getStartDate(), request.getEndDate());

        // Check for conflicts
        AssignmentValidationResponse validation = validateAssignment(request, companyId, null);
        if (!validation.isValid()) {
            throw new AssignmentConflictException("Assignment conflicts detected", validation.getErrors());
        }

        // Terminate any existing active assignments for this vehicle
        terminateExistingVehicleAssignments(request.getVehicleId(), companyId, assignedBy);

        // Create new assignment
        VehicleAssignment assignment = VehicleAssignment.builder()
                .vehicle(vehicle)
                .driverId(request.getDriverId())
                .companyId(companyId)
                .assignedDate(LocalDateTime.now())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(AssignmentStatus.ASSIGNED)
                .assignmentType(request.getAssignmentType())
                .shiftStartTime(request.getShiftStartTime())
                .shiftEndTime(request.getShiftEndTime())
                .notes(request.getNotes())
                .restrictions(request.getRestrictions())
                .createdBy(assignedBy)
                .updatedBy(assignedBy)
                .build();

        assignment = assignmentRepository.save(assignment);

        // Update vehicle's current driver
        vehicle.assignDriver(request.getDriverId());
        vehicle.setUpdatedBy(assignedBy);
        vehicleRepository.save(vehicle);

        // Clear related caches
        clearAssignmentCaches(companyId, request.getVehicleId(), request.getDriverId());

        logger.info("Successfully assigned driver {} to vehicle {}", request.getDriverId(), request.getVehicleId());
        return mapToVehicleAssignmentResponse(assignment);
    }

    /**
     * Update vehicle assignment
     */
    public VehicleAssignmentResponse updateAssignment(UUID assignmentId, UpdateAssignmentRequest request,
                                                      UUID companyId, UUID updatedBy) {
        logger.info("Updating assignment: {} for company: {}", assignmentId, companyId);

        VehicleAssignment assignment = assignmentRepository.findByIdAndCompanyId(assignmentId, companyId)
                .orElseThrow(() -> new AssignmentNotFoundException("Assignment not found: " + assignmentId));

        // Validate assignment update
        if (request.getEndDate() != null) {
            validateAssignmentDates(assignment.getStartDate(), request.getEndDate());
        }

        // Update fields
        updateAssignmentFields(assignment, request, updatedBy);

        assignment = assignmentRepository.save(assignment);

        // Clear related caches
        clearAssignmentCaches(companyId, assignment.getVehicle().getId(), assignment.getDriverId());

        logger.info("Successfully updated assignment: {}", assignmentId);
        return mapToVehicleAssignmentResponse(assignment);
    }

    /**
     * Terminate assignment
     */
    @CacheEvict(value = "assignments", key = "#assignmentId")
    public VehicleAssignmentResponse terminateAssignment(UUID assignmentId, UUID companyId, UUID terminatedBy) {
        logger.info("Terminating assignment: {} for company: {}", assignmentId, companyId);

        VehicleAssignment assignment = assignmentRepository.findByIdAndCompanyId(assignmentId, companyId)
                .orElseThrow(() -> new AssignmentNotFoundException("Assignment not found: " + assignmentId));

        // Terminate the assignment
        assignment.terminate();
        assignment.setUpdatedBy(terminatedBy);

        assignment = assignmentRepository.save(assignment);

        // Update vehicle to remove driver assignment
        Vehicle vehicle = assignment.getVehicle();
        vehicle.unassignDriver();
        vehicle.setUpdatedBy(terminatedBy);
        vehicleRepository.save(vehicle);

        // Clear related caches
        clearAssignmentCaches(companyId, vehicle.getId(), assignment.getDriverId());

        logger.info("Successfully terminated assignment: {}", assignmentId);
        return mapToVehicleAssignmentResponse(assignment);
    }

    /**
     * Check-in driver to vehicle
     */
    public CheckInOutResponse checkInDriver(VehicleCheckinRequest request, UUID companyId, UUID performedBy) {
        logger.info("Processing check-in for assignment: {}", request.getAssignmentId());

        VehicleAssignment assignment = assignmentRepository.findByIdAndCompanyId(request.getAssignmentId(), companyId)
                .orElseThrow(() -> new AssignmentNotFoundException("Assignment not found: " + request.getAssignmentId()));

        // Validate check-in
        if (!assignment.isActive()) {
            throw new AssignmentValidationException("Cannot check-in to inactive assignment");
        }

        if (assignment.isCheckedIn()) {
            throw new AssignmentValidationException("Driver is already checked in");
        }

        // Perform check-in
        assignment.checkIn(request.getLatitude(), request.getLongitude());
        assignment.setUpdatedBy(performedBy);

        assignmentRepository.save(assignment);

        // Clear related caches
        clearAssignmentCaches(companyId, assignment.getVehicle().getId(), assignment.getDriverId());

        logger.info("Successfully checked in driver for assignment: {}", request.getAssignmentId());

        return CheckInOutResponse.builder()
                .assignmentId(assignment.getId())
                .vehicleId(assignment.getVehicle().getId())
                .driverId(assignment.getDriverId())
                .operationType("CHECK_IN")
                .timestamp(assignment.getLastCheckinTime())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .success(true)
                .message("Driver successfully checked in")
                .build();
    }

    /**
     * Check-out driver from vehicle
     */
    public CheckInOutResponse checkOutDriver(VehicleCheckoutRequest request, UUID companyId, UUID performedBy) {
        logger.info("Processing check-out for assignment: {}", request.getAssignmentId());

        VehicleAssignment assignment = assignmentRepository.findByIdAndCompanyId(request.getAssignmentId(), companyId)
                .orElseThrow(() -> new AssignmentNotFoundException("Assignment not found: " + request.getAssignmentId()));

        // Validate check-out
        if (!assignment.isCheckedIn()) {
            throw new AssignmentValidationException("Driver is not currently checked in");
        }

        // Update vehicle mileage if provided
        if (request.getEndingMileage() != null) {
            Vehicle vehicle = assignment.getVehicle();
            if (request.getEndingMileage() >= vehicle.getCurrentMileage()) {
                vehicle.setCurrentMileage(request.getEndingMileage());
                vehicle.setUpdatedBy(performedBy);
                vehicleRepository.save(vehicle);
            }
        }

        // Perform check-out
        assignment.checkOut();
        assignment.setUpdatedBy(performedBy);

        assignmentRepository.save(assignment);

        // Clear related caches
        clearAssignmentCaches(companyId, assignment.getVehicle().getId(), assignment.getDriverId());

        logger.info("Successfully checked out driver for assignment: {}", request.getAssignmentId());

        return CheckInOutResponse.builder()
                .assignmentId(assignment.getId())
                .vehicleId(assignment.getVehicle().getId())
                .driverId(assignment.getDriverId())
                .operationType("CHECK_OUT")
                .timestamp(assignment.getLastCheckoutTime())
                .success(true)
                .message("Driver successfully checked out")
                .build();
    }

    /**
     * Get assignment by ID
     */
    @Cacheable(value = "assignments", key = "#assignmentId")
    public VehicleAssignmentResponse getAssignmentById(UUID assignmentId, UUID companyId) {
        logger.debug("Fetching assignment: {} for company: {}", assignmentId, companyId);

        VehicleAssignment assignment = assignmentRepository.findByIdAndCompanyId(assignmentId, companyId)
                .orElseThrow(() -> new AssignmentNotFoundException("Assignment not found: " + assignmentId));

        return mapToVehicleAssignmentResponse(assignment);
    }

    /**
     * Get assignments by company with pagination
     */
    public PagedResponse<VehicleAssignmentResponse> getAssignmentsByCompany(UUID companyId, Pageable pageable) {
        logger.debug("Fetching assignments for company: {} with pagination", companyId);

        Page<VehicleAssignment> assignmentPage = assignmentRepository.findByCompanyId(companyId, pageable);

        List<VehicleAssignmentResponse> assignmentResponses = assignmentPage.getContent().stream()
                .map(this::mapToVehicleAssignmentResponse)
                .collect(Collectors.toList());

        return PagedResponse.<VehicleAssignmentResponse>builder()
                .content(assignmentResponses)
                .page(assignmentPage.getNumber())
                .size(assignmentPage.getSize())
                .totalElements((int) assignmentPage.getTotalElements())
                .totalPages(assignmentPage.getTotalPages())
                .first(assignmentPage.isFirst())
                .last(assignmentPage.isLast())
                .empty(assignmentPage.isEmpty())
                .build();
    }

    /**
     * Search assignments with criteria
     */
    public PagedResponse<VehicleAssignmentResponse> searchAssignments(AssignmentSearchRequest searchRequest,
                                                                      UUID companyId, Pageable pageable) {
        logger.debug("Searching assignments for company: {} with criteria", companyId);

        Specification<VehicleAssignment> spec = createAssignmentSpecification(searchRequest, companyId);
        Page<VehicleAssignment> assignmentPage = assignmentRepository.findAll(spec, pageable);

        List<VehicleAssignmentResponse> assignmentResponses = assignmentPage.getContent().stream()
                .map(this::mapToVehicleAssignmentResponse)
                .collect(Collectors.toList());

        return PagedResponse.<VehicleAssignmentResponse>builder()
                .content(assignmentResponses)
                .page(assignmentPage.getNumber())
                .size(assignmentPage.getSize())
                .totalElements((int) assignmentPage.getTotalElements())
                .totalPages(assignmentPage.getTotalPages())
                .first(assignmentPage.isFirst())
                .last(assignmentPage.isLast())
                .empty(assignmentPage.isEmpty())
                .build();
    }

    /**
     * Get active assignments for a company
     */
    public List<VehicleAssignmentResponse> getActiveAssignments(UUID companyId) {
        logger.debug("Fetching active assignments for company: {}", companyId);

        List<VehicleAssignment> assignments = assignmentRepository.findByCompanyIdAndStatus(companyId, AssignmentStatus.ASSIGNED);

        return assignments.stream()
                .filter(VehicleAssignment::isActive)
                .map(this::mapToVehicleAssignmentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get assignments for a specific driver
     */
    @Cacheable(value = "driver-assignments", key = "#driverId")
    public List<VehicleAssignmentResponse> getDriverAssignments(UUID driverId, UUID companyId) {
        logger.debug("Fetching assignments for driver: {} in company: {}", driverId, companyId);

        List<VehicleAssignment> assignments = assignmentRepository.findByDriverIdAndCompanyId(driverId, companyId);

        return assignments.stream()
                .map(this::mapToVehicleAssignmentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get assignments for a specific vehicle
     */
    public List<VehicleAssignmentResponse> getVehicleAssignments(UUID vehicleId, UUID companyId) {
        logger.debug("Fetching assignments for vehicle: {} in company: {}", vehicleId, companyId);

        List<VehicleAssignment> assignments = assignmentRepository.findByVehicleIdAndCompanyId(vehicleId, companyId);

        return assignments.stream()
                .map(this::mapToVehicleAssignmentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get currently checked-in assignments
     */
    public List<VehicleAssignmentResponse> getCurrentlyCheckedInAssignments(UUID companyId) {
        logger.debug("Fetching currently checked-in assignments for company: {}", companyId);

        List<VehicleAssignment> assignments = assignmentRepository.findCurrentlyCheckedInAssignments(companyId);

        return assignments.stream()
                .map(this::mapToVehicleAssignmentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get expiring assignments
     */
    public List<VehicleAssignmentResponse> getExpiringAssignments(UUID companyId, int daysThreshold) {
        LocalDate endDate = LocalDate.now().plusDays(daysThreshold);
        List<VehicleAssignment> assignments = assignmentRepository.findExpiringAssignments(companyId, endDate);

        return assignments.stream()
                .map(this::mapToVehicleAssignmentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Validate assignment for conflicts
     */
    public AssignmentValidationResponse validateAssignment(AssignDriverRequest request, UUID companyId, UUID excludeAssignmentId) {
        logger.debug("Validating assignment for vehicle: {} and driver: {}", request.getVehicleId(), request.getDriverId());

        AssignmentValidationResponse.AssignmentValidationResponseBuilder responseBuilder =
                AssignmentValidationResponse.builder()
                        .isValid(true)
                        .hasConflicts(false)
                        .errors(new ArrayList<>())
                        .warnings(new ArrayList<>())
                        .conflicts(new ArrayList<>());

        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : LocalDate.now().plusYears(10);

        // Check for vehicle conflicts
        boolean vehicleConflict = assignmentRepository.hasVehicleConflict(
                request.getVehicleId(), companyId, request.getStartDate(), endDate, excludeAssignmentId);

        if (vehicleConflict) {
            List<VehicleAssignment> conflictingAssignments = assignmentRepository.findConflictingVehicleAssignments(
                    request.getVehicleId(), companyId, request.getStartDate(), endDate, excludeAssignmentId);

            for (VehicleAssignment conflict : conflictingAssignments) {
                AssignmentValidationResponse.ConflictInfo conflictInfo = AssignmentValidationResponse.ConflictInfo.builder()
                        .conflictType("VEHICLE_CONFLICT")
                        .conflictingAssignmentId(conflict.getId())
                        .conflictStartDate(conflict.getStartDate())
                        .conflictEndDate(conflict.getEndDate())
                        .description("Vehicle is already assigned to another driver during this period")
                        .build();
                responseBuilder.addConflict(conflictInfo);
            }
        }

        // Check for driver conflicts
        boolean driverConflict = assignmentRepository.hasDriverConflict(
                request.getDriverId(), companyId, request.getStartDate(), endDate, excludeAssignmentId);

        if (driverConflict) {
            List<VehicleAssignment> conflictingAssignments = assignmentRepository.findConflictingDriverAssignments(
                    request.getDriverId(), companyId, request.getStartDate(), endDate, excludeAssignmentId);

            for (VehicleAssignment conflict : conflictingAssignments) {
                AssignmentValidationResponse.ConflictInfo conflictInfo = AssignmentValidationResponse.ConflictInfo.builder()
                        .conflictType("DRIVER_CONFLICT")
                        .conflictingAssignmentId(conflict.getId())
                        .conflictStartDate(conflict.getStartDate())
                        .conflictEndDate(conflict.getEndDate())
                        .description("Driver is already assigned to another vehicle during this period")
                        .build();
                responseBuilder.addConflict(conflictInfo);
            }
        }

        return responseBuilder.build();
    }

    // Private helper methods

//    private void validateDriver(UUID driverId, UUID companyId) {
//        // This would call the User Service to validate the driver exists and is active
//        // For now, we'll implement a placeholder
//        try {
//            // userServiceClient.validateDriver(driverId, companyId);
//            logger.debug("Driver validation placeholder for driver: {}", driverId);
//        } catch (Exception e) {
//            throw new DriverValidationException("Driver