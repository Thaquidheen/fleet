// PasswordService.java
package com.fleetmanagement.userservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class PasswordService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordService.class);

    @Value("${app.security.password.min-length:8}")
    private int minLength;

    @Value("${app.security.password.require-uppercase:true}")
    private boolean requireUppercase;

    @Value("${app.security.password.require-lowercase:true}")
    private boolean requireLowercase;

    @Value("${app.security.password.require-digits:true}")
    private boolean requireDigits;

    @Value("${app.security.password.require-special-chars:true}")
    private boolean requireSpecialChars;

    // Password strength patterns
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*[0-9].*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

    // Common weak passwords to reject
    private static final String[] COMMON_WEAK_PASSWORDS = {
            "password", "123456", "123456789", "12345678", "12345", "1234567", "admin", "qwerty",
            "abc123", "password123", "admin123", "welcome", "login", "letmein", "master", "monkey",
            "dragon", "1234567890", "football", "iloveyou", "princess", "welcome123", "sunshine"
    };

    // Character sets for password generation
    private static final String UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGIT_CHARS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*()_+-=[]{}|;:,.<>?";

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Validate password against all configured rules
     */
    public boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            logger.debug("Password validation failed: password is null or empty");
            return false;
        }

        // Check minimum length
        if (password.length() < minLength) {
            logger.debug("Password validation failed: length {} is less than required {}",
                    password.length(), minLength);
            return false;
        }

        // Check maximum length (security best practice)
        if (password.length() > 128) {
            logger.debug("Password validation failed: length {} exceeds maximum 128", password.length());
            return false;
        }

        // Check for common weak passwords
        if (isCommonWeakPassword(password)) {
            logger.debug("Password validation failed: password is too common");
            return false;
        }

        // Check uppercase requirement
        if (requireUppercase && !UPPERCASE_PATTERN.matcher(password).matches()) {
            logger.debug("Password validation failed: missing uppercase letter");
            return false;
        }

        // Check lowercase requirement
        if (requireLowercase && !LOWERCASE_PATTERN.matcher(password).matches()) {
            logger.debug("Password validation failed: missing lowercase letter");
            return false;
        }

        // Check digit requirement
        if (requireDigits && !DIGIT_PATTERN.matcher(password).matches()) {
            logger.debug("Password validation failed: missing digit");
            return false;
        }

        // Check special character requirement
        if (requireSpecialChars && !SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            logger.debug("Password validation failed: missing special character");
            return false;
        }

        // Check for sequential characters (like "123" or "abc")
        if (hasSequentialCharacters(password)) {
            logger.debug("Password validation failed: contains sequential characters");
            return false;
        }

        // Check for repeated characters (like "aaa" or "111")
        if (hasRepeatedCharacters(password)) {
            logger.debug("Password validation failed: contains repeated characters");
            return false;
        }

        logger.debug("Password validation passed");
        return true;
    }

    /**
     * Get detailed password requirements as a user-friendly string
     */
    public String getPasswordRequirements() {
        StringBuilder requirements = new StringBuilder();
        requirements.append("Password must be at least ").append(minLength).append(" characters long");

        List<String> rules = new ArrayList<>();

        if (requireUppercase) {
            rules.add("at least one uppercase letter");
        }

        if (requireLowercase) {
            rules.add("at least one lowercase letter");
        }

        if (requireDigits) {
            rules.add("at least one digit");
        }

        if (requireSpecialChars) {
            rules.add("at least one special character (!@#$%^&*()_+-=[]{}|;:,.<>?)");
        }

        if (!rules.isEmpty()) {
            requirements.append(" and contain ");
            for (int i = 0; i < rules.size(); i++) {
                if (i > 0 && i == rules.size() - 1) {
                    requirements.append(" and ");
                } else if (i > 0) {
                    requirements.append(", ");
                }
                requirements.append(rules.get(i));
            }
        }

        requirements.append(". Password should not contain sequential or repeated characters, and should not be a common password.");

        return requirements.toString();
    }

    /**
     * Calculate password strength score (0-100)
     */
    public int calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }

        int score = 0;

        // Length scoring (up to 25 points)
        if (password.length() >= 8) score += 10;
        if (password.length() >= 12) score += 10;
        if (password.length() >= 16) score += 5;

        // Character variety scoring (up to 40 points)
        if (UPPERCASE_PATTERN.matcher(password).matches()) score += 10;
        if (LOWERCASE_PATTERN.matcher(password).matches()) score += 10;
        if (DIGIT_PATTERN.matcher(password).matches()) score += 10;
        if (SPECIAL_CHAR_PATTERN.matcher(password).matches()) score += 10;

        // Complexity scoring (up to 35 points)
        if (!hasRepeatedCharacters(password)) score += 15;
        if (!hasSequentialCharacters(password)) score += 10;
        if (!isCommonWeakPassword(password)) score += 10;

        return Math.min(score, 100);
    }

    /**
     * Get password strength description
     */
    public String getPasswordStrengthDescription(int score) {
        if (score < 30) return "Very Weak";
        if (score < 50) return "Weak";
        if (score < 70) return "Fair";
        if (score < 85) return "Good";
        return "Strong";
    }

    /**
     * Generate a secure random password
     */
    public String generateSecurePassword(int length) {
        if (length < minLength) {
            length = minLength;
        }

        StringBuilder allChars = new StringBuilder();
        StringBuilder password = new StringBuilder();

        // Ensure at least one character from each required category
        if (requireUppercase) {
            allChars.append(UPPERCASE_CHARS);
            password.append(UPPERCASE_CHARS.charAt(secureRandom.nextInt(UPPERCASE_CHARS.length())));
        }

        if (requireLowercase) {
            allChars.append(LOWERCASE_CHARS);
            password.append(LOWERCASE_CHARS.charAt(secureRandom.nextInt(LOWERCASE_CHARS.length())));
        }

        if (requireDigits) {
            allChars.append(DIGIT_CHARS);
            password.append(DIGIT_CHARS.charAt(secureRandom.nextInt(DIGIT_CHARS.length())));
        }

        if (requireSpecialChars) {
            allChars.append(SPECIAL_CHARS);
            password.append(SPECIAL_CHARS.charAt(secureRandom.nextInt(SPECIAL_CHARS.length())));
        }

        // Fill the rest of the password length
        String allCharsStr = allChars.toString();
        for (int i = password.length(); i < length; i++) {
            password.append(allCharsStr.charAt(secureRandom.nextInt(allCharsStr.length())));
        }

        // Shuffle the password to avoid predictable patterns
        return shuffleString(password.toString());
    }

    /**
     * Check if password contains the username (case-insensitive)
     */
    public boolean containsUsername(String password, String username) {
        if (password == null || username == null) {
            return false;
        }

        return password.toLowerCase().contains(username.toLowerCase()) ||
                username.toLowerCase().contains(password.toLowerCase());
    }

    /**
     * Check if password contains personal information
     */
    public boolean containsPersonalInfo(String password, String firstName, String lastName, String email) {
        if (password == null) {
            return false;
        }

        String passwordLower = password.toLowerCase();

        if (firstName != null && firstName.length() > 2 &&
                passwordLower.contains(firstName.toLowerCase())) {
            return true;
        }

        if (lastName != null && lastName.length() > 2 &&
                passwordLower.contains(lastName.toLowerCase())) {
            return true;
        }

        if (email != null) {
            String emailUsername = email.split("@")[0];
            if (emailUsername.length() > 2 && passwordLower.contains(emailUsername.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Validate password complexity for user creation/update
     */
    public PasswordValidationResult validatePasswordComplex(String password, String username,
                                                            String firstName, String lastName, String email) {
        PasswordValidationResult result = new PasswordValidationResult();

        if (!isValidPassword(password)) {
            result.setValid(false);
            result.addError("Password does not meet basic requirements");
        }

        if (containsUsername(password, username)) {
            result.setValid(false);
            result.addError("Password should not contain the username");
        }

        if (containsPersonalInfo(password, firstName, lastName, email)) {
            result.setValid(false);
            result.addError("Password should not contain personal information");
        }

        int strength = calculatePasswordStrength(password);
        result.setStrengthScore(strength);
        result.setStrengthDescription(getPasswordStrengthDescription(strength));

        if (strength < 50) {
            result.addWarning("Password strength is " + result.getStrengthDescription().toLowerCase());
        }

        return result;
    }

    // Helper methods

    private boolean isCommonWeakPassword(String password) {
        String passwordLower = password.toLowerCase();
        for (String weakPassword : COMMON_WEAK_PASSWORDS) {
            if (passwordLower.equals(weakPassword) ||
                    passwordLower.contains(weakPassword) ||
                    weakPassword.contains(passwordLower)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasSequentialCharacters(String password) {
        for (int i = 0; i <= password.length() - 3; i++) {
            char c1 = password.charAt(i);
            char c2 = password.charAt(i + 1);
            char c3 = password.charAt(i + 2);

            // Check for ascending sequence
            if (c2 == c1 + 1 && c3 == c2 + 1) {
                return true;
            }

            // Check for descending sequence
            if (c2 == c1 - 1 && c3 == c2 - 1) {
                return true;
            }
        }
        return false;
    }

    private boolean hasRepeatedCharacters(String password) {
        for (int i = 0; i <= password.length() - 3; i++) {
            char c = password.charAt(i);
            if (password.charAt(i + 1) == c && password.charAt(i + 2) == c) {
                return true;
            }
        }
        return false;
    }

    private String shuffleString(String string) {
        char[] characters = string.toCharArray();
        for (int i = characters.length - 1; i > 0; i--) {
            int randomIndex = secureRandom.nextInt(i + 1);
            char temp = characters[i];
            characters[i] = characters[randomIndex];
            characters[randomIndex] = temp;
        }
        return new String(characters);
    }

    // Inner class for password validation results
    public static class PasswordValidationResult {
        private boolean valid = true;
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        private int strengthScore;
        private String strengthDescription;

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }

        public List<String> getErrors() { return errors; }
        public void addError(String error) {
            this.errors.add(error);
            this.valid = false;
        }

        public List<String> getWarnings() { return warnings; }
        public void addWarning(String warning) { this.warnings.add(warning); }

        public int getStrengthScore() { return strengthScore; }
        public void setStrengthScore(int strengthScore) { this.strengthScore = strengthScore; }

        public String getStrengthDescription() { return strengthDescription; }
        public void setStrengthDescription(String strengthDescription) {
            this.strengthDescription = strengthDescription;
        }

        public boolean hasWarnings() { return !warnings.isEmpty(); }
        public boolean hasErrors() { return !errors.isEmpty(); }
    }
}