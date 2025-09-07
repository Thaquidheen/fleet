package com.fleetmanagement.companyservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Optional;
import java.util.UUID;

@Configuration
@EnableJpaRepositories(basePackages = "com.fleetmanagement.companyservice.repository")
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableTransactionManagement
public class DatabaseConfig {

    @Bean
    public AuditorAware<UUID> auditorAware() {
        return new AuditorAwareImpl();
    }

    /**
     * Custom AuditorAware implementation to track who created/updated entities
     */
    public static class AuditorAwareImpl implements AuditorAware<UUID> {

        @Override
        public Optional<UUID> getCurrentAuditor() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() ||
                    "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.empty();
            }

            try {
                // In JWT-based auth, the principal is usually the user ID as string
                String userIdString = authentication.getName();
                UUID userId = UUID.fromString(userIdString);
                return Optional.of(userId);
            } catch (IllegalArgumentException e) {
                // If parsing fails, return system user ID or empty
                return Optional.empty();
            }
        }
    }
}