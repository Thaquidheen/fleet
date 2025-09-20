

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
 * Device User Assignment Entity
 * Tracks assignment of devices to users (primarily for mobile devices)
 */
@Entity
@Table(name = "device_user_assignments", indexes = {
        @Index(name = "idx_device_user_device", columnList = "device_id"),
        @Index(name = "idx_device_user_user", columnList = "user_id"),
        @Index(name = "idx_device_user_status", columnList = "status"),
        @Index(name = "idx_device_user_active", columnList = "assigned_at, unassigned_at")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class DeviceUserAssignment {

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
     * User ID from user service
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * Company that owns both device and user
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
     * Current shift ID (for tracking purposes)
     */
    @Column(name = "current_shift_id")
    private String currentShiftId;

    /**
     * Whether tracking is currently enabled
     */
    @Column(name = "tracking_enabled", nullable = false)
    @Builder.Default
    private Boolean trackingEnabled = false;

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
     * Check if tracking is enabled
     */
    public boolean isTrackingEnabled() {
        return Boolean.TRUE.equals(trackingEnabled) && isActive();
    }

    /**
     * Start tracking with shift
     */
    public void startTracking(String shiftId) {
        this.trackingEnabled = true;
        this.currentShiftId = shiftId;
    }

    /**
     * Stop tracking
     */
    public void stopTracking() {
        this.trackingEnabled = false;
        this.currentShiftId = null;
    }

    /**
     * Unassign device from user
     */
    public void unassign(UUID unassignedBy, String reason) {
        this.status = AssignmentStatus.UNASSIGNED;
        this.unassignedAt = LocalDateTime.now();
        this.unassignedBy = unassignedBy;
        this.trackingEnabled = false;
        this.currentShiftId = null;
        if (reason != null) {
            this.notes = (this.notes != null ? this.notes + "; " : "") + "Unassigned: " + reason;
        }
    }
}