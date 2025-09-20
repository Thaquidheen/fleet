package com.fleetmanagement.bridgeservice.model.traccar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TraccarCommand {

    private Long id;

    @JsonProperty("deviceId")
    private Long deviceId;

    private String type;

    private String description;

    @JsonProperty("textChannel")
    private Boolean textChannel;

    @JsonProperty("attributes")
    private Map<String, Object> attributes;

    @JsonProperty("serverTime")
    private Instant serverTime;
}