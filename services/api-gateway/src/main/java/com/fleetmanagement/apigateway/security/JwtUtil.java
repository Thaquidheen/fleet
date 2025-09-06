package com.fleetmanagement.apigateway.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * A robust utility class for handling JSON Web Tokens (JWTs).
 *
 * This class provides methods for generating, parsing, and validating JWTs
 * using the JJWT library (version 0.12.x). It is designed to be used
 * as a Spring Component for easy injection into services and security filters.
 *
 * Key Features:
 * - Token Generation with custom claims.
 * - Token Validation with detailed logging for different failure reasons.
 * - Extraction of individual claims (e.g., username, userId) from a token.
 * - Use of HMAC-SHA512 for secure signing.
 * - Configuration driven by application properties for secret and expiration.
 *
 * @author Fleet Management Team (Refined by AI)
 */
@Component
@Slf4j
public class JwtUtil {

    // IMPORTANT: This default secret is for development ONLY.
    // In production, you MUST override this value with a strong, securely-generated
    // secret key stored in a secure configuration service or environment variable.
    // For the HS512 algorithm used here, a secret key should be at least 512 bits (64 bytes) long.
    @Value("${jwt.secret:fleet-management-secret-key-for-jwt-token-signing-should-be-very-long-and-secure-and-is-at-least-64-bytes}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400}") // Default: 24 hours in seconds
    private Long jwtExpiration;

    /**
     * Creates the signing key from the jwtSecret string.
     * This method is called internally for token creation and validation.
     * @return a SecretKey instance
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = this.jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates an access token for a user with specified details.
     *
     * @param username The subject of the token.
     * @param userId The unique identifier for the user.
     * @param companyId The identifier for the user's company.
     * @param role The role of the user (e.g., "ADMIN", "USER").
     * @param email The user's email address.
     * @return A signed JWT string.
     */
    public String generateToken(String username, String userId, String companyId, String role, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("companyId", companyId);
        claims.put("role", role);
        claims.put("email", email);
        claims.put("tokenType", "ACCESS");

        return createToken(claims, username);
    }

    /**
     * Private helper method to construct the JWT.
     */
    private String createToken(Map<String, Object> claims, String subject) {
        final Date now = new Date();
        final Date expiryDate = new Date(now.getTime() + this.jwtExpiration * 1000);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .issuer("fleet-management-system")
                .signWith(getSigningKey()) // JJWT 0.12.x infers algorithm from key type
                .compact();
    }

    /**
     * Validates a JWT token's signature and expiration.
     * This method is useful for a quick check, for example in a gateway filter.
     * It logs specific errors for debugging purposes.
     *
     * @param token The JWT token string.
     * @return {@code true} if the token is valid, {@code false} otherwise.
     */
    public boolean isTokenValid(String token) {
        try {
            // Jwts.parser() is the new builder factory method in 0.12.x
            // verifyWith() sets the key, and parseSignedClaims() replaces the deprecated parseClaimsJws()
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT Token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT Token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT Token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty or invalid: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Extracts a specific claim from a token using a claims resolver function.
     *
     * @param token The JWT token string.
     * @param claimsResolver A function that takes Claims and returns the desired value.
     * @param <T> The type of the claim to be returned.
     * @return The extracted claim.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parses the token to extract all claims. This is the central parsing logic.
     * Note: This method will throw an exception if the token is invalid (expired, malformed, etc.).
     *
     * @param token The JWT token string.
     * @return The Claims object containing all data from the token payload.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    public String extractCompanyId(String token) {
        return extractClaim(token, claims -> claims.get("companyId", String.class));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Checks if the token is expired. Note that the parsing process itself
     * will throw an ExpiredJwtException, but this method provides an explicit check.
     *
     * @param token The JWT token string.
     * @return {@code true} if the token's expiration date is in the past.
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            // If parsing fails because the token is expired, then it is indeed expired.
            return true;
        }
    }

    /**
     * Extracts all user details from a token into a Map.
     * This provides a safe way to get all details without multiple parsing operations.
     *
     * @param token the JWT token
     * @return A map containing user details, or an empty map if parsing fails.
     */
    public Map<String, String> extractUserDetails(String token) {
        Map<String, String> userDetails = new HashMap<>();
        try {
            Claims claims = extractAllClaims(token);
            userDetails.put("username", claims.getSubject());
            userDetails.put("userId", claims.get("userId", String.class));
            userDetails.put("companyId", claims.get("companyId", String.class));
            userDetails.put("role", claims.get("role", String.class));
            userDetails.put("email", claims.get("email", String.class));
        } catch (Exception e) {
            log.error("Could not extract user details from token: {}", e.getMessage());
        }
        return userDetails;
    }

    /**
     * Generates a pre-configured test token for development and testing purposes.
     *
     * @return A valid JWT for a test user.
     */
    public String generateTestToken() {
        return generateToken(
                "test-admin",
                "a1b2c3d4-test-user-id-e5f6",
                "e5f6a1b2-test-company-id-c3d4",
                "ROLE_ADMIN",
                "admin@fleetmanagement.com"
        );
    }
}
