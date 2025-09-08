package com.fleetmanagement.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients; // ADD THIS
import org.springframework.retry.annotation.EnableRetry; // ADD THIS

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients  // NEW: Enable Feign clients
@EnableRetry         // NEW: Enable retry functionality
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}