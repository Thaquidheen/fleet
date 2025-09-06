package com.fleetmanagement.userservice.dto.request;

import com.fleetmanagement.userservice.domain.enums.UserRole;
import com.fleetmanagement.userservice.domain.enums.UserStatus;

import java.util.UUID;

public class UserSearchRequest {

    private String searchTerm;
    private UUID companyId;
    private UserRole role;
    private UserStatus status;
    private Boolean emailVerified;

    // Constructors
    public UserSearchRequest() {}

    // Getters and Setters
    public String getSearchTerm() { return searchTerm; }
    public void setSearchTerm(String searchTerm) { this.searchTerm = searchTerm; }

    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }
}