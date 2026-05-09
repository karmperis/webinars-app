package com.karmperis.webinars_app.dto;

import java.util.Map;

/**
 * DTO used to return validation errors in a standardized format.
 * @param code application-specific error code
 * @param message readable summary message
 * @param errors map of field names to validation error messages
 */
public record ValidationErrorResponseDTO(
        String code,
        String message,
        Map<String, String> errors) {
}