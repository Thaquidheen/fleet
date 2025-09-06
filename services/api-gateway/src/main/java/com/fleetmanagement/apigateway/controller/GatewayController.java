package com.fleetmanagement.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gateway Controller
 *
 * Provides information about the API Gateway and registered services
 *
 * @author Fleet Management Team
 */
@RestController
@RequestMapping("/api/gateway")
@Slf4j
public class GatewayController {

    @Autowired
    private DiscoveryClient discoveryClient;

    /**
     * Get API Gateway information
     */
    @GetMapping("/info")
    public Mono<ResponseEntity<Map<String, Object>>> getGatewayInfo() {
        log.debug("📊 Fetching API Gateway information");

        Map<String, Object> info = new HashMap<>();

        // Basic gateway information
        info.put("service", "API Gateway");
        info.put("description", "Single Entry Point for Fleet Management System APIs");
        info.put("version", "1.0.0");
        info.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        info.put("port", 8080);

        // Available routes
        Map<String, String> routes = new HashMap<>();
        routes.put("/api/auth/**", "User Service - Authentication");
        routes.put("/api/users/**", "User Service - User Management");
        routes.put("/api/companies/**", "Company Service - Multi-tenancy");
        routes.put("/api/vehicles/**", "Vehicle Service - Fleet Management");
        routes.put("/api/devices/**", "Device Service - GPS Devices");
        routes.put("/api/locations/**", "Location Service - Real-time Tracking");
        routes.put("/api/alerts/**", "Alert Service - Notifications");
        routes.put("/api/analytics/**", "Analytics Service - Reports");
        routes.put("/api/maintenance/**", "Maintenance Service - Scheduling");
        info.put("routes", routes);

        // Gateway features
        Map<String, Object> features = new HashMap<>();
        features.put("authentication", "JWT-based");
        features.put("rateLimiting", "Redis-based");
        features.put("circuitBreaker", "Resilience4j");
        features.put("serviceDiscovery", "Eureka");
        features.put("corsSupport", true);
        features.put("loadBalancing", true);
        info.put("features", features);

        log.debug("✅ Gateway information retrieved");
        return Mono.just(ResponseEntity.ok(info));
    }

    /**
     * Get registered services from Eureka
     */
    @GetMapping("/services")
    public Mono<ResponseEntity<Map<String, Object>>> getRegisteredServices() {
        log.debug("🔍 Fetching registered services from Eureka");

        Map<String, Object> response = new HashMap<>();

        try {
            // Get all registered services
            List<String> services = discoveryClient.getServices();

            Map<String, Object> serviceDetails = services.stream()
                    .collect(Collectors.toMap(
                            serviceName -> serviceName,
                            serviceName -> {
                                List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
                                return instances.stream()
                                        .map(instance -> Map.of(
                                                "instanceId", instance.getInstanceId(),
                                                "host", instance.getHost(),
                                                "port", instance.getPort(),
                                                "uri", instance.getUri().toString(),
                                                "secure", instance.isSecure(),
                                                "metadata", instance.getMetadata()
                                        ))
                                        .collect(Collectors.toList());
                            }
                    ));

            response.put("registeredServices", serviceDetails);
            response.put("totalServices", services.size());
            response.put("totalInstances", services.stream()
                    .mapToInt(service -> discoveryClient.getInstances(service).size())
                    .sum());
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        } catch (Exception e) {
            log.error("🚫 Failed to fetch registered services: {}", e.getMessage());
            response.put("error", "Failed to fetch services");
            response.put("message", e.getMessage());
        }

        log.debug("✅ Registered services information retrieved");
        return Mono.just(ResponseEntity.ok(response));
    }

    /**
     * Get API Gateway health status
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> getGatewayHealth() {
        Map<String, Object> health = new HashMap<>();

        health.put("status", "UP");
        health.put("service", "api-gateway");
        health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        health.put("description", "Fleet Management API Gateway is operational");

        // Check connectivity to Eureka
        try {
            List<String> services = discoveryClient.getServices();
            health.put("eurekaConnectivity", "UP");
            health.put("discoveredServices", services.size());
        } catch (Exception e) {
            health.put("eurekaConnectivity", "DOWN");
            health.put("eurekaError", e.getMessage());
        }

        return Mono.just(ResponseEntity.ok(health));
    }

    /**
     * Get system status overview
     */
    @GetMapping("/status")
    public Mono<ResponseEntity<Map<String, Object>>> getSystemStatus() {
        log.debug("📈 Fetching Fleet Management system status");

        Map<String, Object> status = new HashMap<>();

        // System information
        status.put("systemName", "Fleet Management System");
        status.put("gatewayStatus", "RUNNING");
        status.put("entryPoint", "http://localhost:8080");
        status.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // Expected vs available services
        List<String> expectedServices = List.of(
                "eureka-server", "api-gateway", "user-service", "company-service",
                "vehicle-service", "device-service", "location-service",
                "message-processor", "alert-service", "analytics-service", "maintenance-service"
        );

        List<String> availableServices = discoveryClient.getServices();

        status.put("expectedServices", expectedServices);
        status.put("availableServices", availableServices);
        status.put("missingServices", expectedServices.stream()
                .filter(service -> !availableServices.contains(service))
                .collect(Collectors.toList()));

        // Calculate system readiness
        double readiness = expectedServices.isEmpty() ? 100.0 :
                (double) availableServices.size() / expectedServices.size() * 100.0;
        status.put("systemReadiness", String.format("%.1f%%", readiness));

        // Overall system health
        String overallStatus = readiness >= 80 ? "HEALTHY" :
                readiness >= 50 ? "PARTIAL" : "DEGRADED";
        status.put("overallStatus", overallStatus);

        log.debug("✅ System status: {} ({}% ready)", overallStatus, String.format("%.1f", readiness));
        return Mono.just(ResponseEntity.ok(status));
    }
}