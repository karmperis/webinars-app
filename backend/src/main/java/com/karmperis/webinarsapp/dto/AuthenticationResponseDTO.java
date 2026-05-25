package com.karmperis.webinarsapp.dto;

/**
 * Response payload returned after successful authentication.
 *
 * @param token the issued authentication token
 */
public record AuthenticationResponseDTO(
        String token
) {
}