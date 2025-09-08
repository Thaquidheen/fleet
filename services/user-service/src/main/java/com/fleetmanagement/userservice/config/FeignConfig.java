package com.fleetmanagement.userservice.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new JwtRequestInterceptor();
    }

    public static class JwtRequestInterceptor implements RequestInterceptor {
        @Override
        public void apply(RequestTemplate requestTemplate) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()) {
                Object credentials = authentication.getCredentials();
                if (credentials != null) {
                    requestTemplate.header("Authorization", "Bearer " + credentials.toString());
                }
            }

            requestTemplate.header("X-Service-Name", "user-service");
            requestTemplate.header("X-Request-ID", java.util.UUID.randomUUID().toString());
        }
    }
}