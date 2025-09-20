package com.fleetmanagement.bridgeservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(TraccarApiException.class)
    public ResponseEntity<Map<String, Object>> handleTraccarApiException(TraccarApiException ex) {
        log.error("Traccar API error", ex);
        return buildErrorResponse(HttpStatus.BAD_GATEWAY, "Traccar API Error", ex.getMessage());
    }

    @ExceptionHandler(DataTransformationException.class)
    public ResponseEntity<Map<String, Object>> handleDataTransformationException(DataTransformationException ex) {
        log.error("Data transformation error", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Data Transformation Error", ex.getMessage());
    }

    @ExceptionHandler(EventPublishingException.class)
    public ResponseEntity<Map<String, Object>> handleEventPublishingException(EventPublishingException ex) {
        log.error("Event publishing error", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Event Publishing Error", ex.getMessage());
    }

    @ExceptionHandler(DeviceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleDeviceNotFoundException(DeviceNotFoundException ex) {
        log.error("Device not found", ex);
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Device Not Found", ex.getMessage());
    }

    @ExceptionHandler(SyncException.class)
    public ResponseEntity<Map<String, Object>> handleSyncException(SyncException ex) {
        log.error("Sync error", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Synchronization Error", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred");
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String error, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", Instant.now().toString());
        errorResponse.put("status", status.value());
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        errorResponse.put("path", "/bridge");

        return new ResponseEntity<>(errorResponse, status);
    }
}