package com.fleetmanagement.bridgeservice.config;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@Slf4j
public class RetryConfiguration {

    @Bean("traccarApiRetry")
    public Retry traccarApiRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(1))
                .exponentialBackoffMultiplier(2)
                .retryOnException(throwable -> {
                    // Retry on specific exceptions
                    return throwable instanceof RuntimeException &&
                            !throwable.getCause() instanceof InterruptedException;
                })
                .build();

        Retry retry = Retry.of("traccarApi", config);

        retry.getEventPublisher()
                .onRetry(event -> log.warn("Retry attempt {} for operation: {}",
                        event.getNumberOfRetryAttempts(), event.getName()));

        return retry;
    }
}