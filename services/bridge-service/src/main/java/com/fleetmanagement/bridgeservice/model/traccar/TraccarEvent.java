package com.fleetmanagement.bridgeservice.model.traccar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
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
    private Instant serverTime;

    @JsonProperty("attributes")
    private Map<String, Object> attributes;
}
