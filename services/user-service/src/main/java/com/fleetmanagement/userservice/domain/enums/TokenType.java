// TokenType.java (Enum)
package com.fleetmanagement.userservice.domain.enums;

public enum TokenType {
    EMAIL_VERIFICATION("Email Verification"),
    PASSWORD_RESET("Password Reset"),
    TWO_FACTOR_AUTH("Two Factor Authentication");

    private final String description;

    TokenType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
