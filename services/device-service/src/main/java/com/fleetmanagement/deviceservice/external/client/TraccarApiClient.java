// services/device-service/src/main/java/com/fleetmanagement/deviceservice/external/client/TraccarApiClient.java
package com.fleetmanagement.deviceservice.external.client;

import com.fleetmanagement.deviceservice.external.dto.TraccarDevice;
import com.fleetmanagement.deviceservice.external.dto.TraccarPosition;
import com.fleetmanagement.deviceservice.external.dto.TraccarCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class TraccarApiClient {

    private final RestTemplate restTemplate;

    @Value("${traccar.api.url}")
    private String traccarBaseUrl;

    @Value("${traccar.api.username}")
    private String username;

    @Value("${traccar.api.password}")
    private String password;

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public List<TraccarDevice> getAllDevices() {
        try {
            String url = traccarBaseUrl + "/api/devices";
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());

            ResponseEntity<TraccarDevice[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, TraccarDevice[].class);

            return Arrays.asList(response.getBody());
        } catch (Exception e) {
            log.error("Error getting devices from Traccar: ", e);
            throw new RuntimeException("Failed to get devices from Traccar", e);
        }
    }

    public Optional<TraccarDevice> getDeviceById(Long id) {
        try {
            String url = traccarBaseUrl + "/api/devices/" + id;
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());

            ResponseEntity<TraccarDevice> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, TraccarDevice.class);

            return Optional.ofNullable(response.getBody());
        } catch (Exception e) {
            log.error("Error getting device {} from Traccar: ", id, e);
            return Optional.empty();
        }
    }

    public TraccarDevice createDevice(String name, String uniqueId) {
        try {
            String url = traccarBaseUrl + "/api/devices";

            TraccarDevice device = new TraccarDevice();
            device.setName(name);
            device.setUniqueId(uniqueId);

            HttpEntity<TraccarDevice> entity = new HttpEntity<>(device, createHeaders());

            ResponseEntity<TraccarDevice> response = restTemplate.postForEntity(
                    url, entity, TraccarDevice.class);

            return response.getBody();
        } catch (Exception e) {
            log.error("Error creating device in Traccar: ", e);
            throw new RuntimeException("Failed to create device in Traccar", e);
        }
    }

    public TraccarDevice updateDevice(Long id, TraccarDevice device) {
        try {
            String url = traccarBaseUrl + "/api/devices/" + id;

            HttpEntity<TraccarDevice> entity = new HttpEntity<>(device, createHeaders());

            ResponseEntity<TraccarDevice> response = restTemplate.exchange(
                    url, HttpMethod.PUT, entity, TraccarDevice.class);

            return response.getBody();
        } catch (Exception e) {
            log.error("Error updating device {} in Traccar: ", id, e);
            throw new RuntimeException("Failed to update device in Traccar", e);
        }
    }

    public void deleteDevice(Long id) {
        try {
            String url = traccarBaseUrl + "/api/devices/" + id;
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());

            restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
        } catch (Exception e) {
            log.error("Error deleting device {} from Traccar: ", id, e);
            throw new RuntimeException("Failed to delete device from Traccar", e);
        }
    }

    public List<TraccarPosition> getDevicePositions(Long deviceId) {
        try {
            String url = traccarBaseUrl + "/api/positions?deviceId=" + deviceId;
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());

            ResponseEntity<TraccarPosition[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, TraccarPosition[].class);

            return Arrays.asList(response.getBody());
        } catch (Exception e) {
            log.error("Error getting positions for device {} from Traccar: ", deviceId, e);
            throw new RuntimeException("Failed to get positions from Traccar", e);
        }
    }

    public void sendCommand(Long deviceId, String commandType) {
        try {
            String url = traccarBaseUrl + "/api/commands/send";

            TraccarCommand command = new TraccarCommand();
            command.setDeviceId(deviceId);
            command.setType(commandType);

            HttpEntity<TraccarCommand> entity = new HttpEntity<>(command, createHeaders());

            restTemplate.postForEntity(url, entity, Void.class);

            log.info("Command {} sent to device {}", commandType, deviceId);
        } catch (Exception e) {
            log.error("Error sending command to device {} in Traccar: ", deviceId, e);
            throw new RuntimeException("Failed to send command to Traccar", e);
        }
    }
}