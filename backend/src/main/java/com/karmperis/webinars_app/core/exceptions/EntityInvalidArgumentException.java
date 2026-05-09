package com.karmperis.webinars_app.core.exceptions;

/**
 * Exception thrown when a provided argument is invalid for a specific entity operation.
 */
public class EntityInvalidArgumentException extends AppGenericException {
    private static final String DEFAULT_CODE = "InvalidArgument";

    /**
     * Create a new exception for an "invalid argument" error.
     * @param code    base application error code/prefix
     * @param message readable error message
     */
    public EntityInvalidArgumentException(String code, String message) {
        super(code + DEFAULT_CODE, message);
    }
}