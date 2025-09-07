package com.fleetmanagement.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;


/**
 * API Gateway Configuration
 *
 * Configures CORS, rate limiting, and other gateway features
 * for the Fleet Management System
 *
 * @author Fleet Management Team
 */
@Configuration
@Slf4j
public class GatewayConfig {

    /**
     * CORS Configuration for Frontend Applications
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        log.info("🌐 Configuring CORS for API Gateway");

        CorsConfiguration corsConfig = new CorsConfiguration();

        // Allow specific origins (update for production)
        corsConfig.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:3000",    // React Admin Dashboard
                "http://localhost:3001",    // React Native development
                "http://localhost:4200",    // Angular (if used)
                "http://localhost:8080",    // Gateway itself
                "https://*.fleetmanagement.com"  // Production domains
        ));

        // Allow specific headers
        corsConfig.setAllowedHeaders(Arrays.asList(
                "Origin",
                "Content-Type",
                "Accept",
                "Authorization",
                "X-Requested-With",
                "X-Company-ID",  // Custom header for multi-tenancy
                "Cache-Control"
        ));

        // Allow specific HTTP methods
        corsConfig.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // Allow credentials (cookies, authorization headers)
        corsConfig.setAllowCredentials(true);

        // Cache preflight response for 1 hour
        corsConfig.setMaxAge(3600L);

        // Expose specific headers to frontend
        corsConfig.setExposedHeaders(Arrays.asList(
                "X-Total-Count",
                "X-Page-Number",
                "X-Page-Size",
                "X-Rate-Limit-Remaining"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        log.info("✅ CORS configuration applied to API Gateway");
        return new CorsWebFilter(source);
    }

    /**
     * Rate Limiting Key Resolver - Rate limit by User ID
     */
    @Bean
    @Primary
    public KeyResolver userKeyResolver() {
        log.info("⚡ Configuring rate limiting by User ID");

        return exchange -> {
            // Try to get user ID from JWT token (will be implemented in AuthFilter)
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");

            if (userId != null && !userId.isEmpty()) {
                return Mono.just("user:" + userId);
            }

            // Fallback to IP address if no user ID
            String clientIP = getClientIP(exchange);
            return Mono.just("ip:" + clientIP);
        };
    }

    /**
     * Rate Limiting Key Resolver - Rate limit by Company ID
     */
    @Bean
    public KeyResolver companyKeyResolver() {
        log.info("🏢 Configuring rate limiting by Company ID");

        return exchange -> {
            // Try to get company ID from custom header or JWT
            String companyId = exchange.getRequest().getHeaders().getFirst("X-Company-ID");

            if (companyId != null && !companyId.isEmpty()) {
                return Mono.just("company:" + companyId);
            }

            // Fallback to user-based limiting
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");
            if (userId != null && !userId.isEmpty()) {
                return Mono.just("user:" + userId);
            }

            // Final fallback to IP
            String clientIP = getClientIP(exchange);
            return Mono.just("ip:" + clientIP);
        };
    }

    /**
     * Rate Limiting Key Resolver - Rate limit by IP Address
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        log.info("🌐 Configuring rate limiting by IP Address");

        return exchange -> {
            String clientIP = getClientIP(exchange);
            return Mono.just("ip:" + clientIP);
        };
    }

    /**
     * Rate Limiting Key Resolver - Rate limit by API endpoint
     */
    @Bean
    public KeyResolver endpointKeyResolver() {
        log.info("🎯 Configuring rate limiting by API endpoint");

        return exchange -> {
            String path = exchange.getRequest().getPath().value();
            String method = exchange.getRequest().getMethod().name();

            // Create a key based on HTTP method and path pattern
            String endpointKey = method + ":" + normalizePathForRateLimit(path);

            return Mono.just("endpoint:" + endpointKey);
        };
    }

    /**
     * Redis Rate Limiter Configuration
     */
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(10, 20, 1);
    }

    /**
     * Helper method to extract real client IP
     */
    private String getClientIP(org.springframework.web.server.ServerWebExchange exchange) {
        String clientIP = "unknown";

        // Try to get real IP from headers (for load balancers/proxies)
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        String xRealIP = exchange.getRequest().getHeaders().getFirst("X-Real-IP");

        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, get the first one
            clientIP = xForwardedFor.split(",")[0].trim();
        } else if (xRealIP != null && !xRealIP.isEmpty()) {
            clientIP = xRealIP;
        } else if (exchange.getRequest().getRemoteAddress() != null) {
            clientIP = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }

        return clientIP;
    }

    /**
     * Helper method to normalize paths for rate limiting
     */
    private String normalizePathForRateLimit(String path) {
        // Replace IDs and UUIDs with placeholders for rate limiting
        return path
                .replaceAll("/\\d+", "/{id}")                    // Replace numeric IDs
                .replaceAll("/[a-fA-F0-9-]{36}", "/{uuid}")      // Replace UUIDs
                .replaceAll("/[a-fA-F0-9-]{32}", "/{id}")        // Replace 32-char IDs
                .toLowerCase();
    }
}