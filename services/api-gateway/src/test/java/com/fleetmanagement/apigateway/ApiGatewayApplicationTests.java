package com.fleetmanagement.apigateway;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for API Gateway
 *
 * Tests the core functionality of the API Gateway including
 * health checks, routing, and fallback mechanisms
 *
 * @author Fleet Management Team
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@SpringJUnitConfig
@DisplayName("API Gateway Integration Tests")
class ApiGatewayApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Context loads successfully")
    void contextLoads() {
        // This test verifies that the Spring application context loads without errors
        // If this test passes, it means all beans are properly configured
    }

    @Test
    @DisplayName("API Gateway health endpoint is accessible")
    void healthEndpointShouldBeAccessible() {
        // Test the actuator health endpoint
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/actuator/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("UP");
    }

    @Test
    @DisplayName("Gateway info endpoint returns application information")
    void gatewayInfoEndpointShouldReturnInfo() {
        // Test the gateway info endpoint
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/gateway/info", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("API Gateway");
        assertThat(response.getBody()).contains("Fleet Management System");
    }

    @Test
    @DisplayName("Gateway health endpoint returns status")
    void gatewayHealthEndpointShouldReturnStatus() {
        // Test the custom gateway health endpoint
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/gateway/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("api-gateway");
        assertThat(response.getBody()).contains("UP");
    }

    @Test
    @DisplayName("Gateway services endpoint returns registered services")
    void gatewayServicesEndpointShouldReturnServices() {
        // Test the services discovery endpoint
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/gateway/services", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("registeredServices");
    }

    @Test
    @DisplayName("Gateway status endpoint returns system status")
    void gatewayStatusEndpointShouldReturnSystemStatus() {
        // Test the system status endpoint
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/gateway/status", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Fleet Management System");
        assertThat(response.getBody()).contains("systemReadiness");
    }

    @Test
    @DisplayName("Fallback endpoints are accessible")
    void fallbackEndpointsShouldBeAccessible() {
        // Test auth fallback
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/fallback/auth", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).contains("Authentication service is temporarily unavailable");
        assertThat(response.getBody()).contains("fallback");
    }

    @Test
    @DisplayName("Prometheus metrics endpoint is enabled")
    void prometheusMetricsEndpointShouldBeEnabled() {
        // Test Prometheus metrics endpoint
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/actuator/prometheus", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("jvm_memory");
    }

    @Test
    @DisplayName("Gateway routes endpoint shows configured routes")
    void gatewayRoutesEndpointShouldShowRoutes() {
        // Test the actuator gateway routes endpoint
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/actuator/gateway/routes", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}