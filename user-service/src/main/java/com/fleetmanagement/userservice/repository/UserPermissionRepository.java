// UserPermissionRepository.java
package com.fleetmanagement.userservice.repository;

import com.fleetmanagement.userservice.domain.entity.UserPermission;
import com.fleetmanagement.userservice.domain.enums.PermissionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, UUID> {

    // Basic find methods
    List<UserPermission> findByUserId(UUID userId);

    Page<UserPermission> findByUserId(UUID userId, Pageable pageable);

    List<UserPermission> findByUserIdAndGrantedTrue(UUID userId);

    List<UserPermission> findByUserIdAndResource(UUID userId, String resource);

    Optional<UserPermission> findByUserIdAndResourceAndPermissionType(UUID userId,
                                                                      String resource,
                                                                      PermissionType permissionType);

    // Permission checks
    @Query("SELECT p FROM UserPermission p WHERE p.user.id = :userId AND p.resource = :resource " +
            "AND p.permissionType = :permissionType AND p.granted = true " +
            "AND (p.expiresAt IS NULL OR p.expiresAt > :now)")
    Optional<UserPermission> findValidPermission(@Param("userId") UUID userId,
                                                 @Param("resource") String resource,
                                                 @Param("permissionType") PermissionType permissionType,
                                                 @Param("now") LocalDateTime now);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM UserPermission p " +
            "WHERE p.user.id = :userId AND p.resource = :resource " +
            "AND p.permissionType = :permissionType AND p.granted = true " +
            "AND (p.expiresAt IS NULL OR p.expiresAt > :now)")
    boolean hasPermission(@Param("userId") UUID userId,
                          @Param("resource") String resource,
                          @Param("permissionType") PermissionType permissionType,
                          @Param("now") LocalDateTime now);

    // Resource-based queries
    List<UserPermission> findByResource(String resource);

    List<UserPermission> findByResourceAndPermissionType(String resource, PermissionType permissionType);

    List<UserPermission> findByResourceAndGrantedTrue(String resource);

    // Expired permission queries
    List<UserPermission> findByExpiresAtBefore(LocalDateTime dateTime);

    @Query("SELECT p FROM UserPermission p WHERE p.expiresAt < :now AND p.granted = true")
    List<UserPermission> findExpiredGrantedPermissions(@Param("now") LocalDateTime now);

    // Update operations
    @Modifying
    @Query("UPDATE UserPermission p SET p.granted = false WHERE p.expiresAt < :now AND p.granted = true")
    void revokeExpiredPermissions(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE UserPermission p SET p.granted = :granted WHERE p.user.id = :userId " +
            "AND p.resource = :resource AND p.permissionType = :permissionType")
    void updatePermissionStatus(@Param("userId") UUID userId,
                                @Param("resource") String resource,
                                @Param("permissionType") PermissionType permissionType,
                                @Param("granted") Boolean granted);

    // Cleanup operations
    @Modifying
    @Query("DELETE FROM UserPermission p WHERE p.expiresAt < :deleteBefore")
    void deleteExpiredPermissions(@Param("deleteBefore") LocalDateTime deleteBefore);

    // Statistics
    @Query("SELECT COUNT(p) FROM UserPermission p WHERE p.user.id = :userId AND p.granted = true")
    long countGrantedPermissionsByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(p) FROM UserPermission p WHERE p.resource = :resource AND p.granted = true")
    long countGrantedPermissionsByResource(@Param("resource") String resource);
}