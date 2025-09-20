package com.fleetmanagement.bridgeservice.model.domain;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class LocationData {

    private UUID deviceId;
    private Long traccarDeviceId;
    private UUID companyId;

    // Position data
    private Double latitude;
    private Double longitude;
    private Double altitude;
    private Double speed;
    private Double course;
    private Double accuracy;

    // Timestamps
    private Instant deviceTime;
    private Instant serverTime;
    private Instant processedTime;

    // Status
    private Boolean valid;
    private String address;
    private String protocol;

    // Additional data
    private Double odometer;
    private Boolean ignition;

    // Data quality
    private String source;
    private Integer satelliteCount;
    private Double hdop;
}