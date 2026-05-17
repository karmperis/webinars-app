package com.karmperis.webinarsapp.dto;

import java.util.UUID;

/**
 * Read-only DTO for capability entities.
 *
 * @param uuid capability unique identifier
 * @param name capability name
 * @param description capability description
 */
public record CapabilityReadOnlyDTO(
        UUID uuid,
        String name,
        String description
) {
}