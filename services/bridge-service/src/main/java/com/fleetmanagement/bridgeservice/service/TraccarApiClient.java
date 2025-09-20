package com.fleetmanagement.bridgeservice.service;

import com.fleetmanagement.bridgeservice.config.TraccarConfig;
import com.fleetmanagement.bridgeservice.exception.TraccarApiException;
import com.fleetmanagement.bridgeservice.model.traccar.*;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class TraccarApiClient {

    private final WebClient webClient;
    private final TraccarConfig traccarConfig;

    @Autowired
    public TraccarApiClient(TraccarConfig traccarConfig) {
        this.traccarConfig = traccarConfig;
        this.webClient = WebClient.builder()
                .baseUrl(traccarConfig.getBaseUrl())
                .defaultHeaders(headers -> headers.setBasicAuth(
                        traccarConfig.getUsername(),
                        traccarConfig.getPassword()))
                .build();

        log.info("Traccar API client initialized for: {}", traccarConfig.getBaseUrl());
    }

    @Retry(name = "traccarApi")
    public List<TraccarPosition> getLatestPositions() {
        try {
            log.debug("Fetching latest positions from Traccar");

            List<TraccarPosition> positions = webClient.get()
                    .uri(traccarConfig.getPositionsEndpoint())
                    .retrieve()
                    .onStatus(HttpStatus::isError, response ->
                            Mono.error(new TraccarApiException("Failed to fetch positions: " + response.statusCode())))
                    .bodyToFlux(TraccarPosition.class)
                    .collectList()
                    .timeout(traccarConfig.getTimeout())
                    .block();

            log.debug("Fetched {} positions from Traccar", positions != null ? positions.size() : 0);
            return positions != null ? positions : List.of();

        } catch (WebClientResponseException e) {
            log.error("Traccar API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new TraccarApiException("Traccar API error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error fetching positions from Traccar", e);
            throw new TraccarApiException("Failed to fetch positions", e);
        }
    }

    @Retry(name = "traccarApi")
    public List<TraccarDevice> getAllDevices() {
        try {
            log.debug("Fetching devices from Traccar");

            List<TraccarDevice> devices = webClient.get()
                    .uri(traccarConfig.getDevicesEndpoint())
                    .retrieve()
                    .onStatus(HttpStatus::isError, response ->
                            Mono.error(new TraccarApiException("Failed to fetch devices: " + response.statusCode())))
                    .bodyToFlux(TraccarDevice.class)
                    .collectList()
                    .timeout(traccarConfig.getTimeout())
                    .block();

            log.debug("Fetched {} devices from Traccar", devices != null ? devices.size() : 0);
            return devices != null ? devices : List.of();

        } catch (Exception e) {
            log.error("Error fetching devices from Traccar", e);
            throw new TraccarApiException("Failed to fetch devices", e);
        }
    }

    @Retry(name = "traccarApi")
    public List<TraccarEvent> getEvents(Instant from, Instant to) {
        try {
            log.debug("Fetching events from Traccar between {} and {}", from, to);

            List<TraccarEvent> events = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(traccarConfig.getEventsEndpoint())
                            .queryParam("from", from.toString())
                            .queryParam("to", to.toString())
                            .build())
                    .retrieve()
                    .onStatus(HttpStatus::isError, response ->
                            Mono.error(new TraccarApiException("Failed to fetch events: " + response.statusCode())))
                    .bodyToFlux(TraccarEvent.class)
                    .collectList()
                    .timeout(traccarConfig.getTimeout())
                    .block();

            log.debug("Fetched {} events from Traccar", events != null ? events.size() : 0);
            return events != null ? events : List.of();

        } catch (Exception e) {
            log.error("Error fetching events from Traccar", e);
            throw new TraccarApiException("Failed to fetch events", e);
        }
    }

    @Retry(name = "traccarApi")
    public TraccarCommand sendCommand(Long deviceId, String commandType, String commandData) {
        try {
            log.debug("Sending command {} to device {}", commandType, deviceId);

            TraccarCommand command = new TraccarCommand();
            command.setDeviceId(deviceId);
            command.setType(commandType);
            command.setDescription(commandData);

            TraccarCommand result = webClient.post()
                    .uri(traccarConfig.getCommandsEndpoint())
                    .bodyValue(command)
                    .retrieve()
                    .onStatus(HttpStatus::isError, response ->
                            Mono.error(new TraccarApiException("Failed to send command: " + response.statusCode())))
                    .bodyToMono(TraccarCommand.class)
                    .timeout(traccarConfig.getTimeout())
                    .block();

            log.debug("Command sent successfully to device {}", deviceId);
            return result;

        } catch (Exception e) {
            log.error("Error sending command to device {}", deviceId, e);
            throw new TraccarApiException("Failed to send command", e);
        }
    }

    @Retry(name = "traccarApi")
    public boolean isServerHealthy() {
        try {
            log.debug("Checking Traccar server health");

            webClient.get()
                    .uri(traccarConfig.getServerEndpoint())
                    .retrieve()
                    .onStatus(HttpStatus::isError, response ->
                            Mono.error(new TraccarApiException("Server health check failed: " + response.statusCode())))
                    .bodyToMono(Object.class)
                    .timeout(traccarConfig.getTimeout())
                    .block();

            log.debug("Traccar server is healthy");
            return true;

        } catch (Exception e) {
            log.warn("Traccar server health check failed", e);
            return false;
        }
    }
}