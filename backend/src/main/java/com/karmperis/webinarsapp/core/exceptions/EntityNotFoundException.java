package com.karmperis.webinarsapp.core.exceptions;

/**
 * Exception thrown when an expected entity cannot be found.
 */
public class EntityNotFoundException extends AppGenericException {
    private static final String DEFAULT_CODE = "NotFound";

    /**
     * Create a new exception for a "not found" error.
     * @param code    base application error code/prefix
     * @param message readable error message
     */
    public EntityNotFoundException(String code, String message) {
        super(code + DEFAULT_CODE, message);
    }
}