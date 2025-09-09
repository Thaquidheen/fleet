package com.fleetmanagement.vehicleservice.domain.entity;

import com.fleetmanagement.vehicleservice.domain.enums.AssignmentStatus;
import com.fleetmanagement.vehicleservice.domain.enums.AssignmentType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

/**
 * Vehicle Assignment Entity
 *
 * Represents the assignment of a vehicle to a driver with time tracking,
 * check-in/check-out functionality, and assignment history.
 */
@Entity
@Table(name = "vehicle_assignments",
        indexes = {
                @Index(name = "idx_vehicle_assignments_vehicle_id", columnList = "vehicle_id"),
                @Index(name = "idx_vehicle_assignments_driver_id", columnList = "driver_id"),
                @Index(name = "idx_vehicle_assignments_company_id", columnList = "company_id"),
                @Index(name = "idx_vehicle_assignments_status", columnList = "status"),
                @Index(name = "idx_vehicle_assignments_dates", columnList = "start_date, end_date")
        })
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleAssignment {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    // Vehicle and Driver Relationship
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_assignment_vehicle"))
    @NotNull(message = "Vehicle is required")
    private Vehicle vehicle;

    @Column(name = "driver_id", nullable = false)
    @NotNull(message = "Driver ID is required")
    private UUID driverId;

    @Column(name = "company_id", nullable = false)
    @NotNull(message = "Company ID is required")
    private UUID companyId;

    // Assignment Details
    @Column(name = "assigned_date", nullable = false)
    @NotNull(message = "Assigned date is required")
    @Builder.Default
    private LocalDateTime assignedDate = LocalDateTime.now();

    @Column(name = "start_date", nullable = false)
    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AssignmentStatus status = AssignmentStatus.ASSIGNED;

    // Assignment Type
    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_type", length = 20, nullable = false)
    @Builder.Default
    private AssignmentType assignmentType = AssignmentType.PERMANENT;

    @Column(name = "shift_start_time")
    private LocalTime shiftStartTime;

    @Column(name = "shift_end_time")
    private LocalTime shiftEndTime;

    // Check-in/Check-out Tracking
    @Column(name = "last_checkin_time")
    private LocalDateTime lastCheckinTime;

    @Column(name = "last_checkout_time")
    private LocalDateTime lastCheckoutTime;

    @Column(name = "checkin_location_lat", precision = 10, scale = 8)
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private BigDecimal checkinLocationLat;

    @Column(name = "checkin_location_lng", precision = 11, scale = 8)
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private BigDecimal checkinLocationLng;

    // Assignment Notes and Restrictions
    @Column(columnDefinition = "TEXT")
    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;

    @Column(name = "restrictions", columnDefinition = "jsonb")
    private Map<String, Object> restrictions;

    // Audit Fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    // Business Logic Methods

    /**
     * Check if the assignment is currently active
     */
    public boolean isActive() {
        return status == AssignmentStatus.ASSIGNED || status == AssignmentStatus.TEMPORARY;
    }

    /**
     * Check if the assignment is expired
     */
    public boolean isExpired() {
        if (endDate == null) {
            return false; // Permanent assignment
        }
        return endDate.isBefore(LocalDate.now());
    }

    /**
     * Check if the assignment is valid for the given date
     */
    public boolean isValidForDate(LocalDate date) {
        if (date.isBefore(startDate)) {
            return false;
        }
        if (endDate != null && date.isAfter(endDate)) {
            return false;
        }
        return isActive();
    }

    /**
     * Check if the assignment is currently in shift time
     */
    public boolean isInShiftTime() {
        if (shiftStartTime == null || shiftEndTime == null) {
            return true; // No shift restrictions
        }

        LocalTime now = LocalTime.now();
        return !now.isBefore(shiftStartTime) && !now.isAfter(shiftEndTime);
    }

    /**
     * Check if driver is currently checked in
     */
    public boolean isCheckedIn() {
        return lastCheckinTime != null &&
                (lastCheckoutTime == null || lastCheckinTime.isAfter(lastCheckoutTime));
    }

    /**
     * Get total assignment duration in days
     */
    public long getAssignmentDurationDays() {
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, end);
    }

    /**
     * Check if assignment has overlapping dates with another assignment
     */
    public boolean hasOverlapWith(VehicleAssignment other) {
        if (other == null) {
            return false;
        }

        LocalDate thisStart = this.startDate;
        LocalDate thisEnd = this.endDate != null ? this.endDate : LocalDate.MAX;
        LocalDate otherStart = other.startDate;
        LocalDate otherEnd = other.endDate != null ? other.endDate : LocalDate.MAX;

        return !(thisEnd.isBefore(otherStart) || thisStart.isAfter(otherEnd));
    }

    /**
     * Check in the driver
     */
    public void checkIn(BigDecimal latitude, BigDecimal longitude) {
        if (!isActive()) {
            throw new IllegalStateException("Cannot check in - assignment is not active");
        }

        this.lastCheckinTime = LocalDateTime.now();
        this.checkinLocationLat = latitude;
        this.checkinLocationLng = longitude;
    }

    /**
     * Check out the driver
     */
    public void checkOut() {
        if (!isCheckedIn()) {
            throw new IllegalStateException("Cannot check out - driver is not checked in");
        }

        this.lastCheckoutTime = LocalDateTime.now();
    }

    /**
     * Extend the assignment end date
     */
    public void extendAssignment(LocalDate newEndDate) {
        if (newEndDate != null && newEndDate.isBefore(startDate)) {
            throw new IllegalArgumentException("New end date cannot be before start date");
        }
        this.endDate = newEndDate;
    }

    /**
     * Terminate the assignment
     */
    public void terminate() {
        this.status = AssignmentStatus.EXPIRED;
        this.endDate = LocalDate.now();

        // Auto check-out if checked in
        if (isCheckedIn()) {
            checkOut();
        }
    }

    /**
     * Reactivate the assignment
     */
    public void reactivate() {
        if (isExpired()) {
            throw new IllegalStateException("Cannot reactivate expired assignment");
        }
        this.status = AssignmentStatus.ASSIGNED;
    }

    /**
     * Set shift times for shift-based assignments
     */
    public void setShiftTimes(LocalTime startTime, LocalTime endTime) {
        if (assignmentType != AssignmentType.SHIFT) {
            throw new IllegalStateException("Can only set shift times for shift-based assignments");
        }

        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Shift start time cannot be after end time");
        }

        this.shiftStartTime = startTime;
        this.shiftEndTime = endTime;
    }

    /**
     * Update assignment notes
     */
    public void updateNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Add restriction to the assignment
     */
    public void addRestriction(String key, Object value) {
        if (restrictions == null) {
            restrictions = new java.util.HashMap<>();
        }
        restrictions.put(key, value);
    }

    /**
     * Remove restriction from the assignment
     */
    public void removeRestriction(String key) {
        if (restrictions != null) {
            restrictions.remove(key);
        }
    }

    /**
     * Check if assignment has a specific restriction
     */
    public boolean hasRestriction(String key) {
        return restrictions != null && restrictions.containsKey(key);
    }

    /**
     * Get restriction value
     */
    public Object getRestriction(String key) {
        return restrictions != null ? restrictions.get(key) : null;
    }

    /**
     * Validate assignment dates
     */
    public boolean isValidAssignmentPeriod() {
        if (endDate == null) {
            return true; // Permanent assignment
        }
        return !endDate.isBefore(startDate);
    }

    /**
     * Get remaining assignment days
     */
    public Long getRemainingDays() {
        if (endDate == null) {
            return null; // Permanent assignment
        }

        LocalDate now = LocalDate.now();
        if (endDate.isBefore(now)) {
            return 0L; // Expired
        }

        return java.time.temporal.ChronoUnit.DAYS.between(now, endDate);
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (assignedDate == null) {
            assignedDate = LocalDateTime.now();
        }
        if (status == null) {
            status = AssignmentStatus.ASSIGNED;
        }
        if (assignmentType == null) {
            assignmentType = AssignmentType.PERMANENT;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Validation Methods
    @AssertTrue(message = "End date must be after start date")
    private boolean isEndDateValid() {
        return isValidAssignmentPeriod();
    }

    @AssertTrue(message = "Shift times are required for shift-based assignments")
    private boolean areShiftTimesValid() {
        if (assignmentType == AssignmentType.SHIFT) {
            return shiftStartTime != null && shiftEndTime != null;
        }
        return true;
    }
}