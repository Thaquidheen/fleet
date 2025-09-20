package com.fleetmanagement.deviceservice.external.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;


/**
 * Traccar Server Info DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TraccarServerInfo {

    private Long id;
    private String version;
    private String map;
    private String bingKey;
    private String mapUrl;
    private String coordinateFormat;
    private String timezone;

    @JsonProperty("twelveHourFormat")
    private Boolean twelveHourFormat;

    @JsonProperty("forceSettings")
    private Boolean forceSettings;

    @JsonProperty("attributes")
    private Map<String, Object> attributes;
}
