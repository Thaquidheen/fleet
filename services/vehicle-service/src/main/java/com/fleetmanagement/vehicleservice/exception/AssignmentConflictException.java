
package com.fleetmanagement.vehicleservice.exception;

import java.util.List;


/**
 * Assignment Conflict Exception
 */
public class AssignmentConflictException extends VehicleServiceException {
    private final List<String> conflicts;

    public AssignmentConflictException(String message, List<String> conflicts) {
        super(message);
        this.conflicts = conflicts;
    }

    public List<String> getConflicts() {
        return conflicts;
    }
}