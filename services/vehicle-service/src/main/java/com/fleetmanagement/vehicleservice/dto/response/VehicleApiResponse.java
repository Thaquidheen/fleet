package com.fleetmanagement.vehicleservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Vehicle API Response wrapper (renamed to avoid Swagger annotation conflict)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VehicleApiResponse<T> {

    private boolean success;
    private T data;
    private String message;
    private LocalDateTime timestamp;
    private String errorCode;
    private String errorDetails;
    private List<String> errors;
    private String path;
    private int status;

    // Constructors
    public VehicleApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public VehicleApiResponse(boolean success, T data, String message) {
        this();
        this.success = success;
        this.data = data;
        this.message = message;
    }

    // Manual Builder Pattern
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private boolean success = true;
        private T data;
        private String message;
        private LocalDateTime timestamp = LocalDateTime.now();
        private String errorCode;
        private String errorDetails;
        private List<String> errors;
        private String path;
        private int status;

        public Builder<T> success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder<T> data(T data) {
            this.data = data;
            return this;
        }

        public Builder<T> message(String message) {
            this.message = message;
            return this;
        }

        public Builder<T> timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder<T> errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public Builder<T> errorDetails(String errorDetails) {
            this.errorDetails = errorDetails;
            return this;
        }

        public Builder<T> errors(List<String> errors) {
            this.errors = errors;
            return this;
        }

        public Builder<T> path(String path) {
            this.path = path;
            return this;
        }

        public Builder<T> status(int status) {
            this.status = status;
            return this;
        }

        public VehicleApiResponse<T> build() {
            VehicleApiResponse<T> response = new VehicleApiResponse<>();
            response.success = this.success;
            response.data = this.data;
            response.message = this.message;
            response.timestamp = this.timestamp;
            response.errorCode = this.errorCode;
            response.errorDetails = this.errorDetails;
            response.errors = this.errors;
            response.path = this.path;
            response.status = this.status;
            return response;
        }
    }

    /**
     * Create a successful response with data and message
     */
    public static <T> VehicleApiResponse<T> success(T data, String message) {
        return VehicleApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }

    /**
     * Create a successful response with only data
     */
    public static <T> VehicleApiResponse<T> success(T data) {
        return success(data, "Operation completed successfully");
    }

    /**
     * Create a successful response with only message
     */
    public static <T> VehicleApiResponse<T> success(String message) {
        return VehicleApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    /**
     * Create an error response
     */
    public static <T> VehicleApiResponse<T> error(String message) {
        return VehicleApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}