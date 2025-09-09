package com.fleetmanagement.vehicleservice.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Vehicle Group Membership Entity
 *
 * Represents the many-to-many relationship between vehicles and vehicle groups.
 * A vehicle can belong to multiple groups, and a group can contain multiple vehicles.
 */
@Entity
@Table(name = "vehicle_group_memberships",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_vehicle_group_membership",
                        columnNames = {"vehicle_id", "vehicle_group_id"})
        },
        indexes = {
                @Index(name = "idx_membership_vehicle_id", columnList = "vehicle_id"),
                @Index(name = "idx_membership_group_id", columnList = "vehicle_group_id"),
                @Index(name = "idx_membership_primary", columnList = "is_primary")
        })
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleGroupMembership {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    // Vehicle Reference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_membership_vehicle"))
    @NotNull(message = "Vehicle is required")
    private Vehicle vehicle;

    // Vehicle Group Reference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_group_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_membership_group"))
    @NotNull(message = "Vehicle group is required")
    private VehicleGroup vehicleGroup;

    // Membership Details
    @Column(name = "assigned_date", nullable = false)
    @Builder.Default
    private LocalDateTime assignedDate = LocalDateTime.now();

    @Column(name = "assigned_by")
    private UUID assignedBy;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;

    // Audit Fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Business Logic Methods

    /**
     * Check if this is the primary group for the vehicle
     */
    public boolean isPrimaryGroup() {
        return isPrimary != null && isPrimary;
    }

    /**
     * Set as primary group for the vehicle
     */
    public void setAsPrimary() {
        this.isPrimary = true;
    }

    /**
     * Remove primary status
     */
    public void removePrimaryStatus() {
        this.isPrimary = false;
    }

    /**
     * Get the vehicle ID
     */
    public UUID getVehicleId() {
        return vehicle != null ? vehicle.getId() : null;
    }

    /**
     * Get the group ID
     */
    public UUID getGroupId() {
        return vehicleGroup != null ? vehicleGroup.getId() : null;
    }

    /**
     * Check if membership is for the same company
     */
    public boolean isSameCompany() {
        if (vehicle == null || vehicleGroup == null) {
            return false;
        }
        return vehicle.getCompanyId().equals(vehicleGroup.getCompanyId());
    }

    /**
     * Get the company ID from the vehicle
     */
    public UUID getCompanyId() {
        return vehicle != null ? vehicle.getCompanyId() : null;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (assignedDate == null) {
            assignedDate = LocalDateTime.now();
        }
        if (isPrimary == null) {
            isPrimary = false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VehicleGroupMembership)) return false;
        VehicleGroupMembership that = (VehicleGroupMembership) o;
        return getVehicleId() != null && getGroupId() != null &&
                getVehicleId().equals(that.getVehicleId()) &&
                getGroupId().equals(that.getGroupId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}