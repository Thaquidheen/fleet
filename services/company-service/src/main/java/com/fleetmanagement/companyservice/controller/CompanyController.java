package controller;

import com.fleetmanagement.companyservice.domain.enums.CompanyStatus;
import com.fleetmanagement.companyservice.domain.enums.SubscriptionPlan;
import com.fleetmanagement.companyservice.dto.request.CreateCompanyRequest;
import com.fleetmanagement.companyservice.dto.request.UpdateCompanyRequest;
import com.fleetmanagement.companyservice.dto.response.CompanyResponse;
import com.fleetmanagement.companyservice.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.fleetmanagement.companyservice.dto.response.CompanyValidationResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/companies")
@Tag(name = "Company Management", description = "Company CRUD operations and management")
public class CompanyController {

    private static final Logger logger = LoggerFactory.getLogger(CompanyController.class);

    private final CompanyService companyService;

    @Autowired
    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping
    @Operation(summary = "Create company", description = "Create a new company (SUPER_ADMIN only)")
    @ApiResponse(responseCode = "201", description = "Company created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid company data")
    @ApiResponse(responseCode = "409", description = "Company already exists")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<CompanyResponse> createCompany(@Valid @RequestBody CreateCompanyRequest request,
                                                         Authentication authentication) {
        logger.info("Create company request for name: {}", request.getName());

        UUID createdBy = UUID.fromString(authentication.getName());
        CompanyResponse response = companyService.createCompany(request, createdBy);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all companies", description = "Retrieve all companies with pagination (SUPER_ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Companies retrieved successfully")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<CompanyResponse>> getAllCompanies(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        logger.info("Get all companies request - page: {}, size: {}", page, size);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CompanyResponse> response = companyService.getAllCompanies(pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{companyId}")
    @Operation(summary = "Get company by ID", description = "Retrieve company information by ID")
    @ApiResponse(responseCode = "200", description = "Company found")
    @ApiResponse(responseCode = "404", description = "Company not found")
    @PreAuthorize("hasRole('SUPER_ADMIN') or @companyService.canAccessCompany(authentication.name, #companyId)")
    public ResponseEntity<CompanyResponse> getCompanyById(@PathVariable UUID companyId) {
        logger.info("Get company request for ID: {}", companyId);

        CompanyResponse response = companyService.getCompanyById(companyId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{companyId}/validation/user-limit")
    public ResponseEntity<CompanyValidationResponse> validateUserLimit(@PathVariable UUID companyId) {
        CompanyResponse company = companyService.getCompanyById(companyId);
        boolean canAdd = companyService.canAddUser(companyId);

        CompanyValidationResponse response = CompanyValidationResponse.builder()
                .canAddUser(canAdd)
                .currentUsers(company.getCurrentUserCount())
                .maxUsers(company.getMaxUsers())
                .subscriptionPlan(company.getSubscriptionPlan().name())
                .message(canAdd ? "Can add user" : "User limit exceeded")
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{companyId}/users/increment")
    public ResponseEntity<Void> incrementUserCount(@PathVariable UUID companyId) {
        companyService.incrementUserCount(companyId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{companyId}/users/decrement")
    public ResponseEntity<Void> decrementUserCount(@PathVariable UUID companyId) {
        companyService.decrementUserCount(companyId);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/subdomain/{subdomain}")
    @Operation(summary = "Get company by subdomain", description = "Retrieve company information by subdomain")
    @ApiResponse(responseCode = "200", description = "Company found")
    @ApiResponse(responseCode = "404", description = "Company not found")
    public ResponseEntity<CompanyResponse> getCompanyBySubdomain(@PathVariable String subdomain) {
        logger.info("Get company request for subdomain: {}", subdomain);

        CompanyResponse response = companyService.getCompanyBySubdomain(subdomain);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search companies", description = "Search companies by name, email, or industry")
    @ApiResponse(responseCode = "200", description = "Search completed successfully")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<CompanyResponse>> searchCompanies(
            @Parameter(description = "Search term") @RequestParam String q,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {

        logger.info("Search companies request with term: {}", q);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CompanyResponse> response = companyService.searchCompanies(q, pageable);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{companyId}")
    @Operation(summary = "Update company", description = "Update company information")
    @ApiResponse(responseCode = "200", description = "Company updated successfully")
    @ApiResponse(responseCode = "404", description = "Company not found")
    @ApiResponse(responseCode = "400", description = "Invalid company data")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<CompanyResponse> updateCompany(@PathVariable UUID companyId,
                                                         @Valid @RequestBody UpdateCompanyRequest request,
                                                         Authentication authentication) {
        logger.info("Update company request for ID: {}", companyId);

        UUID updatedBy = UUID.fromString(authentication.getName());
        CompanyResponse response = companyService.updateCompany(companyId, request, updatedBy);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{companyId}/subscription")
    @Operation(summary = "Update subscription plan", description = "Update company subscription plan (SUPER_ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Subscription updated successfully")
    @ApiResponse(responseCode = "404", description = "Company not found")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<CompanyResponse> updateSubscriptionPlan(@PathVariable UUID companyId,
                                                                  @RequestParam SubscriptionPlan plan,
                                                                  Authentication authentication) {
        logger.info("Update subscription plan request for company: {} to plan: {}", companyId, plan);

        UUID updatedBy = UUID.fromString(authentication.getName());
        CompanyResponse response = companyService.updateSubscriptionPlan(companyId, plan, updatedBy);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{companyId}/suspend")
    @Operation(summary = "Suspend company", description = "Suspend company operations (SUPER_ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Company suspended successfully")
    @ApiResponse(responseCode = "404", description = "Company not found")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> suspendCompany(@PathVariable UUID companyId,
                                                              Authentication authentication) {
        logger.info("Suspend company request for ID: {}", companyId);

        UUID updatedBy = UUID.fromString(authentication.getName());
        companyService.suspendCompany(companyId, updatedBy);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Company suspended successfully");
        response.put("companyId", companyId.toString());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{companyId}/activate")
    @Operation(summary = "Activate company", description = "Activate company operations (SUPER_ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Company activated successfully")
    @ApiResponse(responseCode = "404", description = "Company not found")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> activateCompany(@PathVariable UUID companyId,
                                                               Authentication authentication) {
        logger.info("Activate company request for ID: {}", companyId);

        UUID updatedBy = UUID.fromString(authentication.getName());
        companyService.activateCompany(companyId, updatedBy);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Company activated successfully");
        response.put("companyId", companyId.toString());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get companies by status", description = "Retrieve companies by status")
    @ApiResponse(responseCode = "200", description = "Companies retrieved successfully")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<CompanyResponse>> getCompaniesByStatus(
            @PathVariable CompanyStatus status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        logger.info("Get companies by status request for status: {}", status);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CompanyResponse> response = companyService.getCompaniesByStatus(status, pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active companies", description = "Retrieve all active companies")
    @ApiResponse(responseCode = "200", description = "Active companies retrieved successfully")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<CompanyResponse>> getActiveCompanies(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {

        logger.info("Get active companies request");

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CompanyResponse> response = companyService.getActiveCompanies(pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{companyId}/validation/user")
    @Operation(summary = "Check if company can add user", description = "Validate if company can add new user")
    @ApiResponse(responseCode = "200", description = "Validation result")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Map<String, Boolean>> canAddUser(@PathVariable UUID companyId) {
        logger.info("Check can add user for company: {}", companyId);

        boolean canAdd = companyService.canAddUser(companyId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("canAddUser", canAdd);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{companyId}/validation/vehicle")
    @Operation(summary = "Check if company can add vehicle", description = "Validate if company can add new vehicle")
    @ApiResponse(responseCode = "200", description = "Validation result")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Map<String, Boolean>> canAddVehicle(@PathVariable UUID companyId) {
        logger.info("Check can add vehicle for company: {}", companyId);

        boolean canAdd = companyService.canAddVehicle(companyId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("canAddVehicle", canAdd);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{companyId}/validation/access")
    @Operation(summary = "Validate company access", description = "Validate if company can perform operations")
    @ApiResponse(responseCode = "200", description = "Company access is valid")
    @ApiResponse(responseCode = "400", description = "Company access is invalid")
    public ResponseEntity<Map<String, String>> validateCompanyAccess(@PathVariable UUID companyId) {
        logger.info("Validate company access for ID: {}", companyId);

        companyService.validateCompanyAccess(companyId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Company access is valid");
        response.put("companyId", companyId.toString());

        return ResponseEntity.ok(response);
    }
}