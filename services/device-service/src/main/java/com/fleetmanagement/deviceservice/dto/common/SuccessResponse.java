package com.fleetmanagement.deviceservice.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Success response wrapper
 * 
 * Provides a simple success response format for operations
 * that don't return data but need to confirm success.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SuccessResponse {
    
    private boolean success;
    private String message;
    private LocalDateTime timestamp;
    private Object metadata;

    /**
     * Create a simple success response
     * 
     * @param message success message
     * @return success response
     */
    public static SuccessResponse of(String message) {
        return SuccessResponse.builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create a success response with metadata
     * 
     * @param message success message
     * @param metadata additional metadata
     * @return success response
     */
    public static SuccessResponse of(String message, Object metadata) {
        return SuccessResponse.builder()
                .success(true)
                .message(message)
                .metadata(metadata)
                .timestamp(LocalDateTime.now())
                .build();
    }
}


