package com.fleetmanagement.userservice.dto.response;

import com.fleetmanagement.userservice.domain.enums.UserRole;

import java.util.UUID;

public class AuthenticationResponse {

    private boolean success;
    private String message;
    private UUID userId;
    private String username;
    private String email;
    private UserRole role;
    private UUID companyId;
    private Boolean emailVerified;
    private UUID sessionId;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private Long refreshExpiresIn;
    private Boolean requiresPasswordChange;

    // Constructors
    public AuthenticationResponse() {}

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private AuthenticationResponse response = new AuthenticationResponse();

        public Builder success(boolean success) {
            response.success = success;
            return this;
        }

        public Builder message(String message) {
            response.message = message;
            return this;
        }

        public Builder userId(UUID userId) {
            response.userId = userId;
            return this;
        }

        public Builder username(String username) {
            response.username = username;
            return this;
        }

        public Builder email(String email) {
            response.email = email;
            return this;
        }

        public Builder role(UserRole role) {
            response.role = role;
            return this;
        }

        public Builder companyId(UUID companyId) {
            response.companyId = companyId;
            return this;
        }

        public Builder emailVerified(Boolean emailVerified) {
            response.emailVerified = emailVerified;
            return this;
        }

        public Builder sessionId(UUID sessionId) {
            response.sessionId = sessionId;
            return this;
        }

        public Builder accessToken(String accessToken) {
            response.accessToken = accessToken;
            return this;
        }

        public Builder refreshToken(String refreshToken) {
            response.refreshToken = refreshToken;
            return this;
        }

        public Builder tokenType(String tokenType) {
            response.tokenType = tokenType;
            return this;
        }

        public Builder expiresIn(Long expiresIn) {
            response.expiresIn = expiresIn;
            return this;
        }

        public Builder refreshExpiresIn(Long refreshExpiresIn) {
            response.refreshExpiresIn = refreshExpiresIn;
            return this;
        }

        public Builder requiresPasswordChange(Boolean requiresPasswordChange) {
            response.requiresPasswordChange = requiresPasswordChange;
            return this;
        }

        public AuthenticationResponse build() {
            return response;
        }
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }

    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }

    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public Long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }

    public Long getRefreshExpiresIn() { return refreshExpiresIn; }
    public void setRefreshExpiresIn(Long refreshExpiresIn) { this.refreshExpiresIn = refreshExpiresIn; }

    public Boolean getRequiresPasswordChange() { return requiresPasswordChange; }
    public void setRequiresPasswordChange(Boolean requiresPasswordChange) {
        this.requiresPasswordChange = requiresPasswordChange;
    }
}