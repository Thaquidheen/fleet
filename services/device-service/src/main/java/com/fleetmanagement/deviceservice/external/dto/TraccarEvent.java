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
 * Traccar Event DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TraccarEvent {

    private Long id;
    private String type;

    @JsonProperty("deviceId")
    private Long deviceId;

    @JsonProperty("positionId")
    private Long positionId;

    @JsonProperty("geofenceId")
    private Long geofenceId;

    @JsonProperty("maintenanceId")
    private Long maintenanceId;

    @JsonProperty("serverTime")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime serverTime;

    @JsonProperty("attributes")
    private Map<String, Object> attributes;
}
