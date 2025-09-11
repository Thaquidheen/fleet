package com.fleetmanagement.vehicleservice.exception;

import com.fleetmanagement.vehicleservice.dto.response.VehicleApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(VehicleNotFoundException.class)
    public ResponseEntity<VehicleApiResponse<Void>> handleVehicleNotFound(VehicleNotFoundException ex) {
        logger.warn("Vehicle not found: {}", ex.getMessage());

        VehicleApiResponse<Void> response = VehicleApiResponse.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(VehicleLimitExceededException.class)
    public ResponseEntity<VehicleApiResponse<Void>> handleVehicleLimitExceeded(VehicleLimitExceededException ex) {
        logger.warn("Vehicle limit exceeded: {}", ex.getMessage());

        VehicleApiResponse<Void> response = VehicleApiResponse.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(DriverNotAvailableException.class)
    public ResponseEntity<VehicleApiResponse<Void>> handleDriverNotAvailable(DriverNotAvailableException ex) {
        logger.warn("Driver not available: {}", ex.getMessage());

        VehicleApiResponse<Void> response = VehicleApiResponse.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(VehicleAssignmentConflictException.class)
    public ResponseEntity<VehicleApiResponse<Void>> handleAssignmentConflict(VehicleAssignmentConflictException ex) {
        logger.warn("Assignment conflict: {}", ex.getMessage());

        VehicleApiResponse<Void> response = VehicleApiResponse.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<VehicleApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        logger.warn("Validation error: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        VehicleApiResponse<Map<String, String>> response = VehicleApiResponse.<Map<String, String>>builder()
                .success(false)
                .data(errors)
                .message("Validation failed")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<VehicleApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        logger.warn("Illegal argument: {}", ex.getMessage());

        VehicleApiResponse<Void> response =VehicleApiResponse.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<VehicleApiResponse<Void>> handleGenericException(Exception ex) {
        logger.error("Unexpected error occurred", ex);

        VehicleApiResponse<Void> response = VehicleApiResponse.<Void>builder()
                .success(false)
                .message("An unexpected error occurred")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}