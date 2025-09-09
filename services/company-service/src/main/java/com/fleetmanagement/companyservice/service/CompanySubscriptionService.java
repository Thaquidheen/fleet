package com.fleetmanagement.companyservice.service;

import com.fleetmanagement.companyservice.domain.entity.Company;
import com.fleetmanagement.companyservice.domain.enums.SubscriptionPlan;
import com.fleetmanagement.companyservice.dto.response.CompanyValidationResponse;
import com.fleetmanagement.companyservice.exception.CompanyNotFoundException;
import com.fleetmanagement.companyservice.repository.CompanyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CompanySubscriptionService {

    private static final Logger logger = LoggerFactory.getLogger(CompanySubscriptionService.class);

    private final CompanyRepository companyRepository;

    @Autowired
    public CompanySubscriptionService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public CompanyValidationResponse validateCompanyLimits(UUID companyId) {
        logger.debug("Validating company limits for: {}", companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found: " + companyId));

        int currentUsers = company.getCurrentUserCount();
        int maxUsers = getMaxUsersForSubscription(company.getSubscriptionPlan());
        int availableSlots = maxUsers == -1 ? Integer.MAX_VALUE : Math.max(0, maxUsers - currentUsers);
        boolean canAddUser = maxUsers == -1 || currentUsers < maxUsers;

        return CompanyValidationResponse.builder()
                .companyId(companyId)
                .subscriptionPlan(company.getSubscriptionPlan().name())
                .currentUsers(currentUsers)
                .maxUsers(maxUsers)
                .availableSlots(availableSlots)
                .canAddUser(canAddUser)
                .message(buildValidationMessage(canAddUser, currentUsers, maxUsers))
                .build();
    }

    private int getMaxUsersForSubscription(SubscriptionPlan plan) {
        return switch (plan) {
            case BASIC -> 5;
            case PREMIUM -> 50;
            case ENTERPRISE -> 1000;
            case OWNER -> -1; // Unlimited
            default -> 5;
        };
    }

    private String buildValidationMessage(boolean canAddUser, int currentUsers, int maxUsers) {
        if (maxUsers == -1) {
            return "Unlimited users allowed";
        }
        if (canAddUser) {
            return String.format("Can add user (%d/%d used)", currentUsers, maxUsers);
        } else {
            return String.format("User limit exceeded (%d/%d used)", currentUsers, maxUsers);
        }
    }
}