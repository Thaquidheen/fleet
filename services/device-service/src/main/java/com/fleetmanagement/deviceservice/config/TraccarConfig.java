
package com.fleetmanagement.deviceservice.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import java.time.Duration;

/**
 * Traccar Integration Configuration
 */
@Configuration
public class TraccarConfig {

    @Value("${traccar.base-url}")
    private String baseUrl;

    @Value("${traccar.api-url}")
    private String apiUrl;

    @Value("${traccar.username}")
    private String username;

    @Value("${traccar.password}")
    private String password;

    @Value("${traccar.connection-timeout:30000}")
    private Integer connectionTimeout;

    @Value("${traccar.read-timeout:60000}")
    private Integer readTimeout;

    @Bean
    public RestTemplate traccarRestTemplate() {
        return new RestTemplateBuilder()
                .rootUri(apiUrl)
                .basicAuthentication(username, password)
                .setConnectTimeout(Duration.ofMillis(connectionTimeout))
                .setReadTimeout(Duration.ofMillis(readTimeout))
                .build();
    }

    // Getters
    public String getBaseUrl() { return baseUrl; }
    public String getApiUrl() { return apiUrl; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Integer getConnectionTimeout() { return connectionTimeout; }
    public Integer getReadTimeout() { return readTimeout; }
}

