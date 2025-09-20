

package com.fleetmanagement.deviceservice.domain.entity;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.UUID;


/**
 * Sensor Subscription Entity
 * Represents paid sensor subscriptions for revenue generation
 */
@Entity
@Table(name = "sensor_subscriptions", indexes = {
        @Index(name = "idx_sensor_subscription_sensor", columnList = "device_sensor_id"),
        @Index(name = "idx_sensor_subscription_company", columnList = "company_id"),
        @Index(name = "idx_sensor_subscription_active", columnList = "is_active"),
        @Index(name = "idx_sensor_subscription_billing", columnList = "billing_cycle_start, billing_cycle_end")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class SensorSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Device sensor this subscription is for
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_sensor_id", nullable = false)
    private DeviceSensor deviceSensor;

    /**
     * Company that owns the subscription
     */
    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    /**
     * Monthly price for this sensor
     */
    @Column(name = "monthly_price", nullable = false, precision = 10, scale = 2)
    private Double monthlyPrice;

    /**
     * Whether this subscription is currently active
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Subscription start date
     */
    @Column(name = "subscription_start", nullable = false)
    private LocalDateTime subscriptionStart;

    /**
     * Subscription end date (null for ongoing)
     */
    @Column(name = "subscription_end")
    private LocalDateTime subscriptionEnd;

    /**
     * Current billing cycle start
     */
    @Column(name = "billing_cycle_start", nullable = false)
    private LocalDateTime billingCycleStart;

    /**
     * Current billing cycle end
     */
    @Column(name = "billing_cycle_end", nullable = false)
    private LocalDateTime billingCycleEnd;

    /**
     * Auto-renewal enabled
     */
    @Column(name = "auto_renewal", nullable = false)
    @Builder.Default
    private Boolean autoRenewal = true;

    /**
     * Subscription notes
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * Audit fields
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    // Business methods

    /**
     * Check if subscription is active
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive) &&
                (subscriptionEnd == null || subscriptionEnd.isAfter(LocalDateTime.now()));
    }

    /**
     * Check if subscription is in current billing cycle
     */
    public boolean isInCurrentBillingCycle() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(billingCycleStart) && now.isBefore(billingCycleEnd);
    }

    /**
     * Cancel subscription
     */
    public void cancel() {
        this.isActive = false;
        this.subscriptionEnd = LocalDateTime.now();
        this.autoRenewal = false;
    }

    /**
     * Renew subscription for next billing cycle
     */
    public void renewBillingCycle() {
        this.billingCycleStart = this.billingCycleEnd;
        this.billingCycleEnd = this.billingCycleStart.plusDays(30); // Default 30-day cycle
    }
}