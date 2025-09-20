// DataTransformationException.java
package com.fleetmanagement.bridgeservice.exception;

public class DataTransformationException extends RuntimeException {
    public DataTransformationException(String message) {
        super(message);
    }

    public DataTransformationException(String message, Throwable cause) {
        super(message, cause);
    }
}