package com.fleetmanagement.companyservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
public class BulkUserUpdateRequest {

    @NotNull(message = "Company ID is required")
    private UUID companyId;

    @NotEmpty(message = "Users list cannot be empty")
    @Valid
    private List<UpdateUserRequest> users;

    private UUID updatedBy;
    private String bulkOperationId;
    private String reason; // Reason for bulk update
}