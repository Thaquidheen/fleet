package com.fleetmanagement.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * API Gateway Application
 *
 * Single entry point for all Fleet Management System APIs
 * Provides routing, security, rate limiting, and load balancing
 *
 * Features:
 * - Service discovery integration with Eureka
 * - JWT-based authentication
 * - Rate limiting with Redis
 * - Circuit breaker patterns
 * - CORS handling
 * - Request/response filtering
 * - Load balancing across service instances
 *
 * Entry Point: http://localhost:8080
 *
 * API Routes:
 * - /api/auth/**     → User Service (Authentication)
 * - /api/users/**    → User Service (User Management)
 * - /api/companies/** → Company Service (Multi-tenancy)
 * - /api/vehicles/** → Vehicle Service (Fleet Management)
 * - /api/devices/**  → Device Service (GPS Devices)
 * - /api/locations/** → Location Service (Real-time Tracking)
 * - /api/alerts/**   → Alert Service (Notifications)
 * - /api/analytics/** → Analytics Service (Reports)
 * - /api/maintenance/** → Maintenance Service (Scheduling)
 *
 * @author Fleet Management Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {
        "com.fleetmanagement.apigateway",
        "com.fleetmanagement.gateway"
})
public class ApiGatewayApplication {

    public static void main(String[] args) {
        // Set system properties for reactive web server
        System.setProperty("spring.main.web-application-type", "reactive");
        System.setProperty("java.awt.headless", "true");

        // Print startup banner
        printStartupBanner();

        // Start the API Gateway
        SpringApplication application = new SpringApplication(ApiGatewayApplication.class);

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n🛑 API Gateway is shutting down gracefully...");
        }));

        application.run(args);

        // Print success message
        printSuccessMessage();
    }

    /**
     * Define custom routes programmatically (in addition to configuration)
     * These routes supplement the ones defined in application.yml
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Health check routes (no authentication required)
                .route("health-check", r -> r
                        .path("/health", "/actuator/health")
                        .uri("forward:/actuator/health"))

                // API documentation routes
                .route("api-docs", r -> r
                        .path("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                        .uri("forward:/api-docs"))

                // Gateway info routes (protected)
                .route("gateway-info", r -> r
                        .path("/api/gateway/info", "/api/gateway/services", "/api/gateway/status")
                        .uri("forward:/api/gateway"))

                // Test routes for development
                .route("auth-test", r -> r
                        .path("/api/auth/test-login", "/api/auth/test-token", "/api/auth/validate-token", "/api/auth/profile")
                        .uri("forward:/api/auth"))

                .build();
    }

    /**
     * Print startup banner
     */
    private static void printStartupBanner() {
        System.out.println("""
            ╔══════════════════════════════════════════════════════════════╗
            ║                    🌐 API GATEWAY STARTING                   ║
            ║                                                              ║
            ║           Fleet Management System Entry Point               ║
            ║                                                              ║
            ║  Port: 8080                                                  ║
            ║  Entry Point: http://localhost:8080                         ║
            ║  Health: http://localhost:8080/actuator/health               ║
            ║                                                              ║
            ║  🔗 Routes to all Fleet Management services                  ║
            ║  🔐 JWT Authentication & Security                            ║
            ║  ⚡ Rate Limiting & Circuit Breaker                          ║
            ║  📊 Load Balancing & Service Discovery                       ║
            ║                                                              ║
            ╚══════════════════════════════════════════════════════════════╝
            """);
    }

    /**
     * Print success message after startup
     */
    private static void printSuccessMessage() {
        System.out.println("""
            
            ✅ API Gateway Started Successfully!
            
            🎯 Fleet Management System Entry Point Ready
            🌐 Gateway: http://localhost:8080
            🏥 Health Check: http://localhost:8080/actuator/health
            📈 Metrics: http://localhost:8080/actuator/prometheus
            
            🔗 Available API Routes:
               🔐 Authentication:    /api/auth/**
               👤 Users:            /api/users/**
               🏢 Companies:        /api/companies/**
               🚗 Vehicles:         /api/vehicles/**
               📱 Devices:          /api/devices/**
               📍 Locations:        /api/locations/**
               🚨 Alerts:           /api/alerts/**
               📊 Analytics:        /api/analytics/**
               🔧 Maintenance:      /api/maintenance/**
            
            🔐 Security: JWT-based authentication enabled
            ⚡ Features: Rate limiting, Circuit breaker, CORS
            📡 Discovery: Attempting to connect to Eureka Server (8761)
            
            🧪 Test Endpoints:
               POST /api/auth/test-login
               GET  /api/auth/test-token
               POST /api/auth/validate-token
               GET  /api/auth/profile
            
            """);
    }
}