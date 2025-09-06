package com.fleetmanagement.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;

/**
 * Fallback Controller
 *
 * Provides fallback responses when services are unavailable
 *
 * @author Fleet Management Team
 */
@RestController
@RequestMapping("/fallback")
@Slf4j
public class FallbackController {

    /**
     * Fallback for Authentication Service
     */
    @GetMapping("/auth")
    @PostMapping("/auth")
    public Mono<ResponseEntity<Map<String, Object>>> authFallback() {
        log.warn("🚨 Authentication service is unavailable - using fallback");

        Map<String, Object> response = createFallbackResponse(
                "Authentication service is temporarily unavailable",
                "AUTH_SERVICE_DOWN",
                "Please try again in a few moments"
        );

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    /**
     * Fallback for User Service
     */
    @GetMapping("/users")
    @PostMapping("/users")
    public Mono<ResponseEntity<Map<String, Object>>> usersFallback() {
        log.warn("🚨 User service is unavailable - using fallback");

        Map<String, Object> response = createFallbackResponse(
                "User management service is temporarily unavailable",
                "USER_SERVICE_DOWN",
                "User operations are currently disabled. Please try again later."
        );

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    /**
     * Fallback for Company Service
     */
    @GetMapping("/companies")
    @PostMapping("/companies")
    public Mono<ResponseEntity<Map<String, Object>>> companiesFallback() {
        log.warn("🚨 Company service is unavailable - using fallback");

        Map<String, Object> response = createFallbackResponse(
                "Company management service is temporarily unavailable",
                "COMPANY_SERVICE_DOWN",
                "Company operations are currently disabled. Please try again later."
        );

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    /**
     * Fallback for Vehicle Service
     */
    @GetMapping("/vehicles")
    @PostMapping("/vehicles")
    public Mono<ResponseEntity<Map<String, Object>>> vehiclesFallback() {
        log.warn("🚨 Vehicle service is unavailable - using fallback");

        Map<String, Object> response = createFallbackResponse(
                "Vehicle management service is temporarily unavailable",
                "VEHICLE_SERVICE_DOWN",
                "Vehicle operations are currently disabled. Fleet data may be outdated."
        );

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    /**
     * Fallback for Device Service
     */
    @GetMapping("/devices")
    @PostMapping("/devices")
    public Mono<ResponseEntity<Map<String, Object>>> devicesFallback() {
        log.warn("🚨 Device service is unavailable - using fallback");

        Map<String, Object> response = createFallbackResponse(
                "Device management service is temporarily unavailable",
                "DEVICE_SERVICE_DOWN",
                "Device operations are currently disabled. GPS tracking may be affected."
        );

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    /**
     * Fallback for Location Service
     */
    @GetMapping("/locations")
    @PostMapping("/locations")
    public Mono<ResponseEntity<Map<String, Object>>> locationsFallback() {
        log.warn("🚨 Location service is unavailable - using fallback");

        Map<String, Object> response = createFallbackResponse(
                "Location tracking service is temporarily unavailable",
                "LOCATION_SERVICE_DOWN",
                "Real-time tracking is currently disabled. Last known positions may be shown."
        );

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    /**
     * Fallback for Alert Service
     */
    @GetMapping("/alerts")
    @PostMapping("/alerts")
    public Mono<ResponseEntity<Map<String, Object>>> alertsFallback() {
        log.warn("🚨 Alert service is unavailable - using fallback");

        Map<String, Object> response = createFallbackResponse(
                "Alert management service is temporarily unavailable",
                "ALERT_SERVICE_DOWN",
                "Alert notifications may be delayed. Critical alerts are being queued."
        );

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    /**
     * Fallback for Analytics Service
     */
    @GetMapping("/analytics")
    @PostMapping("/analytics")
    public Mono<ResponseEntity<Map<String, Object>>> analyticsFallback() {
        log.warn("🚨 Analytics service is unavailable - using fallback");

        Map<String, Object> response = createFallbackResponse(
                "Analytics and reporting service is temporarily unavailable",
                "ANALYTICS_SERVICE_DOWN",
                "Report generation is currently disabled. Cached reports may be available."
        );

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    /**
     * Fallback for Maintenance Service
     */
    @GetMapping("/maintenance")
    @PostMapping("/maintenance")
    public Mono<ResponseEntity<Map<String, Object>>> maintenanceFallback() {
        log.warn("🚨 Maintenance service is unavailable - using fallback");

        Map<String, Object> response = createFallbackResponse(
                "Maintenance management service is temporarily unavailable",
                "MAINTENANCE_SERVICE_DOWN",
                "Maintenance scheduling is currently disabled. Urgent maintenance should be handled manually."
        );

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    /**
     * Generic fallback for any unhandled service
     */
    @GetMapping("/generic")
    @PostMapping("/generic")
    public Mono<ResponseEntity<Map<String, Object>>> genericFallback() {
        log.warn("🚨 Generic service fallback triggered");

        Map<String, Object> response = createFallbackResponse(
                "Service is temporarily unavailable",
                "SERVICE_DOWN",
                "The requested service is currently experiencing issues. Please try again later."
        );

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    /**
     * Create standardized fallback response
     */
    private Map<String, Object> createFallbackResponse(String message, String errorCode, String userMessage) {
        Map<String, Object> response = new HashMap<>();

        response.put("error", "Service Unavailable");
        response.put("message", message);
        response.put("errorCode", errorCode);
        response.put("userMessage", userMessage);
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("fallback", true);
        response.put("retryAfter", "30 seconds");

        // Add helpful information
        Map<String, Object> support = new HashMap<>();
        support.put("documentation", "https://docs.fleetmanagement.com/troubleshooting");
        support.put("status", "https://status.fleetmanagement.com");
        support.put("contact", "support@fleetmanagement.com");
        response.put("support", support);

        return response;
    }
}