package com.fleetmanagement.companyservice.service;

import com.fleetmanagement.companyservice.domain.entity.Company;
import com.fleetmanagement.companyservice.domain.enums.CompanyStatus;
import com.fleetmanagement.companyservice.domain.enums.SubscriptionPlan;
import com.fleetmanagement.companyservice.dto.request.CreateCompanyRequest;
import com.fleetmanagement.companyservice.dto.request.UpdateCompanyRequest;
import com.fleetmanagement.companyservice.dto.response.CompanyResponse;
import com.fleetmanagement.companyservice.dto.response.CompanyValidationResponse;
import com.fleetmanagement.companyservice.exception.CompanyNotFoundException;
import com.fleetmanagement.companyservice.service.CompanySubscriptionService;
import com.fleetmanagement.companyservice.exception.SubscriptionLimitException;
import com.fleetmanagement.companyservice.service.EventPublishingService;
import com.fleetmanagement.companyservice.repository.CompanyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class CompanyService {

    private static final Logger logger = LoggerFactory.getLogger(CompanyService.class);

    private final CompanyRepository companyRepository;
    private final CompanySubscriptionService subscriptionService;
    private final EventPublishingService eventPublishingService;

    @Autowired
    public CompanyService(CompanyRepository companyRepository,
                          CompanySubscriptionService subscriptionService,
                          EventPublishingService eventPublishingService) {
        this.companyRepository = companyRepository;
        this.subscriptionService = subscriptionService;
        this.eventPublishingService = eventPublishingService;
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
                .subscriptionPlan(request.getSubscriptionPlan() != null ?
                        request.getSubscriptionPlan() : SubscriptionPlan.BASIC)
                .timezone(request.getTimezone() != null ? request.getTimezone() : "UTC")
                .language(request.getLanguage() != null ? request.getLanguage() : "en")
                .notes(request.getNotes())
                .contactPersonName(request.getContactPersonName())
                .contactPersonTitle(request.getContactPersonTitle())
                .contactPersonEmail(request.getContactPersonEmail())
                .contactPersonPhone(request.getContactPersonPhone())
                .trialEndDate(LocalDate.now().plusDays(30))
                .maxUsers(subscriptionService.getMaxUsersForPlan(request.getSubscriptionPlan() != null ? request.getSubscriptionPlan() : SubscriptionPlan.BASIC))
                .maxVehicles(subscriptionService.getMaxVehiclesForPlan(request.getSubscriptionPlan() != null ? request.getSubscriptionPlan() : SubscriptionPlan.BASIC))
                .currentUserCount(0)
                .currentVehicleCount(0)
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .build();

        Company savedCompany = companyRepository.save(company);
        logger.info("Company created successfully with ID: {}", savedCompany.getId());

        // Publish company created event
        eventPublishingService.publishCompanyCreatedEvent(savedCompany);

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
     * Get companies by status
     */
    @Transactional(readOnly = true)
    public Page<CompanyResponse> getCompaniesByStatus(CompanyStatus status, Pageable pageable) {
        logger.info("Retrieving companies by status: {}", status);

        Page<Company> companies = companyRepository.findByStatus(status, pageable);
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

        // Publish company updated event
        eventPublishingService.publishCompanyUpdatedEvent(savedCompany);

        return mapToResponse(savedCompany);
    }

    /**
     * Delete company (SUPER_ADMIN only)
     */
    public CompanyResponse deleteCompany(UUID companyId, UUID deletedBy) {
        logger.info("Deleting company: {}", companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyId));

        // Soft delete - update status instead of actual deletion
        company.setStatus(CompanyStatus.SUSPENDED);
        company.setUpdatedBy(deletedBy);

        Company savedCompany = companyRepository.save(company);
        logger.info("Company deleted (soft delete) successfully: {}", companyId);

        // Publish company deleted event
        eventPublishingService.publishCompanyDeletedEvent(savedCompany);

        return mapToResponse(savedCompany);
    }

    /**
     * Update subscription plan
     */
    public CompanyResponse updateSubscription(UUID companyId, SubscriptionPlan subscriptionPlan, UUID updatedBy) {
        logger.info("Updating subscription for company: {} to plan: {}", companyId, subscriptionPlan);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyId));

        SubscriptionPlan oldPlan = company.getSubscriptionPlan();
        company.setSubscriptionPlan(subscriptionPlan);

        // Update limits based on new subscription plan
        company.setMaxUsers(subscriptionService.getMaxUsersForPlan(subscriptionPlan));
        company.setMaxVehicles(subscriptionService.getMaxVehiclesForPlan(subscriptionPlan));

        // If upgrading from trial, update status
        if (company.getStatus() == CompanyStatus.TRIAL && subscriptionPlan != SubscriptionPlan.BASIC) {
            company.setStatus(CompanyStatus.ACTIVE);
        }

        company.setUpdatedBy(updatedBy);

        Company savedCompany = companyRepository.save(company);
        logger.info("Subscription updated successfully for company: {}", companyId);

        // Publish subscription changed event
        eventPublishingService.publishCompanySubscriptionChangedEvent(savedCompany, oldPlan, subscriptionPlan);

        return mapToResponse(savedCompany);
    }

    /**
     * Activate company
     */
    public void activateCompany(UUID companyId, UUID activatedBy) {
        logger.info("Activating company: {}", companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyId));

        CompanyStatus oldStatus = company.getStatus();
        company.setStatus(CompanyStatus.ACTIVE);
        company.setUpdatedBy(activatedBy);

        companyRepository.save(company);
        logger.info("Company activated successfully: {}", companyId);

        // Publish status changed event
        eventPublishingService.publishCompanyStatusChangedEvent(company, oldStatus, CompanyStatus.ACTIVE);
    }

    /**
     * Deactivate company
     */
    public void deactivateCompany(UUID companyId, UUID deactivatedBy) {
        logger.info("Deactivating company: {}", companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyId));

        CompanyStatus oldStatus = company.getStatus();
        company.setStatus(CompanyStatus.CANCELLED);
        company.setUpdatedBy(deactivatedBy);

        companyRepository.save(company);
        logger.info("Company deactivated successfully: {}", companyId);

        // Publish status changed event
        eventPublishingService.publishCompanyStatusChangedEvent(company, oldStatus, CompanyStatus.CANCELLED);
    }

    /**
     * Reset trial period
     */
    public void resetTrialPeriod(UUID companyId, UUID resetBy) {
        logger.info("Resetting trial period for company: {}", companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyId));

        company.setStatus(CompanyStatus.TRIAL);
        company.setTrialEndDate(LocalDate.now().plusDays(30));
        company.setUpdatedBy(resetBy);

        companyRepository.save(company);
        logger.info("Trial period reset successfully for company: {}", companyId);

        // Publish status changed event
        eventPublishingService.publishCompanyStatusChangedEvent(company, company.getStatus(), CompanyStatus.TRIAL);
    }

    /**
     * Validate company subscription limits
     */
    @Transactional(readOnly = true)
    public CompanyValidationResponse validateCompanyLimits(UUID companyId) {
        logger.debug("Validating company limits for: {}", companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyId));

        int availableUserSlots = Math.max(0, company.getMaxUsers() - company.getCurrentUserCount());
        boolean canAddUser = availableUserSlots > 0;

        return CompanyValidationResponse.builder()
                .companyId(companyId)
                .subscriptionPlan(company.getSubscriptionPlan().name())
                .currentUsers(company.getCurrentUserCount())
                .maxUsers(company.getMaxUsers())
                .availableSlots(availableUserSlots)
                .canAddUser(canAddUser)
                .message(canAddUser ?
                        "Company can add more users" :
                        "Company has reached maximum user limit")
                .build();
    }

    /**
     * Update user count for company
     */
    public void updateUserCount(UUID companyId, int userCount) {
        logger.debug("Updating user count for company: {} to {}", companyId, userCount);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyId));

        int oldCount = company.getCurrentUserCount();
        company.setCurrentUserCount(userCount);

        companyRepository.save(company);

        // Publish user count changed event if there's a significant change
        if (Math.abs(oldCount - userCount) > 0) {
            eventPublishingService.publishCompanyUserCountChangedEvent(company, oldCount, userCount);
        }
    }

    /**
     * Update vehicle count for company
     */
    public void updateVehicleCount(UUID companyId, int vehicleCount) {
        logger.debug("Updating vehicle count for company: {} to {}", companyId, vehicleCount);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyId));

        company.setCurrentVehicleCount(vehicleCount);
        companyRepository.save(company);
    }

    /**
     * Check if company exists
     */
    @Transactional(readOnly = true)
    public boolean companyExists(UUID companyId) {
        return companyRepository.existsById(companyId);
    }

    /**
     * Check if company is active
     */
    @Transactional(readOnly = true)
    public boolean isCompanyActive(UUID companyId) {
        return companyRepository.findById(companyId)
                .map(company -> company.getStatus() == CompanyStatus.ACTIVE ||
                        company.getStatus() == CompanyStatus.TRIAL)
                .orElse(false);
    }

    // Private helper methods

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
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .createdBy(company.getCreatedBy())
                .updatedBy(company.getUpdatedBy())
                .build();
    }
}