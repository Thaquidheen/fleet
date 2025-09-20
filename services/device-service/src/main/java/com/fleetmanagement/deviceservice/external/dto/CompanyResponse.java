
package com.fleetmanagement.deviceservice.external.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyResponse {

    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String subscriptionPlan;
    private Boolean isActive;

    // Device limits
    private Integer maxDevices;
    private Integer maxMobileDevices;
    private Integer currentDeviceCount;
    private Integer currentMobileDeviceCount;

    // Features enabled
    private Boolean sensorSubscriptionsEnabled;
    private Boolean advancedAnalyticsEnabled;
    private Boolean alertsEnabled;
    private Boolean reportingEnabled;
    private Boolean apiAccessEnabled;

    // Billing information
    private Double monthlySubscriptionFee;
    private String billingCycle;
    private String paymentStatus;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime subscriptionStartDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime subscriptionEndDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
