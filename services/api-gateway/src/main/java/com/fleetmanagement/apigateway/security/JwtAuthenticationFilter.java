package com.fleetmanagement.apigateway.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * JWT Authentication Filter for API Gateway
 *
 * Validates JWT tokens and extracts user information for downstream services
 *
 * @author Fleet Management Team
 */
@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    // Public endpoints that don't require authentication
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/auth/verify-email",
            "/api/auth/test-login",
            "/api/auth/test-token",
            "/api/auth/validate-token",
            "/actuator",
            "/health",
            "/api-docs",
            "/swagger-ui",
            "/api/gateway/health",
            "/api/gateway/info",
            "/fallback"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String method = request.getMethod().name();

        log.debug("🔍 Processing request: {} {}", method, path);

        // Skip authentication for public endpoints
        if (isPublicEndpoint(path)) {
            log.debug("✅ Public endpoint, skipping authentication: {}", path);
            return addBasicHeaders(exchange, chain);
        }

        // Extract JWT token from request
        String token = extractToken(request);

        if (token == null || token.isEmpty()) {
            log.warn("🚫 No JWT token found for protected endpoint: {}", path);
            return onError(exchange, "Missing authentication token", HttpStatus.UNAUTHORIZED);
        }

        // Validate JWT token
        if (!jwtUtil.isTokenValid(token)) {
            log.warn("🚫 Invalid JWT token for endpoint: {}", path);
            return onError(exchange, "Invalid authentication token", HttpStatus.UNAUTHORIZED);
        }

        try {
            // Extract user information from token
            Map<String, String> userDetails = jwtUtil.extractUserDetails(token);

            String userId = userDetails.get("userId");
            String username = userDetails.get("username");
            String companyId = userDetails.get("companyId");
            String role = userDetails.get("role");
            String email = userDetails.get("email");

            log.debug("✅ Authenticated user: {} (ID: {}, Company: {}, Role: {})",
                    username, userId, companyId, role);

            // Validate required fields
            if (userId == null || username == null) {
                log.warn("🚫 Invalid token data - missing required fields");
                return onError(exchange, "Invalid token data", HttpStatus.UNAUTHORIZED);
            }

            // Add user information to request headers for downstream services
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-ID", userId)
                    .header("X-Username", username)
                    .header("X-Company-ID", companyId != null ? companyId : "")
                    .header("X-User-Role", role != null ? role : "VIEWER")
                    .header("X-User-Email", email != null ? email : "")
                    .header("X-Authenticated", "true")
                    .header("X-Gateway", "fleet-management-gateway")
                    .header("X-Timestamp", String.valueOf(System.currentTimeMillis()))
                    .header("X-Request-ID", java.util.UUID.randomUUID().toString())
                    .header("X-Original-Token", token) // Pass original token for service-to-service calls
                    .build();

            // Continue with modified request
            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            log.error("🔥 JWT token processing failed for {}: {}", path, e.getMessage());
            return onError(exchange, "Authentication processing failed", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Add basic headers for public endpoints
     */
    private Mono<Void> addBasicHeaders(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-Gateway", "fleet-management-gateway")
                .header("X-Timestamp", String.valueOf(System.currentTimeMillis()))
                .header("X-Request-ID", java.util.UUID.randomUUID().toString())
                .header("X-Public-Endpoint", "true")
                .header("X-Authenticated", "false")
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    /**
     * Extract JWT token from Authorization header or query parameter
     */
    private String extractToken(ServerHttpRequest request) {
        // Try Authorization header first
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Remove "Bearer " prefix
        }

        // Try to get token from query parameter (for WebSocket connections or special cases)
        String tokenParam = request.getQueryParams().getFirst("token");
        if (tokenParam != null && !tokenParam.isEmpty()) {
            return tokenParam;
        }

        // Try custom header (for mobile apps or special scenarios)
        String customToken = request.getHeaders().getFirst("X-Auth-Token");
        if (customToken != null && !customToken.isEmpty()) {
            return customToken;
        }

        return null;
    }

    /**
     * Check if the endpoint is public (doesn't require authentication)
     */
    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream()
                .anyMatch(endpoint -> path.startsWith(endpoint));
    }

    /**
     * Handle authentication errors with detailed response
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");
        response.getHeaders().add("X-Error-Source", "API-Gateway");

        String errorResponse = String.format(
                """
                {
                    "error": "%s",
                    "message": "%s",
                    "timestamp": "%s",
                    "path": "%s",
                    "status": %d,
                    "trace": "%s"
                }
                """,
                status.getReasonPhrase(),
                message,
                java.time.Instant.now().toString(),
                exchange.getRequest().getPath().value(),
                status.value(),
                java.util.UUID.randomUUID().toString()
        );

        org.springframework.core.io.buffer.DataBuffer buffer =
                response.bufferFactory().wrap(errorResponse.getBytes());

        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // Execute before other filters but after CORS
    }
}