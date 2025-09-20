package com.fleetmanagement.deviceservice.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Error response wrapper
 * 
 * Provides detailed error information including error code,
 * message, details, and validation errors.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    private String errorCode;
    private String message;
    private String details;
    private List<ValidationError> validationErrors;
    private String path;
    private LocalDateTime timestamp;
    private String traceId;

    /**
     * Validation error details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private Object rejectedValue;
        private String message;
    }

    /**
     * Create error response with basic information
     * 
     * @param errorCode error code
     * @param message error message
     * @return error response
     */
    public static ErrorResponse of(String errorCode, String message) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create error response with details
     * 
     * @param errorCode error code
     * @param message error message
     * @param details error details
     * @return error response
     */
    public static ErrorResponse of(String errorCode, String message, String details) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create validation error response
     * 
     * @param errorCode error code
     * @param message error message
     * @param validationErrors validation errors
     * @return error response
     */
    public static ErrorResponse of(String errorCode, String message, List<ValidationError> validationErrors) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .validationErrors(validationErrors)
                .timestamp(LocalDateTime.now())
                .build();
    }
}

