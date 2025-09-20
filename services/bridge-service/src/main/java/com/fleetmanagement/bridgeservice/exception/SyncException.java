// SyncException.java
package com.fleetmanagement.bridgeservice.exception;

public class SyncException extends RuntimeException {
    public SyncException(String message) {
        super(message);
    }

    public SyncException(String message, Throwable cause) {
        super(message, cause);
    }
}