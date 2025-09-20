package com.fleetmanagement.deviceservice.external.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

// ===== TRACCAR API DTOs =====

/**
 * Traccar Device DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TraccarDevice {

    private Long id;
    private String name;
    private String uniqueId;
    private String status;
    private String phone;
    private String model;
    private String contact;
    private String category;

    @JsonProperty("lastUpdate")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime lastUpdate;

    @JsonProperty("positionId")
    private Long positionId;

    @JsonProperty("groupId")
    private Long groupId;

    @JsonProperty("disabled")
    private Boolean disabled;

    @JsonProperty("expirationTime")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime expirationTime;

    @JsonProperty("attributes")
    private Map<String, Object> attributes;
}
