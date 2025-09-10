
package com.fleetmanagement.apigateway.security;


import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;



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