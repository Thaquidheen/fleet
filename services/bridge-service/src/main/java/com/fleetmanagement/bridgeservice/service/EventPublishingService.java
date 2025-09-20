package com.fleetmanagement.bridgeservice.service;

import com.fleetmanagement.bridgeservice.exception.EventPublishingException;
import com.fleetmanagement.bridgeservice.model.events.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class EventPublishingService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${bridge.kafka.topics.location-updates:device.location.updated}")
    private String locationTopic;

    @Value("${bridge.kafka.topics.sensor-readings:device.sensor.reading}")
    private String sensorTopic;

    @Value("${bridge.kafka.topics.device-heartbeat:device.heartbeat}")
    private String heartbeatTopic;

    @Value("${bridge.kafka.topics.command-results:device.command.result}")
    private String commandTopic;

    @Autowired
    public EventPublishingService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishLocationUpdate(LocationUpdatedEvent event) {
        try {
            String key = event.getDeviceId().toString();

            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(locationTopic, key, event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("Location update published for device: {}", event.getDeviceId());
                } else {
                    log.error("Failed to publish location update for device: {}", event.getDeviceId(), ex);
                    throw new EventPublishingException("Failed to publish location update", ex);
                }
            });

        } catch (Exception e) {
            log.error("Error publishing location update event", e);
            throw new EventPublishingException("Failed to publish location update", e);
        }
    }

    public void publishSensorReading(SensorReadingEvent event) {
        try {
            String key = event.getDeviceId().toString();

            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(sensorTopic, key, event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("Sensor reading published for device: {}", event.getDeviceId());
                } else {
                    log.error("Failed to publish sensor reading for device: {}", event.getDeviceId(), ex);
                    throw new EventPublishingException("Failed to publish sensor reading", ex);
                }
            });

        } catch (Exception e) {
            log.error("Error publishing sensor reading event", e);
            throw new EventPublishingException("Failed to publish sensor reading", e);
        }
    }

    public void publishDeviceHeartbeat(DeviceHeartbeatEvent event) {
        try {
            String key = event.getDeviceId().toString();

            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(heartbeatTopic, key, event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("Device heartbeat published for device: {}", event.getDeviceId());
                } else {
                    log.error("Failed to publish device heartbeat for device: {}", event.getDeviceId(), ex);
                    throw new EventPublishingException("Failed to publish device heartbeat", ex);
                }
            });

        } catch (Exception e) {
            log.error("Error publishing device heartbeat event", e);
            throw new EventPublishingException("Failed to publish device heartbeat", e);
        }
    }

    public void publishCommandResult(CommandExecutedEvent event) {
        try {
            String key = event.getDeviceId().toString();

            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(commandTopic, key, event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("Command result published for device: {}", event.getDeviceId());
                } else {
                    log.error("Failed to publish command result for device: {}", event.getDeviceId(), ex);
                    throw new EventPublishingException("Failed to publish command result", ex);
                }
            });

        } catch (Exception e) {
            log.error("Error publishing command result event", e);
            throw new EventPublishingException("Failed to publish command result", e);
        }
    }

    public void publishBatch(List<Object> events) {
        try {
            log.debug("Publishing batch of {} events", events.size());

            for (Object event : events) {
                if (event instanceof LocationUpdatedEvent) {
                    publishLocationUpdate((LocationUpdatedEvent) event);
                } else if (event instanceof SensorReadingEvent) {
                    publishSensorReading((SensorReadingEvent) event);
                } else if (event instanceof DeviceHeartbeatEvent) {
                    publishDeviceHeartbeat((DeviceHeartbeatEvent) event);
                } else if (event instanceof CommandExecutedEvent) {
                    publishCommandResult((CommandExecutedEvent) event);
                } else {
                    log.warn("Unknown event type: {}", event.getClass().getSimpleName());
                }
            }

            log.debug("Batch publishing completed for {} events", events.size());

        } catch (Exception e) {
            log.error("Error publishing event batch", e);
            throw new EventPublishingException("Failed to publish event batch", e);
        }
    }
}