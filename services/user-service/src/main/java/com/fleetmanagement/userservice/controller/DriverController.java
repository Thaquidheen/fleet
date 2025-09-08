package com.fleetmanagement.userservice.controller;

import com.fleetmanagement.userservice.dto.response.DriverResponse;
import com.fleetmanagement.userservice.dto.response.UserResponse;
import com.fleetmanagement.userservice.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/drivers")
@Tag(name = "Driver Management", description = "Driver-specific operations")
public class DriverController {

    private static final Logger logger = LoggerFactory.getLogger(DriverController.class);

    private final DriverService driverService;

    @Autowired
    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Get company drivers", description = "Get all drivers for a company")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<Page<DriverResponse>> getCompanyDrivers(
            @PathVariable UUID companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<DriverResponse> drivers = driverService.getCompanyDrivers(companyId, pageable);

        return ResponseEntity.ok(drivers);
    }

    @GetMapping("/company/{companyId}/available")
    @Operation(summary = "Get available drivers", description = "Get all available drivers for a company")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<List<DriverResponse>> getAvailableDrivers(@PathVariable UUID companyId) {
        List<DriverResponse> availableDrivers = driverService.getAvailableDrivers(companyId);
        return ResponseEntity.ok(availableDrivers);
    }

    @GetMapping("/{driverId}")
    @Operation(summary = "Get driver by ID", description = "Get driver details by ID")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<DriverResponse> getDriverById(@PathVariable UUID driverId) {
        DriverResponse driver = driverService.getDriverById(driverId);
        return ResponseEntity.ok(driver);
    }

    @PostMapping("/{userId}/promote")
    @Operation(summary = "Promote user to driver", description = "Promote an existing user to driver role")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<DriverResponse> promoteToDriver(@PathVariable UUID userId) {
        DriverResponse driver = driverService.promoteToDriver(userId);
        return ResponseEntity.ok(driver);
    }

    @PostMapping("/{userId}/demote")
    @Operation(summary = "Demote driver", description = "Demote a driver to regular user")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<UserResponse> demoteFromDriver(@PathVariable UUID userId) {
        UserResponse user = driverService.demoteFromDriver(userId);
        return ResponseEntity.ok(user);
    }
}