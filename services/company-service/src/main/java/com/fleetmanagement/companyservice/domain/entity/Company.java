package com.fleetmanagement.companyservice.domain.entity;

import com.fleetmanagement.companyservice.domain.enums.CompanyStatus;
import com.fleetmanagement.companyservice.domain.enums.SubscriptionPlan;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "companies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Company {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, length = 50)
    private String subdomain;

    @Column(length = 100)
    private String industry;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 255)
    private String website;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CompanyStatus status = CompanyStatus.TRIAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan", nullable = false)
    @Builder.Default
    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.BASIC;

    @Column(length = 50)
    @Builder.Default
    private String timezone = "UTC";

    @Column(length = 5)
    @Builder.Default
    private String language = "en";

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Subscription limits
    @Column(name = "max_users", nullable = false)
    @Builder.Default
    private Integer maxUsers = 5;

    @Column(name = "max_vehicles", nullable = false)
    @Builder.Default
    private Integer maxVehicles = 10;

    @Column(name = "current_user_count")
    @Builder.Default
    private Integer currentUserCount = 0;

    @Column(name = "current_vehicle_count")
    @Builder.Default
    private Integer currentVehicleCount = 0;

    @Column(name = "trial_end_date")
    private LocalDate trialEndDate;

    // Contact person information
    @Column(name = "contact_person_name", length = 100)
    private String contactPersonName;

    @Column(name = "contact_person_title", length = 100)
    private String contactPersonTitle;

    @Column(name = "contact_person_email", length = 255)
    private String contactPersonEmail;

    @Column(name = "contact_person_phone", length = 20)
    private String contactPersonPhone;

    // Audit fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Version
    private Long version;

    // Business methods
    public boolean canAddUser() {
        return currentUserCount < maxUsers;
    }

    public boolean canAddVehicle() {
        return currentVehicleCount < maxVehicles;
    }

    public void incrementUserCount() {
        if (canAddUser()) {
            this.currentUserCount++;
        }
    }

    public void decrementUserCount() {
        if (currentUserCount > 0) {
            this.currentUserCount--;
        }
    }

    public void incrementVehicleCount() {
        if (canAddVehicle()) {
            this.currentVehicleCount++;
        }
    }

    public void decrementVehicleCount() {
        if (currentVehicleCount > 0) {
            this.currentVehicleCount--;
        }
    }

    public boolean isTrialExpired() {
        return status == CompanyStatus.TRIAL &&
                trialEndDate != null &&
                trialEndDate.isBefore(LocalDate.now());
    }

    public boolean isActive() {
        return status == CompanyStatus.ACTIVE;
    }
}