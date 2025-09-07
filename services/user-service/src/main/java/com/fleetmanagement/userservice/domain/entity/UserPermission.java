package com.fleetmanagement.userservice.domain.entity;

import com.fleetmanagement.userservice.domain.enums.PermissionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_permissions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "resource", "permission_type"}),
        indexes = {
                @Index(name = "idx_permission_user_id", columnList = "user_id"),
                @Index(name = "idx_permission_resource", columnList = "resource"),
                @Index(name = "idx_permission_type", columnList = "permission_type")
        })
public class UserPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "Resource is required")
    @Size(max = 100, message = "Resource must not exceed 100 characters")
    @Column(name = "resource", nullable = false, length = 100)
    private String resource;

    @NotNull(message = "Permission type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "permission_type", nullable = false, columnDefinition = "permission_type")
    private PermissionType permissionType;

    @Column(name = "granted", nullable = false)
    private Boolean granted = true;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "granted_by")
    private UUID grantedBy;

    // Constructors
    public UserPermission() {}

    public UserPermission(User user, String resource, PermissionType permissionType,
                          Boolean granted, UUID grantedBy) {
        this.user = user;
        this.resource = resource;
        this.permissionType = permissionType;
        this.granted = granted;
        this.grantedBy = grantedBy;
    }

    // Business Methods
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isValid() {
        return granted && !isExpired();
    }

    public void revoke() {
        this.granted = false;
    }

    public void grant() {
        this.granted = true;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }

    public PermissionType getPermissionType() { return permissionType; }
    public void setPermissionType(PermissionType permissionType) {
        this.permissionType = permissionType;
    }

    public Boolean getGranted() { return granted; }
    public void setGranted(Boolean granted) { this.granted = granted; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public UUID getGrantedBy() { return grantedBy; }
    public void setGrantedBy(UUID grantedBy) { this.grantedBy = grantedBy; }
}