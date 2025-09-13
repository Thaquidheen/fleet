package com.fleetmanagement.vehicleservice.domain.entity;

import com.fleetmanagement.vehicleservice.domain.enums.GroupType;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import java.util.UUID;

/**
 * Vehicle Group Entity
 *
 * Represents a logical grouping of vehicles for fleet organization.
 * Supports hierarchical structure for departments, regions, and custom groupings.
 */
@Entity
@Table(name = "vehicle_groups",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_vehicle_groups_company_name",
                        columnNames = {"company_id", "name"})
        },
        indexes = {
                @Index(name = "idx_vehicle_groups_company_id", columnList = "company_id"),
                @Index(name = "idx_vehicle_groups_parent", columnList = "parent_group_id"),
                @Index(name = "idx_vehicle_groups_type", columnList = "group_type")
        })
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleGroup {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    @NotNull(message = "Company ID is required")
    private UUID companyId;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Group name is required")
    @Size(max = 100, message = "Group name must not exceed 100 characters")
    private String name;

    @Column(columnDefinition = "TEXT")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    // Hierarchical Structure
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_group_id", foreignKey = @ForeignKey(name = "fk_vehicle_group_parent"))
    private VehicleGroup parentGroup;

    @OneToMany(mappedBy = "parentGroup", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<VehicleGroup> childGroups = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "group_type", nullable = false)
    @Builder.Default
    private GroupType groupType = GroupType.CUSTOM;


    // Group Configuration
    @Column(name = "max_vehicles")
    @Min(value = 0, message = "Max vehicles must be non-negative")
    private Integer maxVehicles;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    // Additional Information
    @Column(length = 255)
    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;

    @Column(name = "manager_id")
    private UUID managerId;



    // Many-to-Many relationship with vehicles
    @OneToMany(mappedBy = "vehicleGroup", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<VehicleGroupMembership> vehicleMemberships = new ArrayList<>();

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
     * Check if this group can accept more vehicles
     */
    public boolean canAddVehicle() {
        if (maxVehicles == null) {
            return true; // No limit set
        }
        return getCurrentVehicleCount() < maxVehicles;
    }
    public void setActive(boolean active) {
        this.isActive = active;
    }
    /**
     * Get current vehicle count in this group
     */
    public int getCurrentVehicleCount() {
        if (vehicleMemberships == null) {
            return 0;
        }
        return (int) vehicleMemberships.stream()
                .filter(membership -> membership.getVehicle() != null)
                .count();
    }

    /**
     * Check if this is a root group (no parent)
     */
    public boolean isRootGroup() {
        return parentGroup == null;
    }

    /**
     * Check if this group has child groups
     */
    public boolean hasChildGroups() {
        return childGroups != null && !childGroups.isEmpty();
    }

    /**
     * Get the full path of this group (parent names separated by " > ")
     */
    public String getFullPath() {
        if (isRootGroup()) {
            return name;
        }
        return parentGroup.getFullPath() + " > " + name;
    }

    /**
     * Get the depth level of this group in the hierarchy
     */
    public int getDepthLevel() {
        if (isRootGroup()) {
            return 0;
        }
        return parentGroup.getDepthLevel() + 1;
    }

    /**
     * Check if this group is an ancestor of the given group
     */
    public boolean isAncestorOf(VehicleGroup group) {
        if (group == null || group.getParentGroup() == null) {
            return false;
        }

        VehicleGroup currentParent = group.getParentGroup();
        while (currentParent != null) {
            if (this.getId().equals(currentParent.getId())) {
                return true;
            }
            currentParent = currentParent.getParentGroup();
        }
        return false;
    }

    /**
     * Check if this group is a descendant of the given group
     */
    public boolean isDescendantOf(VehicleGroup group) {
        return group != null && group.isAncestorOf(this);
    }

    /**
     * Get all ancestor groups
     */
    public List<VehicleGroup> getAncestors() {
        List<VehicleGroup> ancestors = new ArrayList<>();
        VehicleGroup currentParent = this.parentGroup;

        while (currentParent != null) {
            ancestors.add(currentParent);
            currentParent = currentParent.getParentGroup();
        }

        return ancestors;
    }

    /**
     * Get all descendant groups (recursively)
     */
    public List<VehicleGroup> getAllDescendants() {
        List<VehicleGroup> descendants = new ArrayList<>();

        if (childGroups != null) {
            for (VehicleGroup child : childGroups) {
                descendants.add(child);
                descendants.addAll(child.getAllDescendants());
            }
        }

        return descendants;
    }

    /**
     * Add a child group
     */
    public void addChildGroup(VehicleGroup childGroup) {
        if (childGroups == null) {
            childGroups = new ArrayList<>();
        }

        if (childGroup != null && !childGroups.contains(childGroup)) {
            childGroups.add(childGroup);
            childGroup.setParentGroup(this);
        }
    }

    /**
     * Remove a child group
     */
    public void removeChildGroup(VehicleGroup childGroup) {
        if (childGroups != null && childGroup != null) {
            childGroups.remove(childGroup);
            childGroup.setParentGroup(null);
        }
    }

    /**
     * Activate the group
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Deactivate the group
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Update group configuration
     */
    public void updateConfiguration(String name, String description, Integer maxVehicles, String location) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
        this.description = description;
        this.maxVehicles = maxVehicles;
        this.location = location;
    }

    /**
     * Set manager for this group
     */
    public void setManager(UUID managerId) {
        this.managerId = managerId;
    }

    /**
     * Check if group has a manager assigned
     */
    public boolean hasManager() {
        return managerId != null;
    }

    /**
     * Validate group hierarchy to prevent circular references
     */
    public boolean isValidHierarchy(VehicleGroup newParent) {
        if (newParent == null) {
            return true; // Root group is always valid
        }

        if (this.getId().equals(newParent.getId())) {
            return false; // Cannot be parent of itself
        }

        return !this.isAncestorOf(newParent); // Cannot create circular reference
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = true;
        }
        if (sortOrder == null) {
            sortOrder = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}