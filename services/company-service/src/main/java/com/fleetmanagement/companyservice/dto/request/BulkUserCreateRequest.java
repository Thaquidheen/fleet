// BulkUserCreateRequest.java
package com.fleetmanagement.companyservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class BulkUserCreateRequest {

    @NotNull(message = "Company ID is required")
    private UUID companyId;

    @NotEmpty(message = "Users list cannot be empty")
    @Size(max = 100, message = "Cannot create more than 100 users at once")
    @Valid
    private List<CreateUserRequest> users;

    private boolean sendWelcomeEmail;
    private boolean requireEmailVerification;
    private String defaultPassword; // If set, all users will have this password
    private boolean generateRandomPasswords; // If true, generate random passwords for each user
    private UUID createdBy;
    private String bulkOperationId; // For tracking bulk operations
}