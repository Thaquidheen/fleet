

package com.fleetmanagement.deviceservice.config;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



/**
 * Feign Client Configuration
 */
@Configuration
public class FeignConfig {

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // Add common headers for service-to-service communication
            requestTemplate.header("Service-Name", "device-service");
            requestTemplate.header("Content-Type", "application/json");
        };
    }
}