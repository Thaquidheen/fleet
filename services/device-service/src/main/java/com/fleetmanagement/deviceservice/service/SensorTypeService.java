package com.fleetmanagement.deviceservice.service;

import com.fleetmanagement.deviceservice.dto.response.SensorTypeResponse;
import com.fleetmanagement.deviceservice.dto.common.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for Sensor Type Management
 * 
 * Handles sensor type management and configuration
 * for different types of sensors used in fleet devices.
 */
public interface SensorTypeService {

    /**
     * Get all sensor types with pagination
     * 
     * @param pageable pagination parameters
     * @return paginated list of sensor types
     */
    PagedResponse<SensorTypeResponse> getAllSensorTypes(Pageable pageable);

    /**
     * Get sensor type by ID
     * 
     * @param id sensor type ID
     * @return sensor type information
     */
    SensorTypeResponse getSensorTypeById(Long id);

    /**
     * Get sensor types by category
     * 
     * @param category sensor category
     * @return list of sensor types in category
     */
    List<SensorTypeResponse> getSensorTypesByCategory(String category);

    /**
     * Search sensor types
     * 
     * @param query search query
     * @param pageable pagination parameters
     * @return paginated search results
     */
    PagedResponse<SensorTypeResponse> searchSensorTypes(String query, Pageable pageable);

    /**
     * Check if sensor type exists
     * 
     * @param id sensor type ID
     * @return true if exists, false otherwise
     */
    boolean sensorTypeExists(Long id);

    /**
     * Get sensor type count
     * 
     * @return sensor type count
     */
    long getSensorTypeCount();

    /**
     * Get sensor type statistics
     * 
     * @return sensor type statistics
     */
    Object getSensorTypeStatistics();
}

