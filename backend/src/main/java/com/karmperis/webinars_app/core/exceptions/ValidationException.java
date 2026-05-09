package com.karmperis.webinars_app.core.exceptions;

import lombok.Getter;
import org.springframework.validation.BindingResult;

/**
 * Exception thrown when data validation fails.
 * It carries the BindingResult which contains details about specific field errors.
 */
@Getter
public class ValidationException extends AppGenericException {
    private static final String DEFAULT_CODE = "ValidationError";
    private final BindingResult bindingResult;

    /**
     * Create a new validation exception.
     * @param code          base application error code
     * @param message       general error message
     * @param bindingResult the Spring BindingResult containing field errors
     */
    public ValidationException(String code, String message, BindingResult bindingResult) {
        super(code + DEFAULT_CODE, message);
        this.bindingResult = bindingResult;
    }
}