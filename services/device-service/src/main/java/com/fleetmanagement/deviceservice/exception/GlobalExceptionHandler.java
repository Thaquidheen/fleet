package com.fleetmanagement.deviceservice.exception;

import com.fleetmanagement.deviceservice.dto.common.ApiResponse;
import com.fleetmanagement.deviceservice.dto.common.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.List;


/**
 * Global exception handler for the Device Service
 * 
 * Handles all exceptions thrown by the service and provides
 * consistent error responses to clients.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle device not found exceptions
     */
    @ExceptionHandler(DeviceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleDeviceNotFoundException(
            DeviceNotFoundException ex, WebRequest request) {
        log.warn("Device not found: {}", ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .errorCode("DEVICE_NOT_FOUND")
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handle device already exists exceptions
     */
    @ExceptionHandler(DeviceAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleDeviceAlreadyExistsException(
            DeviceAlreadyExistsException ex, WebRequest request) {
        log.warn("Device already exists: {}", ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .errorCode("DEVICE_ALREADY_EXISTS")
                .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Handle validation exceptions
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("Validation error: {}", ex.getMessage());
        
        List<ErrorResponse.ValidationError> validationErrors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            validationErrors.add(ErrorResponse.ValidationError.builder()
                    .field(error.getField())
                    .rejectedValue(error.getRejectedValue())
                    .message(error.getDefaultMessage())
                    .build());
        }
        
        ErrorResponse errorResponse = ErrorResponse.of(
                "VALIDATION_ERROR",
                "Validation failed",
                validationErrors
        );
        
        ApiResponse<ErrorResponse> response = ApiResponse.<ErrorResponse>builder()
                .success(false)
                .message("Validation failed")
                .data(errorResponse)
                .errorCode("VALIDATION_ERROR")
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .errorCode("INVALID_ARGUMENT")
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle generic runtime exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        log.error("Runtime exception occurred: {}", ex.getMessage(), ex);
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message("An unexpected error occurred")
                .errorCode("INTERNAL_ERROR")
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Handle generic exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex, WebRequest request) {
        log.error("Unexpected exception occurred: {}", ex.getMessage(), ex);
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message("An unexpected error occurred")
                .errorCode("INTERNAL_ERROR")
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

