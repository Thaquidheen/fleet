package com.fleetmanagement.bridgeservice.model.traccar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TraccarDevice {

    private Long id;

    private String name;

    @JsonProperty("uniqueId")
    private String uniqueId;

    private String status;

    @JsonProperty("lastUpdate")
    private Instant lastUpdate;

    @JsonProperty("positionId")
    private Long positionId;

    @JsonProperty("groupId")
    private Long groupId;

    private String phone;

    private String model;

    private String contact;

    private String category;

    private Boolean disabled;

    @JsonProperty("attributes")
    private Map<String, Object> attributes;
}