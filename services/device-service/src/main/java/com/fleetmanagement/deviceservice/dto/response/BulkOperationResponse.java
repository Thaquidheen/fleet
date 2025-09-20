package com.fleetmanagement.deviceservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Bulk Operation Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BulkOperationResponse {

    private String operationType;
    private Integer totalItems;
    private Integer successfulItems;
    private Integer failedItems;
    private List<BulkOperationResult> results;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime executedAt;

    private String executedBy;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkOperationResult {
        private UUID itemId;
        private String itemIdentifier;
        private Boolean success;
        private String errorMessage;
        private String details;
    }
}
