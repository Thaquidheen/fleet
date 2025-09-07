package com.fleetmanagement.companyservice.service;

import com.fleetmanagement.companyservice.domain.entity.Company;
import com.fleetmanagement.companyservice.domain.enums.CompanyStatus;
import com.fleetmanagement.companyservice.domain.enums.SubscriptionPlan;
import com.fleetmanagement.companyservice.dto.request.CreateCompanyRequest;
import com.fleetmanagement.companyservice.dto.request.UpdateCompanyRequest;
import com.fleetmanagement.companyservice.dto.response.CompanyResponse;
import com.fleetmanagement.companyservice.exception.CompanyNotFoundException;
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

        // Create company entity
        Company company = Company.builder()
                .name(request.getName())
                .subdomain(request.getSubdomain())
                .industry(request.getIndustry())
                .phone(request.getPhone())
                .email(request.getEmail())
                .website(request.getWebsite())
                .address(request.getAddress())
                .status(CompanyStatus.TRIAL)
                .subscriptionPlan(SubscriptionPlan.BASIC)
                .maxUsers(SubscriptionPlan.BASIC.getMaxUsers())
                .maxVehicles(SubscriptionPlan.BASIC.getMaxVehicles())
                .trialEndDate(LocalDate.now().plusDays(30)) // 30-day trial
                .timezone(request.getTimezone() != null ? request.getTimezone() : "UTC")
                .language(request.getLanguage() != null ? request.getLanguage() : "en")
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
     * Update company information
     */
    public CompanyResponse updateCompany(UUID companyId, UpdateCompanyRequest request, UUID updatedBy) {
        logger.info("Updating company: {}", companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyId));

        // Update fields
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
     * Check if company can add user
     */
    @Transactional(readOnly = true)
    public boolean canAddUser(UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyId));

        return company.canAddUser();
    }

    /**
     * Check if company can add vehicle
     */
    @Transactional(readOnly = true)
    public boolean canAddVehicle(UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyId));

        return company.canAddVehicle();
    }

    /**
     * Increment user count
     */
    public void incrementUserCount(UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyId));

        company.incrementUserCount();
        companyRepository.save(company);

        logger.debug("User count incremented for company: {} (new count: {})",
                companyId, company.getCurrentUserCount());
    }

    /**
     * Decrement user count
     */
    public void decrementUserCount(UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyId));

        company.decrementUserCount();
        companyRepository.save(company);

        logger.debug("User count decremented for company: {} (new count: {})",
                companyId, company.getCurrentUserCount());
    }

    // Helper method to map entity to response
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
                .status(company.getStatus())
                .subscriptionPlan(company.getSubscriptionPlan())
                .maxUsers(company.getMaxUsers())
                .maxVehicles(company.getMaxVehicles())
                .currentUserCount(company.getCurrentUserCount())
                .currentVehicleCount(company.getCurrentVehicleCount())
                .trialEndDate(company.getTrialEndDate())
                .logoUrl(company.getLogoUrl())
                .timezone(company.getTimezone())
                .language(company.getLanguage())
                .utilizationPercentage(company.getUtilizationPercentage())
                .isActive(company.isActive())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }
}