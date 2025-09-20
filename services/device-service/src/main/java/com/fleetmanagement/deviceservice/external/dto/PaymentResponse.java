
package com.fleetmanagement.deviceservice.external.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

// ===== PAYMENT SERVICE DTOs =====

/**
 * Payment Response DTO from Payment Service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResponse {

    private UUID id;
    private UUID companyId;
    private String paymentType;
    private Double amount;
    private String currency;
    private String status;
    private String description;

    // Payment method
    private String paymentMethod;
    private String transactionId;
    private String referenceNumber;

    // Billing period
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime billingPeriodStart;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime billingPeriodEnd;

    // Payment details
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime paymentDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dueDate;

    // Invoice information
    private String invoiceNumber;
    private String invoiceUrl;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}