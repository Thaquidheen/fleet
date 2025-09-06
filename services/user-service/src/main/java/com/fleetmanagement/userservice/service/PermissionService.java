// PermissionService.java
package com.fleetmanagement.userservice.service;

import com.fleetmanagement.userservice.domain.entity.User;
import com.fleetmanagement.userservice.domain.entity.UserPermission;
import com.fleetmanagement.userservice.domain.enums.PermissionType;
import com.fleetmanagement.userservice.domain.enums.UserRole;
import com.fleetmanagement.userservice.exception.ResourceNotFoundException;
import com.fleetmanagement.userservice.repository.UserPermissionRepository;
import com.fleetmanagement.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PermissionService {

    private static final Logger logger = LoggerFactory.getLogger(PermissionService.class);

    private final UserPermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final CacheService cacheService;

    // Define resource categories and their default permissions
    private static final Map<String, Set<PermissionType>> DEFAULT_ROLE_PERMISSIONS = new HashMap<>();

    static {
        // SUPER_ADMIN - Full access to everything
        DEFAULT_ROLE_PERMISSIONS.put("SUPER_ADMIN", Set.of(
                PermissionType.READ, PermissionType.WRITE, PermissionType.DELETE,
                PermissionType.ADMIN, PermissionType.EXECUTE, PermissionType.APPROVE,
                PermissionType.EXPORT, PermissionType.IMPORT
        ));

        // COMPANY_ADMIN - Full access within company scope
        DEFAULT_ROLE_PERMISSIONS.put("COMPANY_ADMIN", Set.of(
                PermissionType.READ, PermissionType.WRITE, PermissionType.DELETE,
                PermissionType.ADMIN, PermissionType.APPROVE, PermissionType.EXPORT, PermissionType.IMPORT
        ));

        // FLEET_MANAGER - Manage fleet operations
        DEFAULT_ROLE_PERMISSIONS.put("FLEET_MANAGER", Set.of(
                PermissionType.READ, PermissionType.WRITE, PermissionType.EXPORT
        ));

        // DRIVER - Limited access to assigned vehicles
        DEFAULT_ROLE_PERMISSIONS.put("DRIVER", Set.of(
                PermissionType.READ
        ));

        // VIEWER - Read-only access
        DEFAULT_ROLE_PERMISSIONS.put("VIEWER", Set.of(
                PermissionType.READ
        ));
    }

    @Autowired
    public PermissionService(UserPermissionRepository permissionRepository,
                             UserRepository userRepository,
                             CacheService cacheService) {
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
        this.cacheService = cacheService;
    }

    /**
     * Check if user has specific permission on a resource
     */
    public boolean hasPermission(UUID userId, String resource, PermissionType permissionType) {
        try {
            // Check cache first
            String cacheKey = String.format("%s:%s:%s", userId, resource, permissionType);
            Object cached = cacheService.get("permission:" + cacheKey);
            if (cached instanceof Boolean) {
                return (Boolean) cached;
            }

            // Get user and check role-based permissions first
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Super admins have all permissions
            if (user.getRole() == UserRole.SUPER_ADMIN) {
                cacheService.set("permission:" + cacheKey, true, java.time.Duration.ofMinutes(15));
                return true;
            }

            // Check role-based default permissions
            boolean hasRolePermission = hasRoleBasedPermission(user.getRole(), resource, permissionType);

            // Check database for explicit permissions (can override role permissions)
            boolean hasExplicitPermission = permissionRepository.hasPermission(
                    userId, resource, permissionType, LocalDateTime.now());

            // User has permission if either role allows it OR explicit permission is granted
            boolean result = hasRolePermission || hasExplicitPermission;

            // Cache the result
            cacheService.set("permission:" + cacheKey, result, java.time.Duration.ofMinutes(15));

            logger.debug("Permission check for user {} on resource {} with type {}: {} (role: {}, explicit: {})",
                    userId, resource, permissionType, result, hasRolePermission, hasExplicitPermission);

            return result;

        } catch (Exception e) {
            logger.error("Error checking permission for user {} on resource {}: {}",
                    userId, resource, e.getMessage());
            return false;
        }
    }

    /**
     * Check multiple permissions at once
     */
    public Map<String, Boolean> hasPermissions(UUID userId, String resource, PermissionType... permissionTypes) {
        Map<String, Boolean> results = new HashMap<>();

        for (PermissionType permissionType : permissionTypes) {
            results.put(permissionType.name(), hasPermission(userId, resource, permissionType));
        }

        return results;
    }

    /**
     * Check if user can access resource in company context
     */
    public boolean hasCompanyPermission(UUID userId, String resource, PermissionType permissionType, UUID resourceCompanyId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Super admins can access any company's resources
            if (user.getRole() == UserRole.SUPER_ADMIN) {
                return hasPermission(userId, resource, permissionType);
            }

            // Users can only access resources from their own company
            if (!user.getCompanyId().equals(resourceCompanyId)) {
                return false;
            }

            return hasPermission(userId, resource, permissionType);

        } catch (Exception e) {
            logger.error("Error checking company permission: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get all user permissions
     */
    @Transactional(readOnly = true)
    public List<UserPermission> getUserPermissions(UUID userId) {
        return permissionRepository.findByUserIdAndGrantedTrue(userId);
    }

    /**
     * Get user permissions by resource
     */
    @Transactional(readOnly = true)
    public List<UserPermission> getUserPermissionsByResource(UUID userId, String resource) {
        return permissionRepository.findByUserIdAndResource(userId, resource);
    }

    /**
     * Get paginated user permissions
     */
    @Transactional(readOnly = true)
    public Page<UserPermission> getUserPermissions(UUID userId, Pageable pageable) {
        return permissionRepository.findByUserId(userId, pageable);
    }

    /**
     * Grant permission to user
     */
    public void grantPermission(UUID userId, String resource, PermissionType permissionType,
                                UUID grantedBy, LocalDateTime expiresAt) {
        logger.info("Granting permission {} on resource {} to user {}", permissionType, resource, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if permission already exists
        Optional<UserPermission> existingPermission = permissionRepository
                .findByUserIdAndResourceAndPermissionType(userId, resource, permissionType);

        UserPermission permission;
        if (existingPermission.isPresent()) {
            permission = existingPermission.get();
            permission.setGranted(true);
            permission.setExpiresAt(expiresAt);
        } else {
            permission = new UserPermission();
            permission.setUser(user);
            permission.setResource(resource);
            permission.setPermissionType(permissionType);
            permission.setGranted(true);
            permission.setExpiresAt(expiresAt);
            permission.setGrantedBy(grantedBy);
        }

        permissionRepository.save(permission);

        // Clear cache
        clearUserPermissionCache(userId);

        logger.info("Permission {} granted to user {} for resource {}", permissionType, userId, resource);
    }

    /**
     * Grant permission without expiration
     */
    public void grantPermission(UUID userId, String resource, PermissionType permissionType, UUID grantedBy) {
        grantPermission(userId, resource, permissionType, grantedBy, null);
    }

    /**
     * Revoke permission from user
     */
    public void revokePermission(UUID userId, String resource, PermissionType permissionType) {
        logger.info("Revoking permission {} on resource {} from user {}", permissionType, resource, userId);

        permissionRepository.updatePermissionStatus(userId, resource, permissionType, false);

        // Clear cache
        clearUserPermissionCache(userId);

        logger.info("Permission {} revoked from user {} for resource {}", permissionType, userId, resource);
    }

    /**
     * Grant multiple permissions at once
     */
    public void grantPermissions(UUID userId, String resource, Set<PermissionType> permissionTypes,
                                 UUID grantedBy, LocalDateTime expiresAt) {
        for (PermissionType permissionType : permissionTypes) {
            grantPermission(userId, resource, permissionType, grantedBy, expiresAt);
        }
    }

    /**
     * Revoke multiple permissions at once
     */
    public void revokePermissions(UUID userId, String resource, Set<PermissionType> permissionTypes) {
        for (PermissionType permissionType : permissionTypes) {
            revokePermission(userId, resource, permissionType);
        }
    }

    /**
     * Revoke all permissions for a user on a resource
     */
    public void revokeAllPermissions(UUID userId, String resource) {
        logger.info("Revoking all permissions on resource {} from user {}", resource, userId);

        List<UserPermission> permissions = permissionRepository.findByUserIdAndResource(userId, resource);
        for (UserPermission permission : permissions) {
            permission.setGranted(false);
            permissionRepository.save(permission);
        }

        // Clear cache
        clearUserPermissionCache(userId);

        logger.info("All permissions revoked from user {} for resource {}", userId, resource);
    }

    /**
     * Copy permissions from one user to another
     */
    public void copyPermissions(UUID fromUserId, UUID toUserId, UUID grantedBy) {
        logger.info("Copying permissions from user {} to user {}", fromUserId, toUserId);

        User toUser = userRepository.findById(toUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Target user not found"));

        List<UserPermission> sourcePermissions = permissionRepository.findByUserIdAndGrantedTrue(fromUserId);

        for (UserPermission sourcePermission : sourcePermissions) {
            grantPermission(toUserId, sourcePermission.getResource(),
                    sourcePermission.getPermissionType(), grantedBy,
                    sourcePermission.getExpiresAt());
        }

        logger.info("Copied {} permissions from user {} to user {}",
                sourcePermissions.size(), fromUserId, toUserId);
    }

    /**
     * Get effective permissions for a user (role + explicit permissions)
     */
    @Transactional(readOnly = true)
    public Map<String, Set<PermissionType>> getEffectivePermissions(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Map<String, Set<PermissionType>> effectivePermissions = new HashMap<>();

        // Get role-based permissions
        Set<PermissionType> rolePermissions = DEFAULT_ROLE_PERMISSIONS.get(user.getRole().name());
        if (rolePermissions != null) {
            effectivePermissions.put("*", new HashSet<>(rolePermissions));
        }

        // Get explicit permissions and merge them
        List<UserPermission> explicitPermissions = permissionRepository.findByUserIdAndGrantedTrue(userId);
        for (UserPermission permission : explicitPermissions) {
            effectivePermissions.computeIfAbsent(permission.getResource(), k -> new HashSet<>())
                    .add(permission.getPermissionType());
        }

        return effectivePermissions;
    }

    /**
     * Get users with specific permission on a resource
     */
    @Transactional(readOnly = true)
    public List<User> getUsersWithPermission(String resource, PermissionType permissionType) {
        List<UserPermission> permissions = permissionRepository
                .findByResourceAndPermissionType(resource, permissionType);

        return permissions.stream()
                .filter(p -> p.getGranted() && (p.getExpiresAt() == null || p.getExpiresAt().isAfter(LocalDateTime.now())))
                .map(UserPermission::getUser)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Cleanup expired permissions (scheduled task)
     */
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void cleanupExpiredPermissions() {
        logger.info("Starting cleanup of expired permissions");

        LocalDateTime now = LocalDateTime.now();

        // Mark expired permissions as revoked
        permissionRepository.revokeExpiredPermissions(now);

        // Delete very old expired permissions (older than 90 days)
        LocalDateTime deleteBefore = now.minusDays(90);
        permissionRepository.deleteExpiredPermissions(deleteBefore);

        logger.info("Expired permissions cleanup completed");
    }

    /**
     * Get permission statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getPermissionStatistics(UUID userId) {
        Map<String, Object> stats = new HashMap<>();

        if (userId != null) {
            long totalPermissions = permissionRepository.countGrantedPermissionsByUserId(userId);
            stats.put("totalPermissions", totalPermissions);

            // Count by permission type
            for (PermissionType type : PermissionType.values()) {
                long count = permissionRepository.findByUserId(userId).stream()
                        .mapToLong(p -> p.getPermissionType() == type && p.getGranted() ? 1 : 0)
                        .sum();
                stats.put(type.name().toLowerCase() + "Permissions", count);
            }
        }

        return stats;
    }

    /**
     * Validate permission request
     */
    public boolean canGrantPermission(UUID requesterId, UUID targetUserId, String resource, PermissionType permissionType) {
        try {
            User requester = userRepository.findById(requesterId)
                    .orElseThrow(() -> new ResourceNotFoundException("Requester not found"));

            User target = userRepository.findById(targetUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Target user not found"));

            // Super admins can grant any permission
            if (requester.getRole() == UserRole.SUPER_ADMIN) {
                return true;
            }

            // Company admins can grant permissions within their company (except to super admins)
            if (requester.getRole() == UserRole.COMPANY_ADMIN) {
                return requester.getCompanyId().equals(target.getCompanyId()) &&
                        target.getRole() != UserRole.SUPER_ADMIN;
            }

            // Fleet managers can grant limited permissions within their company
            if (requester.getRole() == UserRole.FLEET_MANAGER) {
                return requester.getCompanyId().equals(target.getCompanyId()) &&
                        target.getRole() == UserRole.DRIVER &&
                        (permissionType == PermissionType.READ || permissionType == PermissionType.WRITE);
            }

            return false;

        } catch (Exception e) {
            logger.error("Error validating permission grant request: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get resources user has access to
     */
    @Transactional(readOnly = true)
    public Set<String> getUserAccessibleResources(UUID userId) {
        List<UserPermission> permissions = permissionRepository.findByUserIdAndGrantedTrue(userId);
        return permissions.stream()
                .map(UserPermission::getResource)
                .collect(Collectors.toSet());
    }

    /**
     * Get permission types user has on a specific resource
     */
    @Transactional(readOnly = true)
    public Set<PermissionType> getUserPermissionTypes(UUID userId, String resource) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Set<PermissionType> permissionTypes = new HashSet<>();

        // Add role-based permissions
        Set<PermissionType> rolePermissions = DEFAULT_ROLE_PERMISSIONS.get(user.getRole().name());
        if (rolePermissions != null) {
            permissionTypes.addAll(rolePermissions);
        }

        // Add explicit permissions
        List<UserPermission> explicitPermissions = permissionRepository.findByUserIdAndResource(userId, resource);
        explicitPermissions.stream()
                .filter(p -> p.getGranted() && (p.getExpiresAt() == null || p.getExpiresAt().isAfter(LocalDateTime.now())))
                .forEach(p -> permissionTypes.add(p.getPermissionType()));

        return permissionTypes;
    }

    /**
     * Bulk permission operations for efficiency
     */
    public void bulkGrantPermissions(List<BulkPermissionRequest> requests, UUID grantedBy) {
        logger.info("Processing {} bulk permission grant requests", requests.size());

        for (BulkPermissionRequest request : requests) {
            try {
                grantPermission(request.getUserId(), request.getResource(),
                        request.getPermissionType(), grantedBy, request.getExpiresAt());
            } catch (Exception e) {
                logger.error("Failed to grant permission for request {}: {}", request, e.getMessage());
            }
        }

        logger.info("Bulk permission grant operation completed");
    }

    /**
     * Audit permission changes
     */
    public void auditPermissionChange(UUID userId, String resource, PermissionType permissionType,
                                      String action, UUID performedBy) {
        logger.info("AUDIT: User {} {} permission {} on resource {} (performed by {})",
                userId, action, permissionType, resource, performedBy);

        // Here you could save to an audit table or external audit system
        // For now, we just log it
    }

    // Helper methods

    private boolean hasRoleBasedPermission(UserRole role, String resource, PermissionType permissionType) {
        Set<PermissionType> rolePermissions = DEFAULT_ROLE_PERMISSIONS.get(role.name());
        return rolePermissions != null && rolePermissions.contains(permissionType);
    }

    private void clearUserPermissionCache(UUID userId) {
        try {
            cacheService.evictUserPermissions(userId);

            // Also clear specific permission cache entries
            for (PermissionType type : PermissionType.values()) {
                String pattern = userId + ":*:" + type;
                // Note: This is a simplified approach. In production, you might want to use
                // Redis pattern matching to clear all matching keys
            }

            logger.debug("Permission cache cleared for user: {}", userId);
        } catch (Exception e) {
            logger.error("Error clearing permission cache for user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Check if user has admin permissions
     */
    public boolean hasAdminPermission(UUID userId, String resource) {
        return hasPermission(userId, resource, PermissionType.ADMIN);
    }

    /**
     * Check if user can perform CRUD operations
     */
    public boolean canRead(UUID userId, String resource) {
        return hasPermission(userId, resource, PermissionType.READ);
    }

    public boolean canWrite(UUID userId, String resource) {
        return hasPermission(userId, resource, PermissionType.WRITE);
    }

    public boolean canDelete(UUID userId, String resource) {
        return hasPermission(userId, resource, PermissionType.DELETE);
    }

    public boolean canExecute(UUID userId, String resource) {
        return hasPermission(userId, resource, PermissionType.EXECUTE);
    }

    public boolean canApprove(UUID userId, String resource) {
        return hasPermission(userId, resource, PermissionType.APPROVE);
    }

    public boolean canExport(UUID userId, String resource) {
        return hasPermission(userId, resource, PermissionType.EXPORT);
    }

    public boolean canImport(UUID userId, String resource) {
        return hasPermission(userId, resource, PermissionType.IMPORT);
    }

    /**
     * Permission summary for user
     */
    @Transactional(readOnly = true)
    public PermissionSummary getPermissionSummary(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        PermissionSummary summary = new PermissionSummary();
        summary.setUserId(userId);
        summary.setRole(user.getRole());

        // Role-based permissions
        Set<PermissionType> rolePermissions = DEFAULT_ROLE_PERMISSIONS.get(user.getRole().name());
        summary.setRolePermissions(rolePermissions != null ? rolePermissions : new HashSet<>());

        // Explicit permissions
        List<UserPermission> explicitPermissions = permissionRepository.findByUserIdAndGrantedTrue(userId);
        summary.setExplicitPermissions(explicitPermissions);

        // Accessible resources
        summary.setAccessibleResources(getUserAccessibleResources(userId));

        return summary;
    }

    // Inner classes for DTOs

    public static class BulkPermissionRequest {
        private UUID userId;
        private String resource;
        private PermissionType permissionType;
        private LocalDateTime expiresAt;

        // Constructors, getters, and setters
        public BulkPermissionRequest() {}

        public BulkPermissionRequest(UUID userId, String resource, PermissionType permissionType) {
            this.userId = userId;
            this.resource = resource;
            this.permissionType = permissionType;
        }

        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }

        public String getResource() { return resource; }
        public void setResource(String resource) { this.resource = resource; }

        public PermissionType getPermissionType() { return permissionType; }
        public void setPermissionType(PermissionType permissionType) { this.permissionType = permissionType; }

        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

        @Override
        public String toString() {
            return String.format("BulkPermissionRequest{userId=%s, resource='%s', permissionType=%s}",
                    userId, resource, permissionType);
        }
    }

    public static class PermissionSummary {
        private UUID userId;
        private UserRole role;
        private Set<PermissionType> rolePermissions;
        private List<UserPermission> explicitPermissions;
        private Set<String> accessibleResources;

        // Getters and setters
        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }

        public UserRole getRole() { return role; }
        public void setRole(UserRole role) { this.role = role; }

        public Set<PermissionType> getRolePermissions() { return rolePermissions; }
        public void setRolePermissions(Set<PermissionType> rolePermissions) {
            this.rolePermissions = rolePermissions;
        }

        public List<UserPermission> getExplicitPermissions() { return explicitPermissions; }
        public void setExplicitPermissions(List<UserPermission> explicitPermissions) {
            this.explicitPermissions = explicitPermissions;
        }

        public Set<String> getAccessibleResources() { return accessibleResources; }
        public void setAccessibleResources(Set<String> accessibleResources) {
            this.accessibleResources = accessibleResources;
        }
    }
}