package com.fleetmanagement.companyservice.service;

import com.fleetmanagement.companyservice.domain.entity.Company;
import com.fleetmanagement.companyservice.domain.enums.CompanyStatus;
import com.fleetmanagement.companyservice.domain.enums.SubscriptionPlan;
import com.fleetmanagement.companyservice.dto.request.CreateCompanyRequest;
import com.fleetmanagement.companyservice.dto.request.UpdateCompanyRequest;
import com.fleetmanagement.companyservice.dto.response.CompanyResponse;
import com.fleetmanagement.companyservice.exception.CompanyNotFoundException;
import com.fleetmanagement.companyservice.exception.SubscriptionLimitException;
import com.fleetmanagement.companyservice.repository.CompanyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional
public class CompanyService {

    private static final Logger logger = LoggerFactory.getLogger(CompanyService.class);

    private final CompanyRepository companyRepository;

    @Autowired
    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    /**
     * Create a new company (SUPER_ADMIN only)
     */
    public CompanyResponse createCompany(CreateCompanyRequest request, UUID createdBy) {
        logger.info("Creating new company: {}", request.getName());

        // Validate subdomain uniqueness
        if (request.getSubdomain() != null &&
                companyRepository.existsBySubdomain(request.getSubdomain())) {
            throw new IllegalArgumentException("Subdomain already exists");
        }

        // Validate email uniqueness
        if (companyRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Company with this email already exists");
        }

        // Create company entity
        Company company = Company.builder()
                .name(request.getName())
                .subdomain(request.getSubdomain())
                .industry(request.getIndustry())
                .phone(request.getPhone())
                .email(request.getEmail())
                .website(request.getWebsite())
                .address(request.getAddress())
                .logoUrl(request.getLogoUrl())
                .status(CompanyStatus.TRIAL)
                .subscriptionPlan(SubscriptionPlan.BASIC)
                .maxUsers(SubscriptionPlan.BASIC.getMaxUsers())
                .maxVehicles(SubscriptionPlan.BASIC.getMaxVehicles())
                .currentUserCount(0)
                .currentVehicleCount(0)
                .trialEndDate(LocalDate.now().plusDays(30)) // 30-day trial
                .timezone(request.getTimezone() != null ? request.getTimezone() : "UTC")
                .language(request.getLanguage() != null ? request.getLanguage() : "en")
                .notes(request.getNotes())
                .contactPersonName(request.getContactPersonName())
                .contactPersonTitle(request.getContactPersonTitle())
                .contactPersonEmail(request.getContactPersonEmail())
                .contactPersonPhone(request.getContactPersonPhone())
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .build();

        Company savedCompany = companyRepository.save(company);
        logger.info("Company created successfully with ID: {}", savedCompany.getId());

        return mapToResponse(savedCompany);
    }

    /**
     * Get all companies (SUPER_ADMIN only)
     */
    @Transactional(readOnly = true)
    public Page<CompanyResponse> getAllCompanies(Pageable pageable) {
        logger.info("Retrieving all companies");

        Page<Company> companies = companyRepository.findAll(pageable);
        return companies.map(this::mapToResponse);
    }

    /**
     * Get company by ID
     */
    @Transactional(readOnly = true)
    public CompanyResponse getCompanyById(UUID companyId) {
        logger.info("Retrieving company by ID: {}", companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyId));

        return mapToResponse(company);
    }

    /**
     * Get company by subdomain
     */
    @Transactional(readOnly = true)
    public CompanyResponse getCompanyBySubdomain(String subdomain) {
        logger.info("Retrieving company by subdomain: {}", subdomain);

        Company company = companyRepository.findBySubdomain(subdomain)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with subdomain: " + subdomain));

        return mapToResponse(company);
    }

    /**
     * Search companies
     */
    @Transactional(readOnly = true)
    public Page<CompanyResponse> searchCompanies(String searchTerm, Pageable pageable) {
        logger.info("Searching companies with term: {}", searchTerm);

        Page<Company> companies = companyRepository.searchCompanies(searchTerm, pageable);
        return companies.map(this::mapToResponse);
    }

    /**
     * Update company information
     */
    public CompanyResponse updateCompany(UUID companyId, UpdateCompanyRequest request, UUID updatedBy) {
        logger.info("Updating company: {}", companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyId));

        // Update fields only if provided
        if (request.getName() != null) {
            company.setName(request.getName());
        }
        if (request.getIndustry() != null) {
            company.setIndustry(request.getIndustry());
        }
        if (request.getPhone() != null) {
            company.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            // Check email uniqueness
            if (!request.getEmail().equals(company.getEmail()) &&
                    companyRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Company with this email already exists");
            }
            company.setEmail(request.getEmail());
        }
        if (request.getWebsite() != null) {
            company.setWebsite(request.getWebsite());
        }
        if (request.getAddress() != null) {
            company.setAddress(request.getAddress());
        }
        if (request.getLogoUrl() != null) {
            company.setLogoUrl(request.getLogoUrl());
        }
        if (request.getTimezone() != null) {
            company.setTimezone(request.getTimezone());
        }
        if (request.getLanguage() != null) {
            company.setLanguage(request.getLanguage());
        }
        if (request.getNotes() != null) {
            company.setNotes(request.getNotes());
        }
        if (request.getContactPersonName() != null) {
            company.setContactPersonName(request.getContactPersonName());
        }
        if (request.getContactPersonTitle() != null) {
            company.setContactPersonTitle(request.getContactPersonTitle());
        }
        if (request.getContactPersonEmail() != null) {
            company.setContactPersonEmail(request.getContactPersonEmail());
        }
        if (request.getContactPersonPhone() != null) {
            company.setContactPersonPhone(request.getContactPersonPhone());
        }

        company.setUpdatedBy(updatedBy);

        Company savedCompany = companyRepository.save(company);
        logger.info("Company updated successfully: {}", companyId);

        return mapToResponse(savedCompany);
    }

    /**
     * Update company subscription plan
     */
    public CompanyResponse updateSubscriptionPlan(UUID companyId, SubscriptionPlan plan, UUID updatedBy) {
        logger.info("Updating subscription plan for company: {} to {}", companyId, plan);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyId));

        company.setSubscriptionPlan(plan);
        company.setMaxUsers(plan.getMaxUsers());
        company.setMaxVehicles(plan.getMaxVehicles());
        company.setUpdatedBy(updatedBy);

        // If upgrading from trial, activate the company
        if (company.getStatus() == CompanyStatus.TRIAL) {
            company.setStatus(CompanyStatus.ACTIVE);
            company.setTrialEndDate(null);
        }

        Company savedCompany = companyRepository.save(company);
        logger.info("Subscription plan updated successfully for company: {}", companyId);

        return mapToResponse(savedCompany);
    }

    /**
     * Suspend company
     */
    public void suspendCompany(UUID companyId, UUID updatedBy) {
        logger.info("Suspending company: {}", companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyId));

        company.setStatus(CompanyStatus.SUSPENDED);
        company.setUpdatedBy(updatedBy);

        companyRepository.save(company);
        logger.info("Company suspended successfully: {}", companyId);
    }

    /**
     * Activate company
     */
    public void activateCompany(UUID companyId, UUID updatedBy) {
        logger.info("Activating company: {}", companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyId));

        company.setStatus(CompanyStatus.ACTIVE);
        company.setUpdatedBy(updatedBy);

        companyRepository.save(company);
        logger.info("Company activated successfully: {}", companyId);
    }

    /**
     * Check if company can add user (with subscription limit validation)
     */
    @Transactional(readOnly = true)
    public boolean canAddUser(UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyId));

        return company.getCurrentUserCount() < company.getMaxUsers();
    }

    /**
     * Check if company can add vehicle (with subscription limit validation)
     */
    @Transactional(readOnly = true)
    public boolean canAddVehicle(UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyId));

        return company.getCurrentVehicleCount() < company.getMaxVehicles();
    }

    /**
     * Increment user count (called when user is added)
     */
    public void incrementUserCount(UUID companyId) {
        logger.info("Incrementing user count for company: {}", companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyId));

        if (company.getCurrentUserCount() >= company.getMaxUsers()) {
            throw new SubscriptionLimitException("User limit exceeded for company: " + company.getName());
        }

        company.setCurrentUserCount(company.getCurrentUserCount() + 1);
        companyRepository.save(company);

        logger.info("User count incremented for company: {} (now: {})", companyId, company.getCurrentUserCount());
    }

    /**
     * Decrement user count (called when user is removed)
     */
    public void decrementUserCount(UUID companyId) {
        logger.info("Decrementing user count for company: {}", companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyId));

        if (company.getCurrentUserCount() > 0) {
            company.setCurrentUserCount(company.getCurrentUserCount() - 1);
            companyRepository.save(company);
        }

        logger.info("User count decremented for company: {} (now: {})", companyId, company.getCurrentUserCount());
    }

    /**
     * Increment vehicle count (called when vehicle is added)
     */
    public void incrementVehicleCount(UUID companyId) {
        logger.info("Incrementing vehicle count for company: {}", companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyId));

        if (company.getCurrentVehicleCount() >= company.getMaxVehicles()) {
            throw new SubscriptionLimitException("Vehicle limit exceeded for company: " + company.getName());
        }

        company.setCurrentVehicleCount(company.getCurrentVehicleCount() + 1);
        companyRepository.save(company);

        logger.info("Vehicle count incremented for company: {} (now: {})", companyId, company.getCurrentVehicleCount());
    }

    /**
     * Decrement vehicle count (called when vehicle is removed)
     */
    public void decrementVehicleCount(UUID companyId) {
        logger.info("Decrementing vehicle count for company: {}", companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyId));

        if (company.getCurrentVehicleCount() > 0) {
            company.setCurrentVehicleCount(company.getCurrentVehicleCount() - 1);
            companyRepository.save(company);
        }

        logger.info("Vehicle count decremented for company: {} (now: {})", companyId, company.getCurrentVehicleCount());
    }

    /**
     * Get companies by status
     */
    @Transactional(readOnly = true)
    public Page<CompanyResponse> getCompaniesByStatus(CompanyStatus status, Pageable pageable) {
        logger.info("Retrieving companies by status: {}", status);

        Page<Company> companies = companyRepository.findByStatus(status, pageable);
        return companies.map(this::mapToResponse);
    }

    /**
     * Get active companies
     */
    @Transactional(readOnly = true)
    public Page<CompanyResponse> getActiveCompanies(Pageable pageable) {
        logger.info("Retrieving active companies");

        Page<Company> companies = companyRepository.findActiveCompanies(pageable);
        return companies.map(this::mapToResponse);
    }

    /**
     * Validate company status and subscription
     */
    @Transactional(readOnly = true)
    public void validateCompanyAccess(UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyId));

        if (company.getStatus() == CompanyStatus.SUSPENDED) {
            throw new IllegalArgumentException("Company is suspended and cannot perform operations");
        }

        if (company.getStatus() == CompanyStatus.TRIAL &&
                company.getTrialEndDate() != null &&
                company.getTrialEndDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Company trial has expired");
        }
    }

    /**
     * Map Company entity to CompanyResponse DTO
     */
    private CompanyResponse mapToResponse(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .subdomain(company.getSubdomain())
                .industry(company.getIndustry())
                .phone(company.getPhone())
                .email(company.getEmail())
                .website(company.getWebsite())
                .address(company.getAddress())
                .logoUrl(company.getLogoUrl())
                .status(company.getStatus())
                .subscriptionPlan(company.getSubscriptionPlan())
                .timezone(company.getTimezone())
                .language(company.getLanguage())
                .notes(company.getNotes())
                .maxUsers(company.getMaxUsers())
                .maxVehicles(company.getMaxVehicles())
                .currentUserCount(company.getCurrentUserCount())
                .currentVehicleCount(company.getCurrentVehicleCount())
                .trialEndDate(company.getTrialEndDate())
                .contactPersonName(company.getContactPersonName())
                .contactPersonTitle(company.getContactPersonTitle())
                .contactPersonEmail(company.getContactPersonEmail())
                .contactPersonPhone(company.getContactPersonPhone())
                .createdBy(company.getCreatedBy())
                .updatedBy(company.getUpdatedBy())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }
}