// JwtTokenService.java - For JJWT 0.12.3
package com.fleetmanagement.userservice.service;

import com.fleetmanagement.userservice.domain.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtTokenService {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenService.class);

    private final SecretKey secretKey;
    private final long jwtExpiration;
    private final long refreshExpiration;
    private final String issuer;
    private final String audience;

    public JwtTokenService(@Value("${jwt.secret}") String secret,
                           @Value("${jwt.expiration}") long jwtExpiration,
                           @Value("${jwt.refresh-expiration}") long refreshExpiration,
                           @Value("${jwt.issuer}") String issuer,
                           @Value("${jwt.audience}") String audience) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.jwtExpiration = jwtExpiration;
        this.refreshExpiration = refreshExpiration;
        this.issuer = issuer;
        this.audience = audience;
    }

    /**
     * Generate JWT access token for authenticated user
     */
    public String generateAccessToken(User user, String sessionId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId().toString());
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole().name());
        claims.put("companyId", user.getCompanyId() != null ? user.getCompanyId().toString() : null);
        claims.put("sessionId", sessionId);
        claims.put("emailVerified", user.getEmailVerified());
        claims.put("tokenType", "ACCESS");

        return Jwts.builder()
                .claims(claims)
                .subject(user.getId().toString())
                .issuer(issuer)
                .audience().add(audience).and()
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Generate JWT refresh token
     */
    public String generateRefreshToken(User user, String sessionId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId().toString());
        claims.put("sessionId", sessionId);
        claims.put("tokenType", "REFRESH");

        return Jwts.builder()
                .claims(claims)
                .subject(user.getId().toString())
                .issuer(issuer)
                .audience().add(audience).and()
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Extract user ID from JWT token
     */
    public UUID getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return UUID.fromString(claims.getSubject());
    }

    /**
     * Extract username from JWT token
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("username", String.class);
    }

    /**
     * Extract session ID from JWT token
     */
    public String getSessionIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("sessionId", String.class);
    }

    /**
     * Extract user role from JWT token
     */
    public String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("role", String.class);
    }

    /**
     * Extract company ID from JWT token
     */
    public UUID getCompanyIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String companyIdStr = claims.get("companyId", String.class);
        return companyIdStr != null ? UUID.fromString(companyIdStr) : null;
    }

    /**
     * Extract token type from JWT token
     */
    public String getTokenTypeFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("tokenType", String.class);
    }

    /**
     * Extract expiration date from JWT token
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }

    /**
     * Extract issued date from JWT token
     */
    public Date getIssuedDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getIssuedAt();
    }

    /**
     * Check if JWT token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            logger.warn("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .requireIssuer(issuer)
                    .requireAudience(audience)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Validate token and check if it's an access token
     */
    public boolean validateAccessToken(String token) {
        if (!validateToken(token)) {
            return false;
        }
        try {
            String tokenType = getTokenTypeFromToken(token);
            return "ACCESS".equals(tokenType);
        } catch (Exception e) {
            logger.error("Error validating access token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate token and check if it's a refresh token
     */
    public boolean validateRefreshToken(String token) {
        if (!validateToken(token)) {
            return false;
        }
        try {
            String tokenType = getTokenTypeFromToken(token);
            return "REFRESH".equals(tokenType);
        } catch (Exception e) {
            logger.error("Error validating refresh token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Generate new access token from refresh token
     */
    public String refreshAccessToken(String refreshToken, User user) {
        if (!validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String sessionId = getSessionIdFromToken(refreshToken);
        return generateAccessToken(user, sessionId);
    }

    /**
     * Extract all claims from JWT token
     */
    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            logger.error("Error extracting claims from token: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid token", e);
        }
    }

    /**
     * Get remaining time before token expires (in seconds)
     */
    public long getTokenRemainingTime(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            Date now = new Date();
            long remainingMs = expiration.getTime() - now.getTime();
            return Math.max(0, remainingMs / 1000);
        } catch (Exception e) {
            logger.error("Error calculating token remaining time: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Convert Date to LocalDateTime
     */
    public LocalDateTime dateToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Convert LocalDateTime to Date
     */
    public Date localDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Create token info map for response
     */
    public Map<String, Object> createTokenInfo(String accessToken, String refreshToken) {
        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("accessToken", accessToken);
        tokenInfo.put("refreshToken", refreshToken);
        tokenInfo.put("tokenType", "Bearer");
        tokenInfo.put("expiresIn", jwtExpiration / 1000); // in seconds
        tokenInfo.put("refreshExpiresIn", refreshExpiration / 1000); // in seconds
        tokenInfo.put("issuedAt", new Date());
        return tokenInfo;
    }

    /**
     * Extract token from Authorization header
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Check if token needs refresh (expires within next 5 minutes)
     */
    public boolean shouldRefreshToken(String token) {
        try {
            long remainingTime = getTokenRemainingTime(token);
            return remainingTime <= 300; // 5 minutes
        } catch (Exception e) {
            logger.error("Error checking if token should be refreshed: {}", e.getMessage());
            return true;
        }
    }
}