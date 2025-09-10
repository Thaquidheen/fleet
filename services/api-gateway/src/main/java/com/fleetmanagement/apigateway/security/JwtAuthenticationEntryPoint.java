
package com.fleetmanagement.apigateway.security;


import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


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