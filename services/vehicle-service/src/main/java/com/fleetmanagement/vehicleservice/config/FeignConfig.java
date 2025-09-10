package com.fleetmanagement.vehicleservice.config;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.TimeUnit;

@Configuration
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public Request.Options feignRequestOptions() {
        return new Request.Options(
                5000, TimeUnit.MILLISECONDS,  // connectTimeout
                10000, TimeUnit.MILLISECONDS, // readTimeout
                true                          // followRedirects
        );
    }

    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(
                1000,  // period
                3000,  // maxPeriod
                3      // maxAttempts
        );
    }
}