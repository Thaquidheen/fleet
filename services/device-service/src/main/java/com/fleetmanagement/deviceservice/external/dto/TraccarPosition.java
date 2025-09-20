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
public class TraccarPosition {

    private Long id;
    private String protocol;

    @JsonProperty("deviceId")
    private Long deviceId;

    @JsonProperty("deviceTime")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime deviceTime;

    @JsonProperty("fixTime")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime fixTime;

    @JsonProperty("serverTime")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime serverTime;

    private Boolean valid;
    private Double latitude;
    private Double longitude;
    private Double altitude;
    private Double speed;
    private Double course;
    private String address;
    private Double accuracy;
    private String network;

    @JsonProperty("attributes")
    private Map<String, Object> attributes;
}