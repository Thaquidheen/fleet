
package com.fleetmanagement.deviceservice.exception;

/**
 * Company Service Exception
 */
public class CompanyServiceException extends ExternalServiceException {
    public CompanyServiceException(String message) {
        super(message);
    }

    public CompanyServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}