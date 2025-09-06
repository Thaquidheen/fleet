package com.fleetmanagement.userservice.controller;

import com.fleetmanagement.userservice.dto.request.ChangePasswordRequest;
import com.fleetmanagement.userservice.dto.request.LoginRequest;
import com.fleetmanagement.userservice.dto.request.RefreshTokenRequest;
import com.fleetmanagement.userservice.dto.response.AuthenticationResponse;
import com.fleetmanagement.userservice.service.AuthenticationService;
import com.fleetmanagement.userservice.service.JwtTokenService;
import com.fleetmanagement.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthenticationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final JwtTokenService jwtTokenService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService,
                                    UserService userService,
                                    JwtTokenService jwtTokenService) {
        this.authenticationService = authenticationService;
        this.userService = userService;
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT tokens")
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody LoginRequest request,
                                                        HttpServletRequest httpRequest) {
        logger.info("Login request for: {}", request.getUsernameOrEmail());

        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthenticationResponse response = authenticationService.authenticate(request, ipAddress, userAgent);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Generate new access token using refresh token")
    @ApiResponse(responseCode = "200", description = "Token refreshed successfully")
    @ApiResponse(responseCode = "401", description = "Invalid refresh token")
    public ResponseEntity<AuthenticationResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        logger.info("Token refresh request");

        AuthenticationResponse response = authenticationService.refreshToken(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Invalidate current session")
    @ApiResponse(responseCode = "200", description = "Logout successful")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("Authorization") String authHeader,
                                                      @RequestParam(defaultValue = "false") boolean logoutAllSessions) {
        logger.info("Logout request");

        String token = jwtTokenService.extractTokenFromHeader(authHeader);
        authenticationService.logout(token, logoutAllSessions);

        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Change user password")
    @ApiResponse(responseCode = "200", description = "Password changed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                              Authentication authentication) {
        logger.info("Change password request for user: {}", authentication.getName());

        // Validate password confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "New password and confirmation do not match"));
        }

        UUID userId = UUID.fromString(authentication.getName());
        authenticationService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());

        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Verify user email with verification token")
    @ApiResponse(responseCode = "200", description = "Email verified successfully")
    @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {
        logger.info("Email verification request");

        userService.verifyEmail(token);

        return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend email verification", description = "Resend email verification link")
    @ApiResponse(responseCode = "200", description = "Verification email sent")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<Map<String, String>> resendEmailVerification(@RequestParam String email) {
        logger.info("Resend verification request for: {}", email);

        userService.resendEmailVerification(email);

        return ResponseEntity.ok(Map.of("message", "Verification email sent"));
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate session", description = "Validate current session token")
    @ApiResponse(responseCode = "200", description = "Session is valid")
    @ApiResponse(responseCode = "401", description = "Invalid session")
    public ResponseEntity<Map<String, Object>> validateSession(@RequestHeader("Authorization") String authHeader) {
        String token = jwtTokenService.extractTokenFromHeader(authHeader);
        boolean isValid = authenticationService.validateSession(token);

        if (isValid) {
            // Extract user info from token
            UUID userId = jwtTokenService.getUserIdFromToken(token);
            String username = jwtTokenService.getUsernameFromToken(token);
            String role = jwtTokenService.getRoleFromToken(token);
            UUID companyId = jwtTokenService.getCompanyIdFromToken(token);

            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "userId", userId,
                    "username", username,
                    "role", role,
                    "companyId", companyId != null ? companyId : ""
            ));
        } else {
            return ResponseEntity.status(401)
                    .body(Map.of("valid", false, "message", "Invalid session"));
        }
    }

    @PostMapping("/force-password-change/{userId}")
    @Operation(summary = "Force password change", description = "Force user to change password on next login")
    @ApiResponse(responseCode = "200", description = "Password change forced")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Map<String, String>> forcePasswordChange(@PathVariable UUID userId) {
        logger.info("Force password change for user: {}", userId);

        authenticationService.forcePasswordChange(userId);

        return ResponseEntity.ok(Map.of("message", "Password change forced for user"));
    }

    // Helper method to extract client IP address
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
