package com.fleetmanagement.userservice.security;

import com.fleetmanagement.userservice.domain.entity.UserSession;
import com.fleetmanagement.userservice.repository.UserSessionRepository;
import com.fleetmanagement.userservice.service.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JWT Authentication Filter - Fixed to avoid circular dependency
 *
 * This filter validates JWT tokens and sets authentication context
 * without depending on AuthenticationService
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenService jwtTokenService;
    private final UserSessionRepository sessionRepository;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService, UserSessionRepository sessionRepository) {
        this.jwtTokenService = jwtTokenService;
        this.sessionRepository = sessionRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && jwtTokenService.validateAccessToken(jwt)) {

                // Basic session validation - check if session exists and is active
                if (isSessionValid(jwt)) {
                    // Extract user information from JWT
                    UUID userId = jwtTokenService.getUserIdFromToken(jwt);
                    String username = jwtTokenService.getUsernameFromToken(jwt);
                    String role = jwtTokenService.getRoleFromToken(jwt);

                    logger.debug("Valid JWT token found for user: {}", username);

                    // Create UserDetails
                    UserDetails userDetails = User.builder()
                            .username(userId.toString()) // Use userId as principal
                            .password("") // Password not needed for JWT authentication
                            .authorities("ROLE_" + role)
                            .build();

                    // Create authentication token
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    logger.debug("Successfully authenticated user: {} with role: {}", username, role);
                }
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
            // Clear security context on error
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Simple session validation - checks if session exists and is active
     */
    private boolean isSessionValid(String sessionToken) {
        try {
            UserSession session = sessionRepository.findBySessionToken(sessionToken)
                    .orElse(null);

            if (session == null) {
                return false;
            }

            // Check if session is active and not expired
            return session.isActive() && session.getExpiresAt().isAfter(LocalDateTime.now());

        } catch (Exception e) {
            logger.error("Error validating session: {}", e.getMessage());
            return false;
        }
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        return jwtTokenService.extractTokenFromHeader(bearerToken);
    }
}