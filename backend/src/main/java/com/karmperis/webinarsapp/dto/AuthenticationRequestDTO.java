package com.karmperis.webinarsapp.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request payload used to authenticate a user.
 *
 * @param username the account username
 * @param password the account password
 */
public record AuthenticationRequestDTO(
        @NotNull(message = "Username is required")
        String username,

        @NotNull(message = "Password is required")
        String password
) {
}