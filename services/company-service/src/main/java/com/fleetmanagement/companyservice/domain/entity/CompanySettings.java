package com.fleetmanagement.companyservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "company_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CompanySettings {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "setting_key", nullable = false, length = 255)
    private String settingKey;

    @Column(name = "setting_value", nullable = false, columnDefinition = "jsonb")
    private String settingValue;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_encrypted")
    @Builder.Default
    private Boolean isEncrypted = false;

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

    // Add unique constraint
    @Table(uniqueConstraints = {
            @UniqueConstraint(columnNames = {"company_id", "setting_key"})
    })
    public static class CompanySettingsConstraints {}
}