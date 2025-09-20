package com.fleetmanagement.deviceservice.config;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;



/**
 * Kafka Configuration
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Device lifecycle topics
     */
    @Bean
    public NewTopic deviceRegisteredTopic() {
        return TopicBuilder.name("device.registered")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic deviceAssignedTopic() {
        return TopicBuilder.name("device.assigned")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic deviceUnassignedTopic() {
        return TopicBuilder.name("device.unassigned")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic deviceDeactivatedTopic() {
        return TopicBuilder.name("device.deactivated")
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Real-time data topics
     */
    @Bean
    public NewTopic deviceLocationUpdatedTopic() {
        return TopicBuilder.name("device.location.updated")
                .partitions(6)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic deviceSensorDataTopic() {
        return TopicBuilder.name("device.sensor.data")
                .partitions(6)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic deviceHealthUpdateTopic() {
        return TopicBuilder.name("device.health.updated")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic deviceConnectionStatusTopic() {
        return TopicBuilder.name("device.connection.status")
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Command and alert topics
     */
    @Bean
    public NewTopic deviceCommandSentTopic() {
        return TopicBuilder.name("device.command.sent")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic deviceAlertTriggeredTopic() {
        return TopicBuilder.name("device.alert.triggered")
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Sensor subscription topics
     */
    @Bean
    public NewTopic sensorSubscriptionChangedTopic() {
        return TopicBuilder.name("sensor.subscription.changed")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic sensorBillingEventTopic() {
        return TopicBuilder.name("sensor.billing.event")
                .partitions(3)
                .replicas(1)
                .build();
    }
}

