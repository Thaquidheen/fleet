package com.fleetmanagement.apigateway.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT Server Authentication Converter
 *
 * Converts HTTP requests to JWT Authentication objects
 *
 * @author Fleet Management Team
 */
@Slf4j
public class JwtServerAuthenticationConverter implements ServerAuthenticationConverter {

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("Authorization"))
                .filter(authHeader -> authHeader.startsWith("Bearer "))
                .map(authHeader -> authHeader.substring(7))
                .map(token -> {
                    log.debug("🔑 Converting JWT token to authentication");
                    // This will be handled by JwtAuthenticationManager
                    return new JwtAuthenticationToken(null, token, null, null, null);
                });
    }
}

/**
 * JWT Authentication Entry Point
 */
class JwtAuthenticationEntryPoint {
    public static Mono<Void> commence(ServerWebExchange exchange, Exception ex) {
        return Mono.fromRunnable(() -> {
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
        });
    }
}

/**
 * JWT Access Denied Handler
 */
class JwtAccessDeniedHandler {
    public static Mono<Void> handle(ServerWebExchange exchange, Exception denied) {
        return Mono.fromRunnable(() -> {
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.FORBIDDEN);
        });
    }
}