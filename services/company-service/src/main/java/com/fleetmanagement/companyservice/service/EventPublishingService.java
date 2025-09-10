package com.fleetmanagement.companyservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetmanagement.companyservice.domain.entity.Company;
import com.fleetmanagement.companyservice.domain.enums.CompanyStatus;
import com.fleetmanagement.companyservice.domain.enums.SubscriptionPlan;
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
 * FIXED EventPublishingService - Added missing methods and corrected method signatures
 */
@Service
public class EventPublishingService {

    private static final Logger logger = LoggerFactory.getLogger(EventPublishingService.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.company-created:company.created}")
    private String companyCreatedTopic;

    @Value("${app.kafka.topics.company-updated:company.updated}")
    private String companyUpdatedTopic;

    @Value("${app.kafka.topics.company-deleted:company.deleted}")
    private String companyDeletedTopic;

    @Value("${app.kafka.topics.company-subscription-changed:company.subscription.changed}")
    private String subscriptionChangedTopic;

    @Value("${app.kafka.topics.company-user-count-changed:company.user.count.changed}")
    private String userCountChangedTopic;

    @Value("${app.kafka.topics.company-user-count-synchronized:company.user.count.synchronized}")
    private String userCountSynchronizedTopic;

    @Value("${app.kafka.topics.company-status-changed:company.status.changed}")
    private String companyStatusChangedTopic;

    @Value("${app.kafka.topics.bulk-operation-completed:bulk.operation.completed}")
    private String bulkOperationCompletedTopic;

    @Value("${app.kafka.events.enabled:true}")
    private boolean eventsEnabled;

    @Autowired
    public EventPublishingService(KafkaTemplate<String, String> kafkaTemplate,
                                  ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Publish company created event
     */
    public void publishCompanyCreatedEvent(Company company) {
        if (!eventsEnabled) {
            return;
        }

        try {
            CompanyCreatedEvent event = CompanyCreatedEvent.builder()
                    .eventId(UUID.randomUUID())
                    .companyId(company.getId())
                    .companyName(company.getName())
                    .subscriptionPlan(company.getSubscriptionPlan())
                    .status(company.getStatus())
                    .createdAt(company.getCreatedAt())
                    .createdBy(company.getCreatedBy())
                    .eventTimestamp(LocalDateTime.now())
                    .build();

            publishEvent(companyCreatedTopic, company.getId().toString(), event);
            logger.info("Published company created event for company: {}", company.getId());

        } catch (Exception e) {
            logger.error("Failed to publish company created event for company: {}", company.getId(), e);
        }
    }

    /**
     * FIXED: Publish company updated event - Added overloaded method for single parameter
     */
    public void publishCompanyUpdatedEvent(Company company) {
        publishCompanyUpdatedEvent(company, null);
    }

    /**
     * Publish company updated event with previous state
     */
    public void publishCompanyUpdatedEvent(Company company, Company previousState) {
        if (!eventsEnabled) {
            return;
        }

        try {
            CompanyUpdatedEvent event = CompanyUpdatedEvent.builder()
                    .eventId(UUID.randomUUID())
                    .companyId(company.getId())
                    .companyName(company.getName())
                    .subscriptionPlan(company.getSubscriptionPlan())
                    .previousSubscriptionPlan(previousState != null ? previousState.getSubscriptionPlan() : null)
                    .status(company.getStatus())
                    .previousStatus(previousState != null ? previousState.getStatus() : null)
                    .updatedAt(company.getUpdatedAt())
                    .updatedBy(company.getUpdatedBy())
                    .eventTimestamp(LocalDateTime.now())
                    .build();

            publishEvent(companyUpdatedTopic, company.getId().toString(), event);
            logger.info("Published company updated event for company: {}", company.getId());

        } catch (Exception e) {
            logger.error("Failed to publish company updated event for company: {}", company.getId(), e);
        }
    }

    /**
     * MISSING METHOD: Publish company deleted event
     */
    public void publishCompanyDeletedEvent(Company company) {
        if (!eventsEnabled) {
            return;
        }

        try {
            CompanyDeletedEvent event = CompanyDeletedEvent.builder()
                    .eventId(UUID.randomUUID())
                    .companyId(company.getId())
                    .companyName(company.getName())
                    .subscriptionPlan(company.getSubscriptionPlan())
                    .status(company.getStatus())
                    .deletedAt(LocalDateTime.now())
                    .deletedBy(company.getUpdatedBy()) // Using updatedBy as deletedBy
                    .eventTimestamp(LocalDateTime.now())
                    .build();

            publishEvent(companyDeletedTopic, company.getId().toString(), event);
            logger.info("Published company deleted event for company: {}", company.getId());

        } catch (Exception e) {
            logger.error("Failed to publish company deleted event for company: {}", company.getId(), e);
        }
    }

    /**
     * MISSING METHOD: Publish company subscription changed event (overloaded)
     */
    public void publishCompanySubscriptionChangedEvent(Company company, SubscriptionPlan oldPlan, SubscriptionPlan newPlan) {
        publishSubscriptionChangedEvent(company.getId(), oldPlan, newPlan, company.getUpdatedBy());
    }

    /**
     * MISSING METHOD: Publish company status changed event
     */
    public void publishCompanyStatusChangedEvent(Company company, CompanyStatus oldStatus, CompanyStatus newStatus) {
        if (!eventsEnabled) {
            return;
        }

        try {
            CompanyStatusChangedEvent event = CompanyStatusChangedEvent.builder()
                    .eventId(UUID.randomUUID())
                    .companyId(company.getId())
                    .companyName(company.getName())
                    .oldStatus(oldStatus)
                    .newStatus(newStatus)
                    .changedAt(LocalDateTime.now())
                    .changedBy(company.getUpdatedBy())
                    .eventTimestamp(LocalDateTime.now())
                    .build();

            publishEvent(companyStatusChangedTopic, company.getId().toString(), event);
            logger.info("Published company status changed event for company: {} from {} to {}",
                    company.getId(), oldStatus, newStatus);

        } catch (Exception e) {
            logger.error("Failed to publish company status changed event for company: {}", company.getId(), e);
        }
    }

    /**
     * MISSING METHOD: Publish user count changed event (overloaded for Company entity)
     */
    public void publishCompanyUserCountChangedEvent(Company company, int oldCount, int newCount) {
        publishUserCountChangedEvent(company.getId(), newCount, oldCount < newCount ? "INCREMENT" : "DECREMENT");
    }

    /**
     * Publish company subscription changed event
     */
    public void publishSubscriptionChangedEvent(UUID companyId, SubscriptionPlan oldPlan, SubscriptionPlan newPlan, UUID changedBy) {
        if (!eventsEnabled) {
            return;
        }

        try {
            CompanySubscriptionChangedEvent event = CompanySubscriptionChangedEvent.builder()
                    .eventId(UUID.randomUUID())
                    .companyId(companyId)
                    .oldSubscriptionPlan(oldPlan)
                    .newSubscriptionPlan(newPlan)
                    .changedAt(LocalDateTime.now())
                    .changedBy(changedBy)
                    .eventTimestamp(LocalDateTime.now())
                    .build();

            publishEvent(subscriptionChangedTopic, companyId.toString(), event);
            logger.info("Published subscription changed event for company: {} from {} to {}", companyId, oldPlan, newPlan);

        } catch (Exception e) {
            logger.error("Failed to publish subscription changed event for company: {}", companyId, e);
        }
    }

    /**
     * Publish user count changed event
     */
    public void publishUserCountChangedEvent(UUID companyId, int newCount, String changeType) {
        if (!eventsEnabled) {
            return;
        }

        try {
            CompanyUserCountChangedEvent event = CompanyUserCountChangedEvent.builder()
                    .eventId(UUID.randomUUID())
                    .companyId(companyId)
                    .newUserCount(newCount)
                    .changeType(changeType)
                    .changedAt(LocalDateTime.now())
                    .eventTimestamp(LocalDateTime.now())
                    .build();

            publishEvent(userCountChangedTopic, companyId.toString(), event);
            logger.info("Published user count changed event for company: {} (new count: {}, type: {})",
                    companyId, newCount, changeType);

        } catch (Exception e) {
            logger.error("Failed to publish user count changed event for company: {}", companyId, e);
        }
    }

    /**
     * Publish user count synchronized event
     */
    public void publishUserCountSynchronizedEvent(UUID companyId, int previousCount, int currentCount) {
        if (!eventsEnabled) {
            return;
        }

        try {
            CompanyUserCountSynchronizedEvent event = CompanyUserCountSynchronizedEvent.builder()
                    .eventId(UUID.randomUUID())
                    .companyId(companyId)
                    .previousCount(previousCount)
                    .currentCount(currentCount)
                    .synchronizedAt(LocalDateTime.now())
                    .eventTimestamp(LocalDateTime.now())
                    .build();

            publishEvent(userCountSynchronizedTopic, companyId.toString(), event);
            logger.info("Published user count synchronized event for company: {} ({} -> {})",
                    companyId, previousCount, currentCount);

        } catch (Exception e) {
            logger.error("Failed to publish user count synchronized event for company: {}", companyId, e);
        }
    }

    /**
     * Publish bulk operation completed event
     */
    public void publishBulkOperationCompletedEvent(UUID companyId, String operationType, int successful, int failed, String operationId) {
        if (!eventsEnabled) {
            return;
        }

        try {
            BulkOperationCompletedEvent event = BulkOperationCompletedEvent.builder()
                    .eventId(UUID.randomUUID())
                    .companyId(companyId)
                    .operationType(operationType)
                    .operationId(operationId)
                    .successfulItems(successful)
                    .failedItems(failed)
                    .totalItems(successful + failed)
                    .completedAt(LocalDateTime.now())
                    .eventTimestamp(LocalDateTime.now())
                    .build();

            publishEvent(bulkOperationCompletedTopic, companyId.toString(), event);
            logger.info("Published bulk operation completed event for company: {} (operation: {}, success: {}, failed: {})",
                    companyId, operationType, successful, failed);

        } catch (Exception e) {
            logger.error("Failed to publish bulk operation completed event for company: {}", companyId, e);
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
    public static class CompanyCreatedEvent {
        private UUID eventId;
        private UUID companyId;
        private String companyName;
        private SubscriptionPlan subscriptionPlan;
        private CompanyStatus status;
        private LocalDateTime createdAt;
        private UUID createdBy;
        private LocalDateTime eventTimestamp;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CompanyUpdatedEvent {
        private UUID eventId;
        private UUID companyId;
        private String companyName;
        private SubscriptionPlan subscriptionPlan;
        private SubscriptionPlan previousSubscriptionPlan;
        private CompanyStatus status;
        private CompanyStatus previousStatus;
        private LocalDateTime updatedAt;
        private UUID updatedBy;
        private LocalDateTime eventTimestamp;
    }

    /**
     * MISSING EVENT CLASS: CompanyDeletedEvent
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CompanyDeletedEvent {
        private UUID eventId;
        private UUID companyId;
        private String companyName;
        private SubscriptionPlan subscriptionPlan;
        private CompanyStatus status;
        private LocalDateTime deletedAt;
        private UUID deletedBy;
        private LocalDateTime eventTimestamp;
    }

    /**
     * MISSING EVENT CLASS: CompanyStatusChangedEvent
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CompanyStatusChangedEvent {
        private UUID eventId;
        private UUID companyId;
        private String companyName;
        private CompanyStatus oldStatus;
        private CompanyStatus newStatus;
        private LocalDateTime changedAt;
        private UUID changedBy;
        private LocalDateTime eventTimestamp;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CompanySubscriptionChangedEvent {
        private UUID eventId;
        private UUID companyId;
        private SubscriptionPlan oldSubscriptionPlan;
        private SubscriptionPlan newSubscriptionPlan;
        private LocalDateTime changedAt;
        private UUID changedBy;
        private LocalDateTime eventTimestamp;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CompanyUserCountChangedEvent {
        private UUID eventId;
        private UUID companyId;
        private int newUserCount;
        private String changeType; // INCREMENT, DECREMENT, SYNC, MANUAL
        private LocalDateTime changedAt;
        private LocalDateTime eventTimestamp;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CompanyUserCountSynchronizedEvent {
        private UUID eventId;
        private UUID companyId;
        private int previousCount;
        private int currentCount;
        private LocalDateTime synchronizedAt;
        private LocalDateTime eventTimestamp;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BulkOperationCompletedEvent {
        private UUID eventId;
        private UUID companyId;
        private String operationType;
        private String operationId;
        private int successfulItems;
        private int failedItems;
        private int totalItems;
        private LocalDateTime completedAt;
        private LocalDateTime eventTimestamp;
    }
}