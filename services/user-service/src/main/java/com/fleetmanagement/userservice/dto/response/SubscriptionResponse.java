package com.fleetmanagement.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {
    private String plan;
    private int maxUsers;
    private int maxVehicles;
    private boolean unlimited;
    private boolean active;
}