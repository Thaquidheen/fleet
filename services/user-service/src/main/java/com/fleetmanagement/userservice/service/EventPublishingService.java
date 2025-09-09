package com.fleetmanagement.userservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetmanagement.userservice.domain.entity.User;
import com.fleetmanagement.userservice.domain.enums.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Event Publishing Service
 *
 * Service for publishing domain events to Kafka topics for inter-service communication.
 * Handles user-related events that other services need to be aware of.
 */
@Service
public class EventPublishingService {

    private static final Logger logger = LoggerFactory.getLogger(EventPublishingService.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.user-created:user.created}")
    private String userCreatedTopic;

    @Value("${app.kafka.topics.user-updated:user.updated}")
    private String userUpdatedTopic;

    @Value("${app.kafka.topics.user-deleted:user.deleted}")
    private String userDeletedTopic;

    @Value("${app.kafka.topics.driver-assigned:driver.assigned}")
    private String driverAssignedTopic;

    @Value("${app.kafka.topics.driver-unassigned:driver.unassigned}")
    private String driverUnassignedTopic;

    @Value("${app.kafka.topics.user-role-changed:user.role.changed}")
    private String userRoleChangedTopic;

    @Value("${app.kafka.topics.user-status-changed:user.status.changed}")
    private String userStatusChangedTopic;

    @Value("${app.kafka.events.enabled:true}")
    private boolean eventsEnabled;

    @Autowired
    public EventPublishingService(KafkaTemplate<String, String> kafkaTemplate,
                                  ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Publish user created event
     */
    public void publishUserCreatedEvent(User user) {
        if (!eventsEnabled) {
            return;
        }

        try {
            UserCreatedEvent event = UserCreatedEvent.builder()
                    .eventId(UUID.randomUUID())
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .role(user.getRole())
                    .companyId(user.getCompanyId())
                    .status(user.getStatus())
                    .createdAt(user.getCreatedAt())
                    .eventTimestamp(LocalDateTime.now())
                    .build();

            publishEvent(userCreatedTopic, user.getId().toString(), event);
            logger.info("Published user created event for user: {}", user.getId());

        } catch (Exception e) {
            logger.error("Failed to publish user created event for user: {}", user.getId(), e);
        }
    }

    /**
     * Publish user updated event
     */
    public void publishUserUpdatedEvent(User user, User previousState) {
        if (!eventsEnabled) {
            return;
        }

        try {
            UserUpdatedEvent event = UserUpdatedEvent.builder()
                    .eventId(UUID.randomUUID())
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .role(user.getRole())
                    .companyId(user.getCompanyId())
                    .status(user.getStatus())
                    .previousRole(previousState != null ? previousState.getRole() : null)
                    .previousStatus(previousState != null ? previousState.getStatus() : null)
                    .updatedAt(user.getUpdatedAt())
                    .eventTimestamp(LocalDateTime.now())
                    .build();

            publishEvent(userUpdatedTopic, user.getId().toString(), event);
            logger.info("Published user updated event for user: {}", user.getId());

        } catch (Exception e) {
            logger.error("Failed to publish user updated event for user: {}", user.getId(), e);
        }
    }

    /**
     * Publish user deleted event
     */
    public void publishUserDeletedEvent(UUID userId, UUID companyId, UserRole role) {
        if (!eventsEnabled) {
            return;
        }

        try {
            UserDeletedEvent event = UserDeletedEvent.builder()
                    .eventId(UUID.randomUUID())
                    .userId(userId)
                    .companyId(companyId)
                    .role(role)
                    .deletedAt(LocalDateTime.now())
                    .eventTimestamp(LocalDateTime.now())
                    .build();

            publishEvent(userDeletedTopic, userId.toString(), event);
            logger.info("Published user deleted event for user: {}", userId);

        } catch (Exception e) {
            logger.error("Failed to publish user deleted event for user: {}", userId, e);
        }
    }

    /**
     * Publish driver assigned event
     */
    public void publishDriverAssignedEvent(UUID driverId, UUID vehicleId, UUID companyId) {
        if (!eventsEnabled) {
            return;
        }

        try {
            DriverAssignedEvent event = DriverAssignedEvent.builder()
                    .eventId(UUID.randomUUID())
                    .driverId(driverId)
                    .vehicleId(vehicleId)
                    .companyId(companyId)
                    .assignedAt(LocalDateTime.now())
                    .eventTimestamp(LocalDateTime.now())
                    .build();

            publishEvent(driverAssignedTopic, driverId.toString(), event);
            logger.info("Published driver assigned event for driver: {} to vehicle: {}", driverId, vehicleId);

        } catch (Exception e) {
            logger.error("Failed to publish driver assigned event for driver: {}", driverId, e);
        }
    }

    /**
     * Publish driver unassigned event
     */
    public void publishDriverUnassignedEvent(UUID driverId, UUID companyId) {
        if (!eventsEnabled) {
            return;
        }

        try {
            DriverUnassignedEvent event = DriverUnassignedEvent.builder()
                    .eventId(UUID.randomUUID())
                    .driverId(driverId)
                    .companyId(companyId)
                    .unassignedAt(LocalDateTime.now())
                    .eventTimestamp(LocalDateTime.now())
                    .build();

            publishEvent(driverUnassignedTopic, driverId.toString(), event);
            logger.info("Published driver unassigned event for driver: {}", driverId);

        } catch (Exception e) {
            logger.error("Failed to publish driver unassigned event for driver: {}", driverId, e);
        }
    }

    /**
     * Publish user role changed event
     */
    public void publishUserRoleChangedEvent(UUID userId, UserRole oldRole, UserRole newRole, UUID companyId) {
        if (!eventsEnabled) {
            return;
        }

        try {
            UserRoleChangedEvent event = UserRoleChangedEvent.builder()
                    .eventId(UUID.randomUUID())
                    .userId(userId)
                    .oldRole(oldRole)
                    .newRole(newRole)
                    .companyId(companyId)
                    .changedAt(LocalDateTime.now())
                    .eventTimestamp(LocalDateTime.now())
                    .build();

            publishEvent(userRoleChangedTopic, userId.toString(), event);
            logger.info("Published user role changed event for user: {} from {} to {}", userId, oldRole, newRole);

        } catch (Exception e) {
            logger.error("Failed to publish user role changed event for user: {}", userId, e);
        }
    }

    // Private helper method to publish events
    private void publishEvent(String topic, String key, Object event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);

            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, eventJson);

            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    logger.error("Failed to send event to topic {}: {}", topic, throwable.getMessage());
                } else {
                    logger.debug("Event sent successfully to topic {} with key {}: {}",
                            topic, key, result.getRecordMetadata());
                }
            });

        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize event for topic {}: {}", topic, e.getMessage());
            throw new RuntimeException("Event serialization failed", e);
        }
    }

    // Event classes
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserCreatedEvent {
        private UUID eventId;
        private UUID userId;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private UserRole role;
        private UUID companyId;
        private com.fleetmanagement.userservice.domain.enums.UserStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime eventTimestamp;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserUpdatedEvent {
        private UUID eventId;
        private UUID userId;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private UserRole role;
        private UUID companyId;
        private com.fleetmanagement.userservice.domain.enums.UserStatus status;
        private UserRole previousRole;
        private com.fleetmanagement.userservice.domain.enums.UserStatus previousStatus;
        private LocalDateTime updatedAt;
        private LocalDateTime eventTimestamp;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserDeletedEvent {
        private UUID eventId;
        private UUID userId;
        private UUID companyId;
        private UserRole role;
        private LocalDateTime deletedAt;
        private LocalDateTime eventTimestamp;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DriverAssignedEvent {
        private UUID eventId;
        private UUID driverId;
        private UUID vehicleId;
        private UUID companyId;
        private LocalDateTime assignedAt;
        private LocalDateTime eventTimestamp;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DriverUnassignedEvent {
        private UUID eventId;
        private UUID driverId;
        private UUID companyId;
        private LocalDateTime unassignedAt;
        private LocalDateTime eventTimestamp;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserRoleChangedEvent {
        private UUID eventId;
        private UUID userId;
        private UserRole oldRole;
        private UserRole newRole;
        private UUID companyId;
        private LocalDateTime changedAt;
        private LocalDateTime eventTimestamp;
    }
}