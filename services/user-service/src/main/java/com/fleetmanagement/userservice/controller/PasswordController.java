package com.fleetmanagement.userservice.controller;

import com.fleetmanagement.userservice.dto.request.ChangePasswordRequest;
import com.fleetmanagement.userservice.dto.request.ResetPasswordRequest;
import com.fleetmanagement.userservice.service.PasswordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/password")
@Tag(name = "Password Management", description = "Password operations")
public class PasswordController {

    private static final Logger logger = LoggerFactory.getLogger(PasswordController.class);

    private final PasswordService passwordService;

    @Autowired
    public PasswordController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @PostMapping("/change")
    @Operation(summary = "Change password", description = "Change user password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        if (!request.isPasswordConfirmed()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Password confirmation does not match"));
        }

        UUID userId = UUID.fromString(authentication.getName());
        passwordService.changePassword(userId, request);

        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @PostMapping("/reset/initiate")
    @Operation(summary = "Initiate password reset", description = "Send password reset email")
    public ResponseEntity<Map<String, String>> initiatePasswordReset(@RequestParam String email) {
        passwordService.initiatePasswordReset(email);
        return ResponseEntity.ok(Map.of("message", "Password reset email sent"));
    }

    @PostMapping("/reset/confirm")
    @Operation(summary = "Confirm password reset", description = "Reset password with token")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        if (!request.isPasswordConfirmed()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Password confirmation does not match"));
        }

        passwordService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }
}