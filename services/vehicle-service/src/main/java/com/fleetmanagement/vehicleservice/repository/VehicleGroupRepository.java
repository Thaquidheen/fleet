package com.fleetmanagement.vehicleservice.repository;

import com.fleetmanagement.vehicleservice.domain.entity.VehicleGroup;
import com.fleetmanagement.vehicleservice.domain.enums.GroupType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Vehicle Group Repository Interface
 *
 * Provides data access methods for VehicleGroup entities with:
 * - Hierarchical group management
 * - Company-scoped operations
 * - Group type filtering
 * - Tree structure operations
 */
@Repository
public interface VehicleGroupRepository extends JpaRepository<VehicleGroup, UUID>, JpaSpecificationExecutor<VehicleGroup> {

    // Basic find methods
    Optional<VehicleGroup> findByIdAndCompanyId(UUID id, UUID companyId);

    Optional<VehicleGroup> findByNameAndCompanyId(String name, UUID companyId);

    // Existence checks
    boolean existsByNameAndCompanyId(String name, UUID companyId);

    boolean existsByNameAndCompanyIdAndIdNot(String name, UUID companyId, UUID id);

    // Company-specific queries
    List<VehicleGroup> findByCompanyId(UUID companyId);

    Page<VehicleGroup> findByCompanyId(UUID companyId, Pageable pageable);

    List<VehicleGroup> findByCompanyIdAndIsActive(UUID companyId, boolean isActive);

    Page<VehicleGroup> findByCompanyIdAndIsActive(UUID companyId, boolean isActive, Pageable pageable);

    // Group type queries
    List<VehicleGroup> findByCompanyIdAndGroupType(UUID companyId, GroupType groupType);

    List<VehicleGroup> findByCompanyIdAndGroupTypeAndIsActive(UUID companyId, GroupType groupType, boolean isActive);

    // Hierarchical queries - Root groups (no parent)
    List<VehicleGroup> findByCompanyIdAndParentGroupIsNull(UUID companyId);

    List<VehicleGroup> findByCompanyIdAndParentGroupIsNullAndIsActive(UUID companyId, boolean isActive);

    Page<VehicleGroup> findByCompanyIdAndParentGroupIsNull(UUID companyId, Pageable pageable);

    // Child group queries
    List<VehicleGroup> findByParentGroupId(UUID parentGroupId);

    List<VehicleGroup> findByParentGroupIdAndIsActive(UUID parentGroupId, boolean isActive);

    @Query("SELECT COUNT(vg) FROM VehicleGroup vg WHERE vg.parentGroup.id = :parentGroupId AND vg.isActive = true")
    long countActiveChildGroups(@Param("parentGroupId") UUID parentGroupId);

    // Hierarchical depth queries
    @Query("SELECT vg FROM VehicleGroup vg WHERE vg.companyId = :companyId AND vg.parentGroup IS NULL ORDER BY vg.sortOrder, vg.name")
    List<VehicleGroup> findRootGroupsByCompanyOrderBySortOrder(@Param("companyId") UUID companyId);

    // Manager-related queries
    List<VehicleGroup> findByCompanyIdAndManagerId(UUID companyId, UUID managerId);

    @Query("SELECT COUNT(vg) FROM VehicleGroup vg WHERE vg.companyId = :companyId AND vg.managerId = :managerId")
    long countGroupsByManager(@Param("companyId") UUID companyId, @Param("managerId") UUID managerId);

    // Vehicle count queries
    @Query("SELECT vg, COUNT(vm) FROM VehicleGroup vg " +
            "LEFT JOIN vg.vehicleMemberships vm " +
            "WHERE vg.companyId = :companyId " +
            "GROUP BY vg")
    List<Object[]> findGroupsWithVehicleCount(@Param("companyId") UUID companyId);

    @Query("SELECT COUNT(vm) FROM VehicleGroupMembership vm " +
            "WHERE vm.vehicleGroup.id = :groupId")
    long countVehiclesInGroup(@Param("groupId") UUID groupId);

    // Search queries
    @Query("SELECT vg FROM VehicleGroup vg WHERE vg.companyId = :companyId AND " +
            "(LOWER(vg.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(vg.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(vg.location) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<VehicleGroup> searchGroups(@Param("companyId") UUID companyId, @Param("searchTerm") String searchTerm);

    @Query("SELECT vg FROM VehicleGroup vg WHERE vg.companyId = :companyId AND " +
            "(LOWER(vg.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(vg.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(vg.location) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<VehicleGroup> searchGroups(@Param("companyId") UUID companyId,
                                    @Param("searchTerm") String searchTerm,
                                    Pageable pageable);

    // Advanced hierarchical queries
    @Query(value = "WITH RECURSIVE group_hierarchy AS (" +
            "  SELECT id, name, parent_group_id, 0 as level " +
            "  FROM vehicle_groups " +
            "  WHERE company_id = :companyId AND parent_group_id IS NULL " +
            "  UNION ALL " +
            "  SELECT vg.id, vg.name, vg.parent_group_id, gh.level + 1 " +
            "  FROM vehicle_groups vg " +
            "  INNER JOIN group_hierarchy gh ON vg.parent_group_id = gh.id " +
            "  WHERE vg.company_id = :companyId" +
            ") SELECT * FROM group_hierarchy ORDER BY level, name",
            nativeQuery = true)
    List<Object[]> findGroupHierarchy(@Param("companyId") UUID companyId);

    @Query(value = "WITH RECURSIVE group_descendants AS (" +
            "  SELECT id, name, parent_group_id " +
            "  FROM vehicle_groups " +
            "  WHERE id = :groupId " +
            "  UNION ALL " +
            "  SELECT vg.id, vg.name, vg.parent_group_id " +
            "  FROM vehicle_groups vg " +
            "  INNER JOIN group_descendants gd ON vg.parent_group_id = gd.id" +
            ") SELECT vg.* FROM vehicle_groups vg " +
            "INNER JOIN group_descendants gd ON vg.id = gd.id " +
            "WHERE vg.id != :groupId",
            nativeQuery = true)
    List<VehicleGroup> findAllDescendants(@Param("groupId") UUID groupId);

    @Query(value = "WITH RECURSIVE group_ancestors AS (" +
            "  SELECT id, name, parent_group_id " +
            "  FROM vehicle_groups " +
            "  WHERE id = :groupId " +
            "  UNION ALL " +
            "  SELECT vg.id, vg.name, vg.parent_group_id " +
            "  FROM vehicle_groups vg " +
            "  INNER JOIN group_ancestors ga ON vg.id = ga.parent_group_id" +
            ") SELECT vg.* FROM vehicle_groups vg " +
            "INNER JOIN group_ancestors ga ON vg.id = ga.id " +
            "WHERE vg.id != :groupId " +
            "ORDER BY vg.id",
            nativeQuery = true)
    List<VehicleGroup> findAllAncestors(@Param("groupId") UUID groupId);

    // Batch operations
    @Modifying
    @Query("UPDATE VehicleGroup vg SET vg.isActive = :isActive, vg.updatedBy = :updatedBy, vg.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE vg.companyId = :companyId AND vg.id IN :groupIds")
    int updateGroupsActiveStatus(@Param("companyId") UUID companyId,
                                 @Param("groupIds") List<UUID> groupIds,
                                 @Param("isActive") boolean isActive,
                                 @Param("updatedBy") UUID updatedBy);

    @Modifying
    @Query("UPDATE VehicleGroup vg SET vg.managerId = :managerId, vg.updatedBy = :updatedBy, vg.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE vg.companyId = :companyId AND vg.id = :groupId")
    int updateGroupManager(@Param("companyId") UUID companyId,
                           @Param("groupId") UUID groupId,
                           @Param("managerId") UUID managerId,
                           @Param("updatedBy") UUID updatedBy);

    @Modifying
    @Query("UPDATE VehicleGroup vg SET vg.sortOrder = :sortOrder, vg.updatedBy = :updatedBy, vg.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE vg.companyId = :companyId AND vg.id = :groupId")
    int updateGroupSortOrder(@Param("companyId") UUID companyId,
                             @Param("groupId") UUID groupId,
                             @Param("sortOrder") Integer sortOrder,
                             @Param("updatedBy") UUID updatedBy);

    // Analytics queries
    @Query("SELECT vg.groupType, COUNT(vg) FROM VehicleGroup vg WHERE vg.companyId = :companyId GROUP BY vg.groupType")
    List<Object[]> getGroupCountByType(@Param("companyId") UUID companyId);

    @Query("SELECT COUNT(vg) FROM VehicleGroup vg WHERE vg.companyId = :companyId AND vg.isActive = true")
    long countActiveGroupsByCompany(@Param("companyId") UUID companyId);

    @Query("SELECT COUNT(vg) FROM VehicleGroup vg WHERE vg.companyId = :companyId AND vg.parentGroup IS NULL")
    long countRootGroupsByCompany(@Param("companyId") UUID companyId);

    // Validation queries
    @Query("SELECT CASE WHEN COUNT(vg) > 0 THEN true ELSE false END FROM VehicleGroup vg " +
            "WHERE vg.id = :groupId AND vg.companyId = :companyId")
    boolean existsByIdAndCompanyId(@Param("groupId") UUID groupId, @Param("companyId") UUID companyId);

    @Query("SELECT CASE WHEN COUNT(vg) > 0 THEN true ELSE false END FROM VehicleGroup vg " +
            "WHERE vg.parentGroup.id = :parentId AND vg.id = :childId")
    boolean isChildOf(@Param("childId") UUID childId, @Param("parentId") UUID parentId);

    // Circular reference detection
    @Query(value = "WITH RECURSIVE group_path AS (" +
            "  SELECT id, parent_group_id, ARRAY[id] as path " +
            "  FROM vehicle_groups " +
            "  WHERE id = :groupId " +
            "  UNION ALL " +
            "  SELECT vg.id, vg.parent_group_id, gp.path || vg.id " +
            "  FROM vehicle_groups vg " +
            "  INNER JOIN group_path gp ON vg.id = gp.parent_group_id " +
            "  WHERE NOT vg.id = ANY(gp.path)" +
            ") SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END " +
            "FROM group_path WHERE :potentialParentId = ANY(path)",
            nativeQuery = true)
    boolean wouldCreateCircularReference(@Param("groupId") UUID groupId, @Param("potentialParentId") UUID potentialParentId);

    // Custom finder methods for complex queries
    @Query("SELECT vg FROM VehicleGroup vg WHERE vg.companyId = :companyId " +
            "AND (:groupType IS NULL OR vg.groupType = :groupType) " +
            "AND (:isActive IS NULL OR vg.isActive = :isActive) " +
            "AND (:parentGroupId IS NULL OR vg.parentGroup.id = :parentGroupId) " +
            "AND (:managerId IS NULL OR vg.managerId = :managerId)")
    Page<VehicleGroup> findGroupsWithCriteria(@Param("companyId") UUID companyId,
                                              @Param("groupType") GroupType groupType,
                                              @Param("isActive") Boolean isActive,
                                              @Param("parentGroupId") UUID parentGroupId,
                                              @Param("managerId") UUID managerId,
                                              Pageable pageable);

    // Sort order management
    @Query("SELECT MAX(vg.sortOrder) FROM VehicleGroup vg WHERE vg.companyId = :companyId " +
            "AND (:parentGroupId IS NULL AND vg.parentGroup IS NULL OR vg.parentGroup.id = :parentGroupId)")
    Integer getMaxSortOrderForLevel(@Param("companyId") UUID companyId, @Param("parentGroupId") UUID parentGroupId);

    @Modifying
    @Query("UPDATE VehicleGroup vg SET vg.sortOrder = vg.sortOrder + 1 " +
            "WHERE vg.companyId = :companyId AND vg.sortOrder >= :sortOrder " +
            "AND (:parentGroupId IS NULL AND vg.parentGroup IS NULL OR vg.parentGroup.id = :parentGroupId)")
    int incrementSortOrdersFrom(@Param("companyId") UUID companyId,
                                @Param("sortOrder") Integer sortOrder,
                                @Param("parentGroupId") UUID parentGroupId);
}