package com.fleetmanagement.companyservice.dto.response;

import java.util.List;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public  class BulkValidationResponse {
    private boolean canCreate;
    private int maxAllowed;
    private int currentCount;
    private int requestedCount;
    private int availableSlots;
    private String message;
    private List<String> errors;
}