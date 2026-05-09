package com.karmperis.webinarsapp.dto;

/**
 * DTO used to return a standardized error response.
 *
 * @param code application-specific error code
 * @param message readable error message
 */
public record ErrorResponseDTO(String code, String message) {

    /**
     * Constructor that creates an error response with an empty description.
     * @param code application-specific error code
     */
    public ErrorResponseDTO(String code) {
        this(code, "");
    }
}