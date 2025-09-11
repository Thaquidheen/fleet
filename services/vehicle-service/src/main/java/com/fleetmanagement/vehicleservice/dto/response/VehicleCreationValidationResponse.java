package com.fleetmanagement.vehicleservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Vehicle Creation Validation Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleCreationValidationResponse {

    private boolean canCreate;
    private String message;
    private int currentVehicleCount;
    private int maxVehicleLimit;
    private int remainingSlots;
    private String subscriptionPlan;
    private boolean requiresUpgrade;
    private String upgradeUrl;
}