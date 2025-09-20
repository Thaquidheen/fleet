package com.fleetmanagement.deviceservice.service;

import com.fleetmanagement.deviceservice.dto.request.DeviceTypeRequest;
import com.fleetmanagement.deviceservice.dto.response.DeviceTypeResponse;
import com.fleetmanagement.deviceservice.dto.common.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for Device Type Management
 * 
 * Handles device type creation, management, and configuration
 * for different types of fleet devices.
 */
public interface DeviceTypeService {

    /**
     * Create a new device type
     * 
     * @param request device type creation request
     * @return created device type information
     */
    DeviceTypeResponse createDeviceType(DeviceTypeRequest request);

    /**
     * Get all device types with pagination
     * 
     * @param pageable pagination parameters
     * @return paginated list of device types
     */
    PagedResponse<DeviceTypeResponse> getAllDeviceTypes(Pageable pageable);

    /**
     * Get device type by ID
     * 
     * @param id device type ID
     * @return device type information
     */
    DeviceTypeResponse getDeviceTypeById(Long id);

    /**
     * Update device type
     * 
     * @param id device type ID
     * @param request device type update request
     * @return updated device type information
     */
    DeviceTypeResponse updateDeviceType(Long id, DeviceTypeRequest request);

    /**
     * Delete device type
     * 
     * @param id device type ID
     */
    void deleteDeviceType(Long id);

    /**
     * Get device types by category
     * 
     * @param category device category
     * @return list of device types in category
     */
    List<DeviceTypeResponse> getDeviceTypesByCategory(String category);

    /**
     * Get device types by brand
     * 
     * @param brand device brand
     * @return list of device types by brand
     */
    List<DeviceTypeResponse> getDeviceTypesByBrand(String brand);

    /**
     * Search device types
     * 
     * @param query search query
     * @param pageable pagination parameters
     * @return paginated search results
     */
    PagedResponse<DeviceTypeResponse> searchDeviceTypes(String query, Pageable pageable);

    /**
     * Check if device type exists
     * 
     * @param id device type ID
     * @return true if exists, false otherwise
     */
    boolean deviceTypeExists(Long id);

    /**
     * Get device type count
     * 
     * @return device type count
     */
    long getDeviceTypeCount();

    /**
     * Get device type statistics
     * 
     * @return device type statistics
     */
    Object getDeviceTypeStatistics();
}

