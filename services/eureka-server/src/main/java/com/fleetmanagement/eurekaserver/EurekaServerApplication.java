package com.fleetmanagement.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Server Application
 * Service Discovery Server for Fleet Management System
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

	public static void main(String[] args) {
		System.out.println("🌐 Starting Eureka Server - Fleet Management Service Discovery");
		SpringApplication.run(EurekaServerApplication.class, args);
		System.out.println("✅ Eureka Server started successfully on port 8761");
		System.out.println("🔗 Dashboard: http://localhost:8761");
	}
}