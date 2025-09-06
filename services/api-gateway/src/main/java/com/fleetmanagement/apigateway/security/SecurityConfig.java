package com.fleetmanagement.apigateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import lombok.extern.slf4j.Slf4j;

/**
 * Security Configuration for API Gateway
 *
 * Configures JWT authentication and authorization for all routes
 * passing through the API Gateway
 *
 * @author Fleet Management Team
 */
@Configuration
@EnableWebFluxSecurity
@Slf4j
public class SecurityConfig {

    /**
     * Configure security filter chain for reactive web
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        log.info("🔐 Configuring API Gateway Security with JWT Authentication");

        return http
                // Disable CSRF for API Gateway (stateless)
                .csrf(csrf -> csrf.disable())

                // Disable form login (we use JWT)
                .formLogin(form -> form.disable())

                // Disable HTTP Basic (we use JWT)
                .httpBasic(basic -> basic.disable())

                // Configure authorization rules
                .authorizeExchange(exchanges -> exchanges
                        // Public endpoints (no authentication required)
                        .pathMatchers(
                                "/health",
                                "/actuator/**",
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/api/auth/verify-email",
                                "/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/webjars/**",
                                "/favicon.ico",
                                "/api/gateway/health", // Allow gateway health checks
                                "/fallback/**" ,// Allow fallback endpoints
                                "/api/gateway/info"
                        ).permitAll()

                        // Admin-only endpoints
                        .pathMatchers(
                                "/api/admin/**",
                                "/api/companies/*/settings",
                                "/api/users/*/admin",
                                "/api/system/**",

                                "/api/gateway/services",
                                "/api/gateway/status"
                        ).authenticated()

                        // Manager-level endpoints
                        .pathMatchers(
                                "/api/companies/*/users",
                                "/api/vehicles/*/assign",
                                "/api/alerts/*/manage",
                                "/api/reports/company/*"
                        ).authenticated()

                        // Driver-specific endpoints
                        .pathMatchers(
                                "/api/vehicles/*/status",
                                "/api/locations/my",
                                "/api/trips/my"
                        ).authenticated()

                        // General authenticated endpoints
                        .pathMatchers(
                                "/api/users/profile",
                                "/api/vehicles/*/view",
                                "/api/locations/view",
                                "/api/alerts/view"
                        ).authenticated()

                        // All other API endpoints require authentication
                        .pathMatchers("/api/**").authenticated()

                        // Allow all other requests (static resources, etc.)
                        .anyExchange().permitAll()
                )

                // Exception handling
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((exchange, ex) -> {
                            log.warn("🚫 Authentication failed for: {}",
                                    exchange.getRequest().getPath().value());

                            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                            exchange.getResponse().getHeaders().add("Content-Type", "application/json");

                            String errorResponse = "{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}";
                            org.springframework.core.io.buffer.DataBuffer buffer =
                                    exchange.getResponse().bufferFactory().wrap(errorResponse.getBytes());

                            return exchange.getResponse().writeWith(reactor.core.publisher.Mono.just(buffer));
                        })
                        .accessDeniedHandler((exchange, denied) -> {
                            log.warn("⛔ Access denied for: {} to path: {}",
                                    "user",
                                    exchange.getRequest().getPath().value());

                            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.FORBIDDEN);
                            exchange.getResponse().getHeaders().add("Content-Type", "application/json");

                            String errorResponse = "{\"error\":\"Forbidden\",\"message\":\"Access denied\"}";
                            org.springframework.core.io.buffer.DataBuffer buffer =
                                    exchange.getResponse().bufferFactory().wrap(errorResponse.getBytes());

                            return exchange.getResponse().writeWith(reactor.core.publisher.Mono.just(buffer));
                        })
                )

                .build();
    }
}