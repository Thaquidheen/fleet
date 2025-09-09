package com.fleetmanagement.userservice.controller;

import com.fleetmanagement.userservice.dto.response.ApiResponse;
import com.fleetmanagement.userservice.service.EmailVerificationService;
import com.fleetmanagement.userservice.service.EmailVerificationService.EmailVerificationStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/email-verification")
@Tag(name = "Email Verification", description = "Email verification and management operations")
@Validated
public class EmailVerificationController {

    private static final Logger logger = LoggerFactory.getLogger(EmailVerificationController.class);

    private final EmailVerificationService emailVerificationService;

    @Autowired
    public EmailVerificationController(EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify email", description = "Verify user email address using verification token")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Email verified successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired token")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @RequestParam @NotBlank @Parameter(description = "Verification token") String token) {

        logger.info("Email verification request with token: {}", maskToken(token));

        emailVerificationService.verifyEmail(token);

        ApiResponse<Void> response = ApiResponse.success(
                null,
                "Email verified successfully"
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend")
    @Operation(summary = "Resend verification email", description = "Resend verification email to user")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Verification email sent successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Email already verified or rate limited")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    @PreAuthorize("hasRole('USER') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> resendVerificationEmail(
            @RequestParam(required = false) @Parameter(description = "User ID (admin only)") UUID userId,
            Authentication authentication) {

        UUID targetUserId = userId;

        if (targetUserId == null) {
            targetUserId = getUserIdFromAuth(authentication);
        } else {
            validateAdminAccessOrSameUser(authentication, targetUserId);
        }

        logger.info("Resend verification email request for user: {}", targetUserId);

        emailVerificationService.resendVerificationEmail(targetUserId);

        ApiResponse<Void> response = ApiResponse.success(
                null,
                "Verification email sent successfully"
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    @Operation(summary = "Get verification status", description = "Get email verification status for user")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Verification status retrieved successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    @PreAuthorize("hasRole('USER') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<EmailVerificationStatus>> getVerificationStatus(
            @RequestParam(required = false) @Parameter(description = "User ID (admin only)") UUID userId,
            Authentication authentication) {

        UUID targetUserId = userId;

        if (targetUserId == null) {
            targetUserId = getUserIdFromAuth(authentication);
        } else {
            validateAdminAccessOrSameUser(authentication, targetUserId);
        }

        logger.debug("Get verification status request for user: {}", targetUserId);

        EmailVerificationStatus status = emailVerificationService.getVerificationStatus(targetUserId);

        ApiResponse<EmailVerificationStatus> response = ApiResponse.success(
                status,
                "Verification status retrieved successfully"
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/required")
    @Operation(summary = "Check if verification required", description = "Check if email verification is required for user")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Verification requirement status retrieved")
    @PreAuthorize("hasRole('USER') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<VerificationRequiredResponse>> isVerificationRequired(
            @RequestParam(required = false) @Parameter(description = "User ID (admin only)") UUID userId,
            Authentication authentication) {

        UUID targetUserId = userId;

        if (targetUserId == null) {
            targetUserId = getUserIdFromAuth(authentication);
        } else {
            validateAdminAccessOrSameUser(authentication, targetUserId);
        }

        logger.debug("Check verification requirement for user: {}", targetUserId);

        boolean isRequired = emailVerificationService.isVerificationRequired(targetUserId);

        VerificationRequiredResponse verificationRequired = VerificationRequiredResponse.builder()
                .userId(targetUserId)
                .isRequired(isRequired)
                .message(isRequired ? "Email verification is required" : "Email verification is not required")
                .build();

        ApiResponse<VerificationRequiredResponse> response = ApiResponse.success(
                verificationRequired,
                "Verification requirement status retrieved successfully"
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/send")
    @Operation(summary = "Admin: Send verification email", description = "Admin endpoint to send verification email for any user")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Verification email sent successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> adminSendVerificationEmail(
            @RequestParam @Parameter(description = "User ID") UUID userId,
            Authentication authentication) {

        logger.info("Admin send verification email request for user: {} by admin: {}",
                userId, getUserIdFromAuth(authentication));

        emailVerificationService.sendVerificationEmail(userId);

        ApiResponse<Void> response = ApiResponse.success(
                null,
                "Verification email sent successfully"
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/statistics")
    @Operation(summary = "Admin: Get verification statistics", description = "Get email verification statistics for company")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<VerificationStatistics>> getVerificationStatistics(
            @RequestParam(required = false) @Parameter(description = "Company ID (super admin only)") UUID companyId,
            Authentication authentication) {

        UUID targetCompanyId = companyId;

        if (targetCompanyId == null) {
            targetCompanyId = getCompanyIdFromAuth(authentication);
        } else {
            validateSuperAdminAccess(authentication);
        }

        logger.debug("Get verification statistics for company: {}", targetCompanyId);

        VerificationStatistics statistics = getVerificationStatisticsForCompany(targetCompanyId);

        ApiResponse<VerificationStatistics> response = ApiResponse.success(
                statistics,
                "Verification statistics retrieved successfully"
        );

        return ResponseEntity.ok(response);
    }

    private UUID getUserIdFromAuth(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    private UUID getCompanyIdFromAuth(Authentication authentication) {
        return UUID.fromString(authentication.getDetails().toString());
    }

    private void validateAdminAccessOrSameUser(Authentication authentication, UUID targetUserId) {
        UUID requestingUserId = getUserIdFromAuth(authentication);

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_COMPANY_ADMIN") ||
                        auth.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (!isAdmin && !requestingUserId.equals(targetUserId)) {
            throw new SecurityException("Access denied: Can only access own verification status");
        }
    }

    private void validateSuperAdminAccess(Authentication authentication) {
        boolean isSuperAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (!isSuperAdmin) {
            throw new SecurityException("Access denied: Super admin access required");
        }
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 8) {
            return "***";
        }
        return token.substring(0, 4) + "***" + token.substring(token.length() - 4);
    }

    private VerificationStatistics getVerificationStatisticsForCompany(UUID companyId) {
        return VerificationStatistics.builder()
                .companyId(companyId)
                .totalUsers(0)
                .verifiedUsers(0)
                .pendingVerifications(0)
                .verificationRate(0.0)
                .build();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class VerificationRequiredResponse {
        private UUID userId;
        private boolean isRequired;
        private String message;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class VerificationStatistics {
        private UUID companyId;
        private int totalUsers;
        private int verifiedUsers;
        private int pendingVerifications;
        private double verificationRate;
    }
}