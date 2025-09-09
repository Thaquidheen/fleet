package com.fleetmanagement.companyservice.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkValidationResponse {
    private boolean canCreate;
    private String message;
    private int currentUsers;
    private int maxUsers;
    private int requestedUsers;
    private int availableSlots;
}