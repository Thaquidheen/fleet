package com.fleetmanagement.userservice.controller;

//import com.fleetmanagement.userservice.dto.response.ApiResponse;
import com.fleetmanagement.userservice.service.EmailVerificationService;
import com.fleetmanagement.userservice.service.EmailVerificationService.EmailVerificationStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

/**
 * Email Verification Controller
 *
 * REST API endpoints for email verification process including:
 * - Email verification token validation
 * - Resend verification emails
 * - Check verification status
 * - Verification management
 */
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

    /**
     * Verify email using token
     */
    @PostMapping("/verify")
    @Operation(summary = "Verify email", description = "Verify user email address using verification token")
    @SwaggerApiResponse(responseCode = "200", description = "Email verified successfully")
    @SwaggerApiResponse(responseCode = "400", description = "Invalid or expired token")
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

    /**
     * Resend verification email
     */
    @PostMapping("/resend")
    @Operation(summary = "Resend verification email", description = "Resend verification email to user")
    @SwaggerApiResponse(responseCode = "200", description = "Verification email sent successfully")
    @SwaggerApiResponse(responseCode = "400", description = "Email already verified or rate limited")
    @SwaggerApiResponse(responseCode = "404", description = "User not found")
    @PreAuthorize("hasRole('USER') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> resendVerificationEmail(
            @RequestParam(required = false) @Parameter(description = "User ID (admin only)") UUID userId,
            Authentication authentication) {

        UUID targetUserId = userId;

        // If userId not provided, use authenticated user's ID
        if (targetUserId == null) {
            targetUserId = getUserIdFromAuth(authentication);
        } else {
            // Only admins can resend for other users
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

    /**
     * Get verification status for user
     */
    @GetMapping("/status")
    @Operation(summary = "Get verification status", description = "Get email verification status for user")
    @SwaggerApiResponse(responseCode = "200", description = "Verification status retrieved successfully")
    @SwaggerApiResponse(responseCode = "404", description = "User not found")
    @PreAuthorize("hasRole('USER') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<EmailVerificationStatus>> getVerificationStatus(
            @RequestParam(required = false) @Parameter(description = "User ID (admin only)") UUID userId,
            Authentication authentication) {

        UUID targetUserId = userId;

        // If userId not provided, use authenticated user's ID
        if (targetUserId == null) {
            targetUserId = getUserIdFromAuth(authentication);
        } else {
            // Only admins can check status for other users
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

    /**
     * Check if verification is required for user
     */
    @GetMapping("/required")
    @Operation(summary = "Check if verification required", description = "Check if email verification is required for user")
    @SwaggerApiResponse(responseCode = "200", description = "Verification requirement status retrieved")
    @PreAuthorize("hasRole('USER') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<VerificationRequiredResponse>> isVerificationRequired(
            @RequestParam(required = false) @Parameter(description = "User ID (admin only)") UUID userId,
            Authentication authentication) {

        UUID targetUserId = userId;

        // If userId not provided, use authenticated user's ID
        if (targetUserId == null) {
            targetUserId = getUserIdFromAuth(authentication);
        } else {
            // Only admins can check for other users
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

    /**
     * Admin endpoint to send verification email for any user
     */
    @PostMapping("/admin/send")
    @Operation(summary = "Admin: Send verification email", description = "Admin endpoint to send verification email for any user")
    @SwaggerApiResponse(responseCode = "200", description = "Verification email sent successfully")
    @SwaggerApiResponse(responseCode = "403", description = "Access denied")
    @SwaggerApiResponse(responseCode = "404", description = "User not found")
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

    /**
     * Admin endpoint to get verification statistics
     */
    @GetMapping("/admin/statistics")
    @Operation(summary = "Admin: Get verification statistics", description = "Get email verification statistics for company")
    @SwaggerApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    @SwaggerApiResponse(responseCode = "403", description = "Access denied")
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<VerificationStatistics>> getVerificationStatistics(
            @RequestParam(required = false) @Parameter(description = "Company ID (super admin only)") UUID companyId,
            Authentication authentication) {

        UUID targetCompanyId = companyId;

        // If companyId not provided, use authenticated user's company
        if (targetCompanyId == null) {
            targetCompanyId = getCompanyIdFromAuth(authentication);
        } else {
            // Only super admins can check statistics for other companies
            validateSuperAdminAccess(authentication);
        }

        logger.debug("Get verification statistics for company: {}", targetCompanyId);

        // This would need to be implemented in the service
        VerificationStatistics statistics = getVerificationStatisticsForCompany(targetCompanyId);

        ApiResponse<VerificationStatistics> response = ApiResponse.success(
                statistics,
                "Verification statistics retrieved successfully"
        );

        return ResponseEntity.ok(response);
    }

    // Helper methods
    private UUID getUserIdFromAuth(Authentication authentication) {
        // Extract user ID from JWT token or security context
        // Implementation depends on your security setup
        return UUID.fromString(authentication.getName());
    }

    private UUID getCompanyIdFromAuth(Authentication authentication) {
        // Extract company ID from JWT token or security context
        // Implementation depends on your security setup
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
        // This would be implemented in the service layer
        // For now, return a placeholder
        return VerificationStatistics.builder()
                .companyId(companyId)
                .totalUsers(0)
                .verifiedUsers(0)
                .pendingVerifications(0)
                .verificationRate(0.0)
                .build();
    }

    // Supporting DTOs
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