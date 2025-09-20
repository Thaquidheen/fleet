package com.fleetmanagement.deviceservice.domain.entity;

import com.fleetmanagement.deviceservice.domain.enums.AssignmentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Device Vehicle Assignment Entity
 * Tracks assignment of devices to vehicles
 */
@Entity
@Table(name = "device_vehicle_assignments", indexes = {
        @Index(name = "idx_device_vehicle_device", columnList = "device_id"),
        @Index(name = "idx_device_vehicle_vehicle", columnList = "vehicle_id"),
        @Index(name = "idx_device_vehicle_status", columnList = "status"),
        @Index(name = "idx_device_vehicle_active", columnList = "assigned_at, unassigned_at")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class DeviceVehicleAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Device being assigned
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    /**
     * Vehicle ID from vehicle service
     */
    @Column(name = "vehicle_id", nullable = false)
    private UUID vehicleId;

    /**
     * Company that owns both device and vehicle
     */
    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    /**
     * Assignment status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private AssignmentStatus status = AssignmentStatus.ASSIGNED;

    /**
     * Assignment timestamp
     */
    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    /**
     * Unassignment timestamp
     */
    @Column(name = "unassigned_at")
    private LocalDateTime unassignedAt;

    /**
     * User who made the assignment
     */
    @Column(name = "assigned_by", nullable = false)
    private UUID assignedBy;

    /**
     * User who removed the assignment
     */
    @Column(name = "unassigned_by")
    private UUID unassignedBy;

    /**
     * Assignment notes
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * Installation location in vehicle
     */
    @Column(name = "installation_location", length = 100)
    private String installationLocation;

    /**
     * Installation date
     */
    @Column(name = "installation_date")
    private LocalDateTime installationDate;

    /**
     * Installation technician
     */
    @Column(name = "installation_technician", length = 100)
    private String installationTechnician;

    /**
     * Audit fields
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Business methods

    /**
     * Check if assignment is currently active
     */
    public boolean isActive() {
        return status == AssignmentStatus.ASSIGNED && unassignedAt == null;
    }

    /**
     * Unassign device from vehicle
     */
    public void unassign(UUID unassignedBy, String reason) {
        this.status = AssignmentStatus.UNASSIGNED;
        this.unassignedAt = LocalDateTime.now();
        this.unassignedBy = unassignedBy;
        if (reason != null) {
            this.notes = (this.notes != null ? this.notes + "; " : "") + "Unassigned: " + reason;
        }
    }
}