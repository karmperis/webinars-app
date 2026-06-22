package com.karmperis.webinarsapp.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload used to authenticate a user.
 *
 * @param username the account username
 * @param password the account password
 */
public record AuthenticationRequestDTO(
        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Password is required")
        String password
) {
}