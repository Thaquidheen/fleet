package com.fleetmanagement.userservice.service;

import com.fleetmanagement.userservice.domain.entity.User;
import com.fleetmanagement.userservice.domain.enums.UserRole;
import com.fleetmanagement.userservice.dto.request.DriverAssignmentRequest;
import com.fleetmanagement.userservice.dto.response.DriverResponse;
import com.fleetmanagement.userservice.dto.response.UserResponse;
import com.fleetmanagement.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class DriverService {

    private static final Logger logger = LoggerFactory.getLogger(DriverService.class);

    private final UserRepository userRepository;
    private final CacheService cacheService;

    @Autowired
    public DriverService(UserRepository userRepository, CacheService cacheService) {
        this.userRepository = userRepository;
        this.cacheService = cacheService;
    }

    @Transactional(readOnly = true)
    public Page<DriverResponse> getCompanyDrivers(UUID companyId, Pageable pageable) {
        Page<User> drivers = userRepository.findByCompanyIdAndRole(companyId, UserRole.DRIVER, pageable);
        return drivers.map(this::convertToDriverResponse);
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> getAvailableDrivers(UUID companyId) {
        List<User> drivers = userRepository.findByCompanyIdAndRole(companyId, UserRole.DRIVER);
        return drivers.stream()
                .filter(this::isDriverAvailable)
                .map(this::convertToDriverResponse)
                .collect(Collectors.toList());
    }

    public DriverResponse promoteToDriver(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == UserRole.DRIVER) {
            throw new IllegalArgumentException("User is already a driver");
        }

        user.setRole(UserRole.DRIVER);
        User savedUser = userRepository.save(user);

        // Clear cache
        cacheService.evictUser(userId);

        logger.info("User {} promoted to driver", userId);
        return convertToDriverResponse(savedUser);
    }

    public UserResponse demoteFromDriver(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != UserRole.DRIVER) {
            throw new IllegalArgumentException("User is not a driver");
        }

        user.setRole(UserRole.FLEET_MANAGER); // Default demotion role
        User savedUser = userRepository.save(user);

        // Clear cache
        cacheService.evictUser(userId);

        logger.info("User {} demoted from driver", userId);
        return convertToUserResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public DriverResponse getDriverById(UUID driverId) {
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        if (driver.getRole() != UserRole.DRIVER) {
            throw new IllegalArgumentException("User is not a driver");
        }

        return convertToDriverResponse(driver);
    }

    private boolean isDriverAvailable(User driver) {
        // Logic to check if driver is available (not assigned to a vehicle, not on leave, etc.)
        // This would integrate with Vehicle Service to check current assignments
        return driver.getStatus().name().equals("ACTIVE");
    }

    private DriverResponse convertToDriverResponse(User user) {
        return DriverResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .licenseNumber(user.getLicenseNumber())
                .licenseExpiry(user.getLicenseExpiry())
                .status(user.getStatus().name())
                .companyId(user.getCompanyId())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private UserResponse convertToUserResponse(User user) {
        // Use existing conversion logic from UserService
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .companyId(user.getCompanyId())
                .build();
    }
}