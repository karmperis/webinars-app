package com.karmperis.webinars_app.core.exceptions;

/**
 * Exception thrown when an entity is being created but an entity with the same unique
 * identifier already exists.
 */
public class EntityAlreadyExistsException extends AppGenericException {
    private static final String DEFAULT_CODE = "AlreadyExists";

    /**
     * Create a new exception for an "already exists" error.
     * @param code    base application error code/prefix
     * @param message readable error message
     */
    public EntityAlreadyExistsException(String code, String message) {
        super(code + DEFAULT_CODE, message);
    }
}