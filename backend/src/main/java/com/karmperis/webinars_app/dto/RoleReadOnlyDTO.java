package com.karmperis.webinars_app.dto;

import java.util.UUID;

/**
 * Read-only DTO for Role entities.
 * @param uuid the role UUID
 * @param name the role name
 */
public record RoleReadOnlyDTO(
        UUID uuid,
        String name
) {
}
