package com.fleetmanagement.apigateway.security;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Authentication Manager
 *
 * Handles JWT token authentication for reactive security
 *
 * @author Fleet Management Team
 */
@Component
@Slf4j
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        log.debug("🔐 Authenticating JWT token");

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            try {
                // Extract user details from JWT token
                String username = jwtAuth.getPrincipal().toString();
                Object credentials = jwtAuth.getCredentials();

                // Get roles from token claims
                List<String> roles = jwtAuth.getRoles();
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        .collect(Collectors.toList());

                // Create authenticated token
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(username, credentials, authorities);

                // Add additional details
                authToken.setDetails(jwtAuth.getDetails());

                log.debug("✅ JWT authentication successful for user: {}", username);
                return Mono.just(authToken);

            } catch (Exception e) {
                log.error("🚫 JWT authentication failed: {}", e.getMessage());
                return Mono.empty();
            }
        }

        log.warn("🚫 Unsupported authentication type: {}", authentication.getClass());
        return Mono.empty();
    }
}

/**
 * Custom JWT Authentication Token
 */
class JwtAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private final List<String> roles;
    private final String companyId;
    private final String userId;

    public JwtAuthenticationToken(String username, String token, List<String> roles,
                                  String companyId, String userId) {
        super(username, token);
        this.roles = roles;
        this.companyId = companyId;
        this.userId = userId;
    }

    public List<String> getRoles() {
        return roles;
    }

    public String getCompanyId() {
        return companyId;
    }

    public String getUserId() {
        return userId;
    }
}