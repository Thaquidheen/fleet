package com.fleetmanagement.companyservice.dto.request;

import com.fleetmanagement.companyservice.domain.enums.BulkOperationType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkUserOperationRequest {

    @NotNull(message = "Operation type is required")
    private BulkOperationType operation;

    @Valid
    private com.fleetmanagement.companyservice.dto.request.BulkUserCreateRequest createRequest;

    @Valid
    private com.fleetmanagement.companyservice.dto.request.BulkUserUpdateRequest updateRequest;

    private List<UUID> userIds; // For delete operations

    private boolean validateOnly; // If true, only validate without executing
    private boolean skipErrors; // If true, continue processing even if some operations fail
    private String reason; // Reason for bulk operation (for audit)

    // Explicit getter to fix compilation error
    public BulkOperationType getOperation() {
        return operation;
    }
}