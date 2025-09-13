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

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.UUID;

@Entity
@Table(name = "vehicle_assignments")
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

    // FIXED: Added vehicleId field that was missing
    @Column(name = "vehicle_id", nullable = false)
    @NotNull(message = "Vehicle ID is required")
    private UUID vehicleId;

    // Vehicle relationship
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", insertable = false, updatable = false)
    private Vehicle vehicle;

    @Column(name = "driver_id", nullable = false)
    @NotNull(message = "Driver ID is required")
    private UUID driverId;

    @Column(name = "company_id", nullable = false)
    @NotNull(message = "Company ID is required")
    private UUID companyId;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_type", length = 20, nullable = false)
    @Builder.Default
    private AssignmentType assignmentType = AssignmentType.PERMANENT;

    // FIXED: Added missing check-in/check-out time fields
    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    // FIXED: Added missing assignedBy field
    @Column(name = "assigned_by", nullable = false)
    @NotNull(message = "Assigned by is required")
    private UUID assignedBy;

    @Column(columnDefinition = "TEXT")
    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    // Business Logic Methods
    public boolean isActive() {
        return status == AssignmentStatus.ASSIGNED || status == AssignmentStatus.ACTIVE;
    }

    public boolean isCheckedIn() {
        return status == AssignmentStatus.CHECKED_IN;
    }

    // Explicit getters for fields that were causing compilation errors
    public UUID getVehicleId() { return vehicleId; }
    public void setVehicleId(UUID vehicleId) { this.vehicleId = vehicleId; }

    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }

    public LocalDateTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalDateTime checkOutTime) { this.checkOutTime = checkOutTime; }

    public UUID getAssignedBy() { return assignedBy; }
    public void setAssignedBy(UUID assignedBy) { this.assignedBy = assignedBy; }
}
