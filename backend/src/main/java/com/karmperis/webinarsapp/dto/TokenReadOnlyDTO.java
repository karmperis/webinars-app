package com.karmperis.webinarsapp.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Read-only DTO that represents an authentication or verification token.
 * This record carries the token value and a few metadata properties that are useful
 * for clients or services that need to display or verify token information without
 * exposing or modifying the underlying persistence model.
 *
 * @param token      the token string value (for example a JWT or random token)
 * @param type       the token type or purpose (for example "Bearer", "RESET_PASSWORD", etc.)
 * @param used       whether the token has already been consumed/used
 * @param expiryDate the instant when the token becomes invalid
 */
public record TokenReadOnlyDTO(
        String token,
        String type,
        Boolean used,
        Instant expiryDate,
        UUID userUuid
) {
}