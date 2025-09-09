package com.fleetmanagement.vehicleservice.domain.entity;

import com.fleetmanagement.vehicleservice.domain.enums.DocumentType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Vehicle Document Entity
 *
 * Represents documents associated with vehicles such as registration,
 * insurance, inspection certificates, and other legal documents.
 */
@Entity
@Table(name = "vehicle_documents",
        indexes = {
                @Index(name = "idx_vehicle_documents_vehicle_id", columnList = "vehicle_id"),
                @Index(name = "idx_vehicle_documents_type", columnList = "document_type"),
                @Index(name = "idx_vehicle_documents_expiry", columnList = "expiry_date"),
                @Index(name = "idx_vehicle_documents_active", columnList = "is_active")
        })
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDocument {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    // Vehicle Reference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_document_vehicle"))
    @NotNull(message = "Vehicle is required")
    private Vehicle vehicle;

    @Column(name = "company_id", nullable = false)
    @NotNull(message = "Company ID is required")
    private UUID companyId;

    // Document Information
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    @Column(name = "document_name", nullable = false, length = 255)
    @NotBlank(message = "Document name is required")
    @Size(max = 255, message = "Document name must not exceed 255 characters")
    private String documentName;

    @Column(name = "document_number", length = 100)
    @Size(max = 100, message = "Document number must not exceed 100 characters")
    private String documentNumber;

    @Column(name = "file_path", length = 500)
    @Size(max = 500, message = "File path must not exceed 500 characters")
    private String filePath;

    @Column(name = "file_name", length = 255)
    @Size(max = 255, message = "File name must not exceed 255 characters")
    private String fileName;

    @Column(name = "file_size")
    @Min(value = 0, message = "File size must be non-negative")
    private Long fileSize;

    @Column(name = "mime_type", length = 100)
    @Size(max = 100, message = "MIME type must not exceed 100 characters")
    private String mimeType;

    // Document Validity
    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "issuing_authority", length = 255)
    @Size(max = 255, message = "Issuing authority must not exceed 255 characters")
    private String issuingAuthority;

    // Document Status
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "verification_date")
    private LocalDateTime verificationDate;

    @Column(name = "verified_by")
    private UUID verifiedBy;

    // Additional Information
    @Column(columnDefinition = "TEXT")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Column(length = 500)
    @Size(max = 500, message = "Tags must not exceed 500 characters")
    private String tags;

    // Audit Fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "uploaded_by")
    private UUID uploadedBy;

    // Business Logic Methods

    /**
     * Check if the document is expired
     */
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    /**
     * Check if the document is expiring soon
     */
    public boolean isExpiringSoon(int daysThreshold) {
        if (expiryDate == null) {
            return false;
        }
        return expiryDate.isBefore(LocalDate.now().plusDays(daysThreshold));
    }

    /**
     * Get days until expiry
     */
    public Long getDaysUntilExpiry() {
        if (expiryDate == null) {
            return null;
        }

        LocalDate now = LocalDate.now();
        if (expiryDate.isBefore(now)) {
            return 0L; // Already expired
        }

        return java.time.temporal.ChronoUnit.DAYS.between(now, expiryDate);
    }

    /**
     * Check if document is valid (active and not expired)
     */
    public boolean isValid() {
        return isActive && !isExpired();
    }

    /**
     * Check if document requires renewal
     */
    public boolean requiresRenewal(int daysThreshold) {
        return isExpired() || isExpiringSoon(daysThreshold);
    }

    /**
     * Verify the document
     */
    public void verify(UUID verifiedBy) {
        this.isVerified = true;
        this.verifiedBy = verifiedBy;
        this.verificationDate = LocalDateTime.now();
    }

    /**
     * Unverify the document
     */
    public void unverify() {
        this.isVerified = false;
        this.verifiedBy = null;
        this.verificationDate = null;
    }

    /**
     * Activate the document
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Deactivate the document
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Update file information
     */
    public void updateFileInfo(String fileName, String filePath, Long fileSize, String mimeType) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
    }

    /**
     * Update document validity period
     */
    public void updateValidityPeriod(LocalDate issueDate, LocalDate expiryDate) {
        if (issueDate != null && expiryDate != null && issueDate.isAfter(expiryDate)) {
            throw new IllegalArgumentException("Issue date cannot be after expiry date");
        }
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
    }

    /**
     * Renew the document with new expiry date
     */
    public void renew(LocalDate newExpiryDate, String newDocumentNumber) {
        if (newExpiryDate != null && newExpiryDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("New expiry date cannot be in the past");
        }

        this.expiryDate = newExpiryDate;
        if (newDocumentNumber != null && !newDocumentNumber.trim().isEmpty()) {
            this.documentNumber = newDocumentNumber.trim();
        }

        // Reset verification status for renewed document
        this.isVerified = false;
        this.verifiedBy = null;
        this.verificationDate = null;
    }

    /**
     * Add tags to the document
     */
    public void addTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return;
        }

        if (tags == null || tags.isEmpty()) {
            tags = tag.trim();
        } else {
            // Check if tag already exists
            String[] existingTags = tags.split(",");
            for (String existingTag : existingTags) {
                if (existingTag.trim().equalsIgnoreCase(tag.trim())) {
                    return; // Tag already exists
                }
            }
            tags += "," + tag.trim();
        }
    }

    /**
     * Remove tag from the document
     */
    public void removeTag(String tag) {
        if (tags == null || tag == null) {
            return;
        }

        String[] tagArray = tags.split(",");
        StringBuilder newTags = new StringBuilder();

        for (String existingTag : tagArray) {
            if (!existingTag.trim().equalsIgnoreCase(tag.trim())) {
                if (newTags.length() > 0) {
                    newTags.append(",");
                }
                newTags.append(existingTag.trim());
            }
        }

        tags = newTags.toString();
    }

    /**
     * Check if document has a specific tag
     */
    public boolean hasTag(String tag) {
        if (tags == null || tag == null) {
            return false;
        }

        String[] tagArray = tags.split(",");
        for (String existingTag : tagArray) {
            if (existingTag.trim().equalsIgnoreCase(tag.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get file extension from file name
     */
    public String getFileExtension() {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * Get human-readable file size
     */
    public String getFormattedFileSize() {
        if (fileSize == null) {
            return "Unknown";
        }

        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        }
    }

    /**
     * Check if document is an image
     */
    public boolean isImage() {
        if (mimeType == null) {
            return false;
        }
        return mimeType.startsWith("image/");
    }

    /**
     * Check if document is a PDF
     */
    public boolean isPdf() {
        return "application/pdf".equals(mimeType);
    }

    /**
     * Get document age in days
     */
    public Long getDocumentAgeDays() {
        if (issueDate == null) {
            return null;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(issueDate, LocalDate.now());
    }

    /**
     * Get vehicle ID
     */
    public UUID getVehicleId() {
        return vehicle != null ? vehicle.getId() : null;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = true;
        }
        if (isVerified == null) {
            isVerified = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Validation Methods
    @AssertTrue(message = "Expiry date must be after issue date")
    private boolean isExpiryDateValid() {
        if (issueDate == null || expiryDate == null) {
            return true;
        }
        return !expiryDate.isBefore(issueDate);
    }

    @AssertTrue(message = "File size must be positive if file is present")
    private boolean isFileSizeValid() {
        if (filePath != null && !filePath.trim().isEmpty()) {
            return fileSize != null && fileSize > 0;
        }
        return true;
    }
}