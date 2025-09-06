package com.fleetmanagement.gateway.controller;

import com.fleetmanagement.apigateway.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;

/**
 * Auth Test Controller
 *
 * Provides endpoints for testing JWT authentication
 * This will be removed when User Service is implemented
 *
 * @author Fleet Management Team
 */
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthTestController {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Test login endpoint - generates JWT token
     */
    @PostMapping("/test-login")
    public Mono<ResponseEntity<Map<String, Object>>> testLogin(@RequestBody Map<String, String> credentials) {
        log.info("🔐 Test login attempt for user: {}", credentials.get("username"));

        String username = credentials.getOrDefault("username", "admin");
        String password = credentials.getOrDefault("password", "password");

        // Simple test validation (replace with real authentication)
        if ("admin".equals(username) && "password".equals(password)) {
            // Generate JWT token
            String token = jwtUtil.generateToken(
                    username,
                    "test-user-id-123",
                    "test-company-id-456",
                    "ADMIN",
                    "admin@fleetmanagement.com"
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Authentication successful");
            response.put("token", token);
            response.put("tokenType", "Bearer");
            response.put("expiresIn", 86400); // 24 hours
            response.put("user", Map.of(
                    "username", username,
                    "userId", "test-user-id-123",
                    "companyId", "test-company-id-456",
                    "role", "ADMIN",
                    "email", "admin@fleetmanagement.com"
            ));
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            log.info("✅ Test login successful for user: {}", username);
            return Mono.just(ResponseEntity.ok(response));
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Invalid credentials");
            response.put("error", "INVALID_CREDENTIALS");
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            log.warn("🚫 Test login failed for user: {}", username);
            return Mono.just(ResponseEntity.status(401).body(response));
        }
    }

    /**
     * Generate test token endpoint
     */
    @GetMapping("/test-token")
    public Mono<ResponseEntity<Map<String, Object>>> generateTestToken() {
        log.info("🎯 Generating test JWT token");

        String token = jwtUtil.generateTestToken();

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("tokenType", "Bearer");
        response.put("expiresIn", 86400);
        response.put("instructions", "Use this token in Authorization header: Bearer " + token);
        response.put("testUser", Map.of(
                "username", "admin",
                "userId", "test-user-id",
                "companyId", "test-company-id",
                "role", "ADMIN",
                "email", "admin@fleetmanagement.com"
        ));
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        log.info("✅ Test token generated successfully");
        return Mono.just(ResponseEntity.ok(response));
    }

    /**
     * Validate token endpoint
     */
    @PostMapping("/validate-token")
    public Mono<ResponseEntity<Map<String, Object>>> validateToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");

        if (token == null || token.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("error", "Token is required");
            return Mono.just(ResponseEntity.badRequest().body(response));
        }

        boolean isValid = jwtUtil.isTokenValid(token);
        Map<String, Object> response = new HashMap<>();
        response.put("valid", isValid);

        if (isValid) {
            Map<String, String> userDetails = jwtUtil.extractUserDetails(token);
            response.put("userDetails", userDetails);
            response.put("message", "Token is valid");
        } else {
            response.put("message", "Token is invalid or expired");
        }

        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return Mono.just(ResponseEntity.ok(response));
    }

    /**
     * Test protected endpoint
     */
    @GetMapping("/profile")
    public Mono<ResponseEntity<Map<String, Object>>> getProfile(@RequestHeader Map<String, String> headers) {
        log.info("👤 Accessing protected profile endpoint");

        Map<String, Object> response = new HashMap<>();
        response.put("message", "This is a protected endpoint");
        response.put("user", Map.of(
                "userId", headers.getOrDefault("X-User-ID", "unknown"),
                "username", headers.getOrDefault("X-Username", "unknown"),
                "companyId", headers.getOrDefault("X-Company-ID", "unknown"),
                "role", headers.getOrDefault("X-User-Role", "unknown"),
                "email", headers.getOrDefault("X-User-Email", "unknown")
        ));
        response.put("headers", headers);
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        log.info("✅ Profile accessed by user: {}", headers.getOrDefault("X-Username", "unknown"));
        return Mono.just(ResponseEntity.ok(response));
    }
}