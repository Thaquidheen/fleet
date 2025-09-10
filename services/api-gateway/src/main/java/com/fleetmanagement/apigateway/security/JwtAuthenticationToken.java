package com.fleetmanagement.apigateway.security;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;


import java.util.List;




/**
 * Custom JWT Authentication Token
 */
public class JwtAuthenticationToken extends UsernamePasswordAuthenticationToken {

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