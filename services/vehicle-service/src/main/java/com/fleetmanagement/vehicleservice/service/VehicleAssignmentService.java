package com.fleetmanagement.vehicleservice.service;

import com.fleetmanagement.vehicleservice.client.UserServiceClient;
import com.fleetmanagement.vehicleservice.client.UserServiceClient.DriverValidationResponse;
import com.fleetmanagement.vehicleservice.client.UserServiceClient.DriverAssignmentNotification;
import com.fleetmanagement.vehicleservice.domain.entity.Vehicle;
import com.fleetmanagement.vehicleservice.domain.entity.VehicleAssignment;
import com.fleetmanagement.vehicleservice.domain.enums.AssignmentStatus;
import com.fleetmanagement.vehicleservice.domain.enums.AssignmentType;
import com.fleetmanagement.vehicleservice.dto.request.AssignDriverRequest;
import com.fleetmanagement.vehicleservice.dto.response.VehicleAssignmentResponse;
import com.fleetmanagement.vehicleservice.exception.DriverNotAvailableException;
import com.fleetmanagement.vehicleservice.exception.VehicleAssignmentConflictException;
import com.fleetmanagement.vehicleservice.exception.VehicleNotFoundException;
import com.fleetmanagement.vehicleservice.repository.VehicleAssignmentRepository;
import com.fleetmanagement.vehicleservice.repository.VehicleRepository;
import org.slf4j.Logger;



import java.util.List;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.UUID;

@Service
@Transactional
public class VehicleAssignmentService {

    private static final Logger logger = LoggerFactory.getLogger(VehicleAssignmentService.class);

    private final VehicleAssignmentRepository assignmentRepository;
    private final VehicleRepository vehicleRepository;
    private final UserServiceClient userServiceClient;

    @Autowired
    public VehicleAssignmentService(VehicleAssignmentRepository assignmentRepository,
                                    VehicleRepository vehicleRepository,
                                    UserServiceClient userServiceClient) {
        this.assignmentRepository = assignmentRepository;
        this.vehicleRepository = vehicleRepository;
        this.userServiceClient = userServiceClient;
    }

    /**
     * Assign driver to vehicle with complete validation workflow
     */
    @Transactional
    @CacheEvict(value = {"assignments", "driverAssignments", "vehicleAssignments"}, allEntries = true)
    public VehicleAssignmentResponse assignDriverToVehicle(AssignDriverRequest request, UUID companyId, UUID assignedBy) {
        logger.info("Assigning driver {} to vehicle {} for company {}",
                request.getDriverId(), request.getVehicleId(), companyId);

        // 1. VALIDATE DRIVER AVAILABILITY
        validateDriverAvailability(request.getDriverId(), companyId);

        // 2. VALIDATE VEHICLE AVAILABILITY
        validateVehicleAvailability(request.getVehicleId(), companyId);

        // 3. CHECK FOR ASSIGNMENT CONFLICTS
        validateAssignmentConflicts(request.getDriverId(), request.getVehicleId(),
                request.getStartDate(), request.getEndDate());

        // 4. GET VEHICLE DETAILS FOR NOTIFICATION
        Vehicle vehicle = vehicleRepository.findByIdAndCompanyId(request.getVehicleId(), companyId)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found: " + request.getVehicleId()));

        // 5. CREATE ASSIGNMENT
        VehicleAssignment assignment = VehicleAssignment.builder()
                .vehicleId(request.getVehicleId())
                .driverId(request.getDriverId())
                .companyId(companyId)
                .assignmentType(request.getAssignmentType() != null ? request.getAssignmentType() : AssignmentType.TEMPORARY)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(AssignmentStatus.ACTIVE)
                .notes(request.getNotes())
                .assignedBy(assignedBy)
                .createdBy(assignedBy)
                .updatedBy(assignedBy)
                .build();

        VehicleAssignment savedAssignment = assignmentRepository.save(assignment);
        logger.info("Vehicle assignment created successfully with ID: {}", savedAssignment.getId());

        // 6. NOTIFY USER SERVICE ABOUT ASSIGNMENT
        notifyDriverAssignment(request.getDriverId(), vehicle, assignment);

        return mapToResponse(savedAssignment);
    }

    /**
     * Validate driver availability with User Service
     */
    private void validateDriverAvailability(UUID driverId, UUID companyId) {
        try {
            logger.debug("Validating driver availability: {} for company: {}", driverId, companyId);

            DriverValidationResponse validation = userServiceClient.validateDriver(driverId, companyId);

            if (validation == null) {
                throw new DriverNotAvailableException("Unable to validate driver availability");
            }
            if (!validation.isValid()) {
                String errors = validation.getValidationErrors() != null
                        ? String.join(", ", validation.getValidationErrors())
                        : "Driver validation failed";
                throw new DriverNotAvailableException("Driver validation failed: " + errors);
            }
            if (!validation.isAvailable()) {
                String reason = validation.getUnavailabilityReason() != null
                        ? validation.getUnavailabilityReason()
                        : "Driver is not available";
                throw new DriverNotAvailableException("Driver not available: " + reason);
            }
            logger.info("Driver availability validated successfully: {}", driverId);
        } catch (DriverNotAvailableException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to validate driver availability for driver: {}", driverId, e);
            throw new DriverNotAvailableException("Unable to validate driver availability: " + e.getMessage());
        }
    }

    /**
     * Validate vehicle availability for assignment
     */
    private void validateVehicleAvailability(UUID vehicleId, UUID companyId) {
        // Check if vehicle exists and belongs to company
        Vehicle vehicle = vehicleRepository.findByIdAndCompanyId(vehicleId, companyId)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found: " + vehicleId));

        // Check if vehicle is in assignable status
        if (vehicle.getStatus() != com.fleetmanagement.vehicleservice.domain.enums.VehicleStatus.ACTIVE) {
            throw new VehicleAssignmentConflictException("Vehicle is not available for assignment. Status: " + vehicle.getStatus());
        }

        logger.debug("Vehicle availability validated: {}", vehicleId);
    }

    /**
     * Check for assignment conflicts
     */
    private void validateAssignmentConflicts(UUID driverId, UUID vehicleId, LocalDate startDate, LocalDate endDate) {
        // Check for overlapping driver assignments
        List<VehicleAssignment> driverConflicts = assignmentRepository.findOverlappingDriverAssignments(
                driverId, startDate, endDate);

        if (!driverConflicts.isEmpty()) {
            throw new VehicleAssignmentConflictException(
                    "Driver is already assigned to another vehicle during this period");
        }

        // Check for overlapping vehicle assignments
        List<VehicleAssignment> vehicleConflicts = assignmentRepository.findOverlappingVehicleAssignments(
                vehicleId, startDate, endDate);

        if (!vehicleConflicts.isEmpty()) {
            throw new VehicleAssignmentConflictException(
                    "Vehicle is already assigned to another driver during this period");
        }

        logger.debug("No assignment conflicts found for driver: {} and vehicle: {}", driverId, vehicleId);
    }

    /**
     * Notify User Service about driver assignment
     */
    private void notifyDriverAssignment(UUID driverId, Vehicle vehicle, VehicleAssignment assignment) {
        try {
            DriverAssignmentNotification notification = new DriverAssignmentNotification();
            notification.setVehicleId(vehicle.getId());
            notification.setVehicleName(vehicle.getName());
            notification.setLicensePlate(vehicle.getLicensePlate());
            notification.setCompanyId(vehicle.getCompanyId());
            notification.setAssignmentStartDate(assignment.getStartDate());
            notification.setAssignmentEndDate(assignment.getEndDate());
            if (assignment.getAssignmentType() != null) {
                notification.setAssignmentType(assignment.getAssignmentType().name());
            }

            userServiceClient.notifyDriverAssignment(driverId, notification);
            logger.info("Driver assignment notification sent successfully: {}", driverId);

        } catch (Exception e) {
            logger.error("Failed to notify driver assignment for driver: {}", driverId, e);
            // Note: This doesn't fail the assignment creation, but should be monitored
        }
    }

    /**
     * Terminate assignment with notifications
     */
    @Transactional
    @CacheEvict(value = {"assignments", "driverAssignments", "vehicleAssignments"}, allEntries = true)
    public VehicleAssignmentResponse terminateAssignment(UUID assignmentId, UUID companyId, UUID terminatedBy) {
        logger.info("Terminating assignment: {} for company: {}", assignmentId, companyId);

        VehicleAssignment assignment = assignmentRepository.findByIdAndCompanyId(assignmentId, companyId)
                .orElseThrow(() -> new VehicleAssignmentConflictException("Assignment not found: " + assignmentId));

        if (assignment.getStatus() != AssignmentStatus.ACTIVE) {
            throw new VehicleAssignmentConflictException("Assignment is not active: " + assignment.getStatus());
        }

        // Update assignment status
        assignment.setStatus(AssignmentStatus.TERMINATED);
        assignment.setEndDate(LocalDate.now());
        assignment.setUpdatedBy(terminatedBy);
        assignment.setUpdatedAt(LocalDateTime.now());

        VehicleAssignment updatedAssignment = assignmentRepository.save(assignment);

        // NOTIFY USER SERVICE ABOUT UNASSIGNMENT
        try {
            userServiceClient.notifyDriverUnassignment(assignment.getDriverId(), assignment.getVehicleId());
            logger.info("Driver unassignment notification sent: {}", assignment.getDriverId());
        } catch (Exception e) {
            logger.error("Failed to notify driver unassignment: {}", assignment.getDriverId(), e);
        }

        logger.info("Assignment terminated successfully: {}", assignmentId);
        return mapToResponse(updatedAssignment);
    }

    /**
     * Get assignments for a specific driver
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "driverAssignments", key = "#driverId + '_' + #companyId")
    public List<VehicleAssignmentResponse> getDriverAssignments(UUID driverId, UUID companyId) {
        logger.debug("Retrieving assignments for driver: {} in company: {}", driverId, companyId);

        List<VehicleAssignment> assignments = assignmentRepository.findByDriverIdAndCompanyIdOrderByStartDateDesc(
                driverId, companyId);

        return assignments.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get assignments for a specific vehicle
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "vehicleAssignments", key = "#vehicleId + '_' + #companyId")
    public List<VehicleAssignmentResponse> getVehicleAssignments(UUID vehicleId, UUID companyId) {
        logger.debug("Retrieving assignments for vehicle: {} in company: {}", vehicleId, companyId);

        List<VehicleAssignment> assignments = assignmentRepository.findByVehicleIdAndCompanyIdOrderByStartDateDesc(
                vehicleId, companyId);

        return assignments.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get currently active assignments
     */
    @Transactional(readOnly = true)
    public List<VehicleAssignmentResponse> getActiveAssignments(UUID companyId) {
        logger.debug("Retrieving active assignments for company: {}", companyId);

        List<VehicleAssignment> assignments = assignmentRepository.findActiveAssignmentsByCompany(companyId);

        return assignments.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Check-in assignment
     */
    @Transactional
    @CacheEvict(value = {"assignments", "driverAssignments", "vehicleAssignments"}, allEntries = true)
    public VehicleAssignmentResponse checkInAssignment(UUID assignmentId, UUID companyId, UUID checkedInBy) {
        logger.info("Checking in assignment: {} for company: {}", assignmentId, companyId);

        VehicleAssignment assignment = assignmentRepository.findByIdAndCompanyId(assignmentId, companyId)
                .orElseThrow(() -> new VehicleAssignmentConflictException("Assignment not found: " + assignmentId));

        if (assignment.getStatus() != AssignmentStatus.ACTIVE) {
            throw new VehicleAssignmentConflictException("Assignment is not active for check-in");
        }

        assignment.setStatus(AssignmentStatus.CHECKED_IN);
        assignment.setCheckInTime(LocalDateTime.now());
        assignment.setUpdatedBy(checkedInBy);
        assignment.setUpdatedAt(LocalDateTime.now());

        VehicleAssignment updatedAssignment = assignmentRepository.save(assignment);
        logger.info("Assignment checked in successfully: {}", assignmentId);

        return mapToResponse(updatedAssignment);
    }

    /**
     * Check-out assignment
     */
    @Transactional
    @CacheEvict(value = {"assignments", "driverAssignments", "vehicleAssignments"}, allEntries = true)
    public VehicleAssignmentResponse checkOutAssignment(UUID assignmentId, UUID companyId, UUID checkedOutBy) {
        logger.info("Checking out assignment: {} for company: {}", assignmentId, companyId);

        VehicleAssignment assignment = assignmentRepository.findByIdAndCompanyId(assignmentId, companyId)
                .orElseThrow(() -> new VehicleAssignmentConflictException("Assignment not found: " + assignmentId));

        if (assignment.getStatus() != AssignmentStatus.CHECKED_IN) {
            throw new VehicleAssignmentConflictException("Assignment is not checked in for check-out");
        }

        assignment.setStatus(AssignmentStatus.CHECKED_OUT);
        assignment.setCheckOutTime(LocalDateTime.now());
        assignment.setUpdatedBy(checkedOutBy);
        assignment.setUpdatedAt(LocalDateTime.now());

        VehicleAssignment updatedAssignment = assignmentRepository.save(assignment);
        logger.info("Assignment checked out successfully: {}", assignmentId);

        return mapToResponse(updatedAssignment);
    }

    /**
     * Map entity to response DTO
     */
    private VehicleAssignmentResponse mapToResponse(VehicleAssignment assignment) {
        return VehicleAssignmentResponse.builder()
                .id(assignment.getId())
                .vehicleId(assignment.getVehicleId())
                .driverId(assignment.getDriverId())
                .companyId(assignment.getCompanyId())
                .assignmentType(assignment.getAssignmentType())
                .status(assignment.getStatus())
                .startDate(assignment.getStartDate())
                .endDate(assignment.getEndDate())
                .lastCheckIn(assignment.getCheckInTime())
                .lastCheckOut(assignment.getCheckOutTime())
                .notes(assignment.getNotes())
                .assignedBy(assignment.getAssignedBy())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .build();
    }
}