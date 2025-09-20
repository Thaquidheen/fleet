package com.fleetmanagement.deviceservice.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


/**
 * Database Configuration
 */
@Configuration
@EnableJpaAuditing
public class DatabaseConfig {
    // JPA Auditing is enabled via annotation
}


