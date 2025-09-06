package com.fleetmanagement.userservice.security;

import com.fleetmanagement.userservice.service.AuthenticationService;
import com.fleetmanagement.userservice.service.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenService jwtTokenService;
    private final AuthenticationService authenticationService;

    @Autowired
    public JwtAuthenticationFilter(JwtTokenService jwtTokenService,
                                   AuthenticationService authenticationService) {
        this.jwtTokenService = jwtTokenService;
        this.authenticationService = authenticationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && jwtTokenService.validateAccessToken(jwt)) {

                // Validate session in database
                if (authenticationService.validateSession(jwt)) {
                    UUID userId = jwtTokenService.getUserIdFromToken(jwt);
                    String username = jwtTokenService.getUsernameFromToken(jwt);
                    String role = jwtTokenService.getRoleFromToken(jwt);

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
                }
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        return jwtTokenService.extractTokenFromHeader(bearerToken);
    }
}