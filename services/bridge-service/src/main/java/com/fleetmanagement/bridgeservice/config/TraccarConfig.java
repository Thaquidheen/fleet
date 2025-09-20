package com.fleetmanagement.bridgeservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "bridge.traccar")
@Data
@Validated
public class TraccarConfig {

    @NotBlank
    private String baseUrl = "http://traccar-server:8082";

    @NotBlank
    private String username = "admin";

    @NotBlank
    private String password = "admin";

    @NotNull
    private Duration timeout = Duration.ofSeconds(30);

    private int maxRetries = 3;

    private Duration retryDelay = Duration.ofSeconds(1);

    // API endpoints
    private String positionsEndpoint = "/api/positions";
    private String devicesEndpoint = "/api/devices";
    private String eventsEndpoint = "/api/events";
    private String commandsEndpoint = "/api/commands";
    private String serverEndpoint = "/api/server";
}
