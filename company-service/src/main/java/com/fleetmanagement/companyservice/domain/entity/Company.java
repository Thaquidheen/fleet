package com.fleetmanagement.companyservice.domain.entity;

import com.fleetmanagement.companyservice.domain.enums.CompanyStatus;
import com.fleetmanagement.companyservice.domain.enums.SubscriptionPlan;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "companies", indexes = {
        @Index(name = "idx_company_subdomain", columnList = "subdomain"),
        @Index(name = "idx_company_status", columnList = "status"),
        @Index(name = "idx_company_subscription", columnList = "subscriptionPlan")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @NotBlank(message = "Company name is required")
    @Size(max = 100, message = "Company name must not exceed 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 50, message = "Subdomain must not exceed 50 characters")
    @Column(name = "subdomain", unique = true, length = 50)
    private String subdomain;

    @Size(max = 50, message = "Industry must not exceed 50 characters")
    @Column(name = "industry", length = 50)
    private String industry;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    @Column(name = "phone", length = 20)
    private String phone;

    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Column(name = "email", length = 100)
    private String email;

    @Size(max = 255, message = "Website must not exceed 255 characters")
    @Column(name = "website", length = 255)
    private String website;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @NotNull(message = "Company status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CompanyStatus status = CompanyStatus.TRIAL;

    @NotNull(message = "Subscription plan is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan", nullable = false, length = 20)
    @Builder.Default
    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.BASIC;

    @Column(name = "max_users", nullable = false)
    @Builder.Default
    private Integer maxUsers = 5;

    @Column(name = "max_vehicles", nullable = false)
    @Builder.Default
    private Integer maxVehicles = 10;

    @Column(name = "current_user_count", nullable = false)
    @Builder.Default
    private Integer currentUserCount = 0;

    @Column(name = "current_vehicle_count", nullable = false)
    @Builder.Default
    private Integer currentVehicleCount = 0;

    @Column(name = "trial_end_date")
    private LocalDate trialEndDate;

    @Column(name = "logo_url")
    private String logoUrl;

    @Size(max = 10, message = "Timezone must not exceed 10 characters")
    @Column(name = "timezone", length = 10)
    @Builder.Default
    private String timezone = "UTC";

    @Size(max = 10, message = "Language must not exceed 10 characters")
    @Column(name = "language", length = 10)
    @Builder.Default
    private String language = "en";

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Version
    @Column(name = "version")
    private Long version;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<CompanySettings> settings = new HashSet<>();

    // Business Methods
    public boolean canAddUser() {
        return currentUserCount < maxUsers && status == CompanyStatus.ACTIVE;
    }

    public boolean canAddVehicle() {
        return currentVehicleCount < maxVehicles && status == CompanyStatus.ACTIVE;
    }

    public boolean isTrialExpired() {
        return trialEndDate != null && trialEndDate.isBefore(LocalDate.now());
    }

    public boolean isActive() {
        return status == CompanyStatus.ACTIVE && !isTrialExpired();
    }

    public void incrementUserCount() {
        this.currentUserCount++;
    }

    public void decrementUserCount() {
        if (this.currentUserCount > 0) {
            this.currentUserCount--;
        }
    }

    public void incrementVehicleCount() {
        this.currentVehicleCount++;
    }

    public void decrementVehicleCount() {
        if (this.currentVehicleCount > 0) {
            this.currentVehicleCount--;
        }
    }

    public double getUtilizationPercentage() {
        if (maxUsers == 0) return 0.0;
        return (currentUserCount.doubleValue() / maxUsers.doubleValue()) * 100.0;
    }
}