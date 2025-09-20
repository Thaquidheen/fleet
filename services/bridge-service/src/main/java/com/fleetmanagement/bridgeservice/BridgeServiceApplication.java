package com.fleetmanagement.bridgeservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Bridge Service Application
 *
 * Main entry point for the Bridge Service that handles data synchronization
 * between Traccar GPS tracking system and Fleet Management microservices.
 *
 * Key Features:
 * - Real-time position data synchronization
 * - Event publishing to Kafka
 * - Device health monitoring
 * - Command relay to GPS devices
 * - Error recovery and retry mechanisms
 *
 * @author Fleet Management Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
@EnableKafka
@EnableScheduling
@EnableAsync
public class BridgeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BridgeServiceApplication.class, args);
    }
}