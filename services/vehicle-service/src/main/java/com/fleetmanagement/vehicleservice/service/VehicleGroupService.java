package com.fleetmanagement.vehicleservice.service;

import com.fleetmanagement.vehicleservice.domain.entity.VehicleGroup;
import com.fleetmanagement.vehicleservice.domain.entity.VehicleGroupMembership;
import com.fleetmanagement.vehicleservice.domain.enums.GroupType;
import com.fleetmanagement.vehicleservice.dto.request.*;
import com.fleetmanagement.vehicleservice.dto.response.*;
import com.fleetmanagement.vehicleservice.exception.*;
import com.fleetmanagement.vehicleservice.repository.VehicleGroupRepository;
import org.slf4j.Logger;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


import java.util.*;
import java.util.stream.Collectors;

/**
 * Vehicle Group Service
 *
 * Manages fleet organization through hierarchical vehicle groups.
 * Provides business logic for group creation, management, and vehicle assignments.
 */
@Service
@Transactional
public class VehicleGroupService {

    private static final Logger logger = LoggerFactory.getLogger(VehicleGroupService.class);

    private final VehicleGroupRepository vehicleGroupRepository;
    private final CacheService cacheService;

    @Autowired
    public VehicleGroupService(VehicleGroupRepository vehicleGroupRepository,
                               CacheService cacheService) {
        this.vehicleGroupRepository = vehicleGroupRepository;
        this.cacheService = cacheService;
    }

    /**
     * Create a new vehicle group
     */
    public VehicleGroupResponse createVehicleGroup(CreateVehicleGroupRequest request, UUID companyId, UUID createdBy) {
        logger.info("Creating vehicle group: {} for company: {}", request.getName(), companyId);

        // Validate group name uniqueness within company
        if (vehicleGroupRepository.existsByNameAndCompanyId(request.getName(), companyId)) {
            throw new VehicleGroupAlreadyExistsException("Vehicle group with name '" + request.getName() + "' already exists");
        }

        // Validate parent group if specified
        VehicleGroup parentGroup = null;
        if (request.getParentGroupId() != null) {
            parentGroup = vehicleGroupRepository.findByIdAndCompanyId(request.getParentGroupId(), companyId)
                    .orElseThrow(() -> new VehicleGroupNotFoundException("Parent group not found: " + request.getParentGroupId()));
        }

        // Determine sort order
        Integer sortOrder = request.getSortOrder();
        if (sortOrder == null) {
            Integer maxSortOrder = vehicleGroupRepository.getMaxSortOrderForLevel(companyId, request.getParentGroupId());
            sortOrder = (maxSortOrder != null ? maxSortOrder : 0) + 10;
        } else {
            // Increment existing sort orders if needed
            vehicleGroupRepository.incrementSortOrdersFrom(companyId, sortOrder, request.getParentGroupId());
        }

        // Create vehicle group entity
        VehicleGroup vehicleGroup = VehicleGroup.builder()
                .companyId(companyId)
                .name(request.getName())
                .description(request.getDescription())
                .parentGroup(parentGroup)
                .groupType(request.getGroupType())
                .maxVehicles(request.getMaxVehicles())
                .location(request.getLocation())
                .managerId(request.getManagerId())
                .customFields(request.getCustomFields())
                .sortOrder(sortOrder)
                .isActive(true)
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .build();

        vehicleGroup = vehicleGroupRepository.save(vehicleGroup);

        logger.info("Successfully created vehicle group: {} with ID: {}", vehicleGroup.getName(), vehicleGroup.getId());
        return mapToVehicleGroupResponse(vehicleGroup);
    }

    /**
     * Get vehicle group by ID
     */
    @Cacheable(value = "vehicle-group-details", key = "#groupId")
    public VehicleGroupResponse getVehicleGroupById(UUID groupId, UUID companyId) {
        logger.debug("Fetching vehicle group: {} for company: {}", groupId, companyId);

        VehicleGroup vehicleGroup = vehicleGroupRepository.findByIdAndCompanyId(groupId, companyId)
                .orElseThrow(() -> new VehicleGroupNotFoundException("Vehicle group not found: " + groupId));

        return mapToVehicleGroupResponse(vehicleGroup);
    }

    /**
     * Update vehicle group
     */
    @CacheEvict(value = "vehicle-group-details", key = "#groupId")
    public VehicleGroupResponse updateVehicleGroup(UUID groupId, UpdateVehicleGroupRequest request, UUID companyId, UUID updatedBy) {
        logger.info("Updating vehicle group: {} for company: {}", groupId, companyId);

        VehicleGroup vehicleGroup = vehicleGroupRepository.findByIdAndCompanyId(groupId, companyId)
                .orElseThrow(() -> new VehicleGroupNotFoundException("Vehicle group not found: " + groupId));

        // Validate name uniqueness if name is being updated
        if (StringUtils.hasText(request.getName()) &&
                !request.getName().equals(vehicleGroup.getName()) &&
                vehicleGroupRepository.existsByNameAndCompanyIdAndIdNot(request.getName(), companyId, groupId)) {
            throw new VehicleGroupAlreadyExistsException("Vehicle group with name '" + request.getName() + "' already exists");
        }

        // Validate parent group change
        if (request.getParentGroupId() != null) {
            if (!vehicleGroup.isValidHierarchy(findParentGroup(request.getParentGroupId(), companyId))) {
                throw new VehicleGroupValidationException("Invalid hierarchy: would create circular reference");
            }
        }

        // Update fields
        updateVehicleGroupFields(vehicleGroup, request, updatedBy);

        vehicleGroup = vehicleGroupRepository.save(vehicleGroup);

        logger.info("Successfully updated vehicle group: {}", groupId);
        return mapToVehicleGroupResponse(vehicleGroup);
    }

    /**
     * Delete vehicle group
     */
    @CacheEvict(value = "vehicle-group-details", key = "#groupId")
    public void deleteVehicleGroup(UUID groupId, UUID companyId, UUID deletedBy) {
        logger.info("Deleting vehicle group: {} for company: {}", groupId, companyId);

        VehicleGroup vehicleGroup = vehicleGroupRepository.findByIdAndCompanyId(groupId, companyId)
                .orElseThrow(() -> new VehicleGroupNotFoundException("Vehicle group not found: " + groupId));

        // Validate deletion (check for child groups and vehicles)
        validateVehicleGroupDeletion(vehicleGroup);

        vehicleGroupRepository.delete(vehicleGroup);

        logger.info("Successfully deleted vehicle group: {}", groupId);
    }

    /**
     * Get all vehicle groups for a company
     */
    public List<VehicleGroupResponse> getVehicleGroupsByCompany(UUID companyId) {
        logger.debug("Fetching all vehicle groups for company: {}", companyId);

        List<VehicleGroup> vehicleGroups = vehicleGroupRepository.findByCompanyIdAndIsActive(companyId, true);

        return vehicleGroups.stream()
                .map(this::mapToVehicleGroupResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get root vehicle groups (no parent) for a company
     */
    public List<VehicleGroupResponse> getRootVehicleGroups(UUID companyId) {
        logger.debug("Fetching root vehicle groups for company: {}", companyId);

        List<VehicleGroup> rootGroups = vehicleGroupRepository.findRootGroupsByCompanyOrderBySortOrder(companyId);

        return rootGroups.stream()
                .map(this::mapToVehicleGroupResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get child groups for a parent group
     */
    public List<VehicleGroupResponse> getChildGroups(UUID parentGroupId, UUID companyId) {
        logger.debug("Fetching child groups for parent: {}", parentGroupId);

        // Verify parent group exists and belongs to company
        vehicleGroupRepository.findByIdAndCompanyId(parentGroupId, companyId)
                .orElseThrow(() -> new VehicleGroupNotFoundException("Parent group not found: " + parentGroupId));

        List<VehicleGroup> childGroups = vehicleGroupRepository.findByParentGroupIdAndIsActive(parentGroupId, true);

        return childGroups.stream()
                .map(this::mapToVehicleGroupResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get complete group hierarchy for a company
     */
    public List<VehicleGroupHierarchyResponse> getGroupHierarchy(UUID companyId) {
        logger.debug("Building group hierarchy for company: {}", companyId);

        List<Object[]> hierarchyData = vehicleGroupRepository.findGroupHierarchy(companyId);

        return hierarchyData.stream()
                .map(row -> VehicleGroupHierarchyResponse.builder()
                        .rootGroupId((UUID) row[0])
                        .rootGroupName((String) row[1])
                        .parentGroupId((UUID) row[2])
                        .level((Integer) row[3])
                        .build())
                .collect(Collectors.toList());
    }
    private Specification<VehicleGroup> createVehicleGroupSpecification(VehicleGroupSearchRequest req, UUID companyId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("companyId"), companyId));

            if (req != null) {
                if (StringUtils.hasText(req.getName())) {
                    predicates.add(cb.like(cb.lower(root.get("name")), "%" + req.getName().toLowerCase() + "%"));
                }
                if (req.getGroupType() != null) {
                    predicates.add(cb.equal(root.get("groupType"), req.getGroupType()));
                }
                if (req.getParentGroupId() != null) {
                    predicates.add(cb.equal(root.get("parentGroup").get("id"), req.getParentGroupId()));
                }
                if (req.getManagerId() != null) {
                    predicates.add(cb.equal(root.get("managerId"), req.getManagerId()));
                }
                if (req.getIsActive() != null) {
                    predicates.add(cb.equal(root.get("isActive"), req.getIsActive()));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
    private VehicleGroupResponse mapToVehicleGroupResponse(VehicleGroup entity) {
        if (entity == null) return null;
        return VehicleGroupResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .companyId(entity.getCompanyId())
                .parentGroupId(entity.getParentGroup() != null ? entity.getParentGroup().getId() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    /**
     * Search vehicle groups with criteria
     */


    public PagedResponse<VehicleGroupResponse> searchVehicleGroups(VehicleGroupSearchRequest searchRequest, UUID companyId, Pageable pageable) {
        logger.debug("Searching vehicle groups for company: {} with criteria", companyId);

        Specification<VehicleGroup> spec = createVehicleGroupSpecification(searchRequest, companyId);
        Page<VehicleGroup> groupPage = vehicleGroupRepository.findAll(spec, pageable);

        List<VehicleGroupResponse> groupResponses = groupPage.getContent().stream()
                .map(this::mapToVehicleGroupResponse)
                .collect(Collectors.toList());

        return PagedResponse.<VehicleGroupResponse>builder()
                .content(groupResponses)
                .page(groupPage.getNumber())
                .size(groupPage.getSize())
                .totalElements((int) groupPage.getTotalElements())
                .totalPages(groupPage.getTotalPages())
                .first(groupPage.isFirst())
                .last(groupPage.isLast())
                .empty(groupPage.isEmpty())
                .build();
    }

    /**
     * Get vehicle groups by type
     */
    public List<VehicleGroupResponse> getVehicleGroupsByType(UUID companyId, GroupType groupType) {
        logger.debug("Fetching vehicle groups by type: {} for company: {}", groupType, companyId);

        List<VehicleGroup> vehicleGroups = vehicleGroupRepository.findByCompanyIdAndGroupTypeAndIsActive(companyId, groupType, true);

        return vehicleGroups.stream()
                .map(this::mapToVehicleGroupResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get groups managed by a specific manager
     */
    public List<VehicleGroupResponse> getGroupsByManager(UUID companyId, UUID managerId) {
        logger.debug("Fetching groups managed by: {} for company: {}", managerId, companyId);

        List<VehicleGroup> vehicleGroups = vehicleGroupRepository.findByCompanyIdAndManagerId(companyId, managerId);

        return vehicleGroups.stream()
                .map(this::mapToVehicleGroupResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update group sort order
     */
    public void updateGroupSortOrder(UUID groupId, Integer newSortOrder, UUID companyId, UUID updatedBy) {
        logger.debug("Updating sort order for group: {} to {}", groupId, newSortOrder);

        vehicleGroupRepository.updateGroupSortOrder(companyId, groupId, newSortOrder, updatedBy);
    }

    /**
     * Activate/Deactivate group
     */
    public VehicleGroupResponse toggleGroupStatus(UUID groupId, UUID companyId, UUID updatedBy) {
        logger.info("Toggling status for vehicle group: {}", groupId);

        VehicleGroup vehicleGroup = vehicleGroupRepository.findByIdAndCompanyId(groupId, companyId)
                .orElseThrow(() -> new VehicleGroupNotFoundException("Vehicle group not found: " + groupId));

        vehicleGroup.setActive(!vehicleGroup.getIsActive());
        vehicleGroup.setUpdatedBy(updatedBy);

        vehicleGroup = vehicleGroupRepository.save(vehicleGroup);

        return mapToVehicleGroupResponse(vehicleGroup);
    }

    /**
     * Get group statistics
     */
    public VehicleGroupStatisticsResponse getGroupStatistics(UUID companyId) {
        logger.debug("Generating group statistics for company: {}", companyId);

        List<Object[]> groupCountByType = vehicleGroupRepository.getGroupCountByType(companyId);
        long activeGroupsCount = vehicleGroupRepository.countActiveGroupsByCompany(companyId);
        long rootGroupsCount = vehicleGroupRepository.countRootGroupsByCompany(companyId);

        Map<GroupType, Integer> typeDistribution = new HashMap<>();
        for (Object[] row : groupCountByType) {
            typeDistribution.put((GroupType) row[0], ((Number) row[1]).intValue());
        }

        // Derive vehiclesByGroupType (example uses same distribution; replace with real vehicle counts if available)
        Map<String, Integer> vehiclesByGroupType = typeDistribution.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));

        return VehicleGroupStatisticsResponse.builder()
                .companyId(companyId)
                .totalActiveGroups((int) activeGroupsCount)
                .rootGroups((int) rootGroupsCount)
                .groupsByType(typeDistribution)
                .vehiclesByGroupType(vehiclesByGroupType)
                .generatedAt(java.time.LocalDateTime.now())
                .build();
    }

    // Private helper methods

    private VehicleGroup findParentGroup(UUID parentGroupId, UUID companyId) {
        return vehicleGroupRepository.findByIdAndCompanyId(parentGroupId, companyId)
                .orElseThrow(() -> new VehicleGroupNotFoundException("Parent group not found: " + parentGroupId));
    }

    private void validateVehicleGroupDeletion(VehicleGroup vehicleGroup) {
        // Check for child groups
        if (vehicleGroup.hasChildGroups()) {
            throw new VehicleGroupValidationException("Cannot delete group with child groups. Move or delete child groups first.");
        }

        // Check for assigned vehicles
        if (vehicleGroup.getCurrentVehicleCount() > 0) {
            throw new VehicleGroupValidationException("Cannot delete group with assigned vehicles. Remove vehicles first.");
        }
    }

    private void updateVehicleGroupFields(VehicleGroup vehicleGroup, UpdateVehicleGroupRequest request, UUID updatedBy) {
        if (StringUtils.hasText(request.getName())) {
            vehicleGroup.setName(request.getName());
        }
        if (request.getDescription() != null) {
            vehicleGroup.setDescription(request.getDescription());
        }
        if (request.getParentGroupId() != null) {
            VehicleGroup parentGroup = findParentGroup(request.getParentGroupId(), vehicleGroup.getCompanyId());
            vehicleGroup.setParentGroup(parentGroup);
        }
        if (request.getMaxVehicles() != null) {
            vehicleGroup.setMaxVehicles(request.getMaxVehicles());
        }
        if (StringUtils.hasText(request.getLocation())) {
            vehicleGroup.setLocation(request.getLocation());
        }
        if (request.getManagerId() != null) {
            vehicleGroup.setManagerId(request.getManagerId());
        }
        if (request.getIsActive() != null) {
            vehicleGroup.setActive(request.getIsActive());
        }
        if (request.getCustomFields() != null) {
            vehicleGroup.setCustomFields(request.getCustomFields());
        }
    }

}