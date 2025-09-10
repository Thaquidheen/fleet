package com.fleetmanagement.companyservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * FIXED JwtTokenService - Compatible with JJWT 0.11.x
 *
 * Changed from verifyWith() to setSigningKey() method which is supported in 0.11.x
 */
@Service
public class JwtTokenService {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenService.class);

    @Value("${jwt.secret:fleet-management-secret-key-for-jwt-token-signing-should-be-very-long-and-secure-and-is-at-least-64-bytes}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationTime;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean validateAccessToken(String token) {
        try {
            // Fixed: Use setSigningKey() instead of verifyWith() for JJWT 0.11.x compatibility
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            logger.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public UUID getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return UUID.fromString(claims.getSubject());
    }

    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("username", String.class);
    }

    public String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("role", String.class);
    }

    public UUID getCompanyIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String companyIdStr = claims.get("companyId", String.class);
        return companyIdStr != null ? UUID.fromString(companyIdStr) : null;
    }

    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private Claims getClaimsFromToken(String token) {
        // Fixed: Use setSigningKey() instead of verifyWith() for JJWT 0.11.x compatibility
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Additional utility methods for token validation

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration().before(new java.util.Date());
        } catch (Exception e) {
            logger.debug("Error checking token expiration: {}", e.getMessage());
            return true; // Consider expired if we can't parse
        }
    }

    public boolean isValidTokenFormat(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        // JWT should have 3 parts separated by dots
        String[] parts = token.split("\\.");
        return parts.length == 3;
    }

    public String getIssuerFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getIssuer();
        } catch (Exception e) {
            logger.debug("Error getting issuer from token: {}", e.getMessage());
            return null;
        }
    }

    public java.util.Date getExpirationFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration();
        } catch (Exception e) {
            logger.debug("Error getting expiration from token: {}", e.getMessage());
            return null;
        }
    }
}