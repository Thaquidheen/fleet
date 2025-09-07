package com.fleetmanagement.companyservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Company Service Application
 *
 * Handles company management, multi-tenancy, and subscription management
 * for the Fleet Management System.
 *
 * Features:
 * - Company CRUD operations
 * - Multi-tenant data isolation
 * - Subscription and billing management
 * - Company settings and configuration
 * - Bulk user management
 * - Cross-service integration
 *
 * @author Fleet Management Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class CompanyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompanyServiceApplication.class, args);
    }
}