// BulkOperationResponse.java
package com.fleetmanagement.companyservice.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkOperationResponse {
    private int successful;
    private int failed;
    private int total;
    private List<String> errors;
    private List<String> warnings;
    private Map<UUID, String> failureReasons;
    private java.time.LocalDateTime processedAt;
    private String operationType;
}
