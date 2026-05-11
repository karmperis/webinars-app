package com.karmperis.webinarsapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO used to create a new capability.
 * @param name capability name (required)
 * @param description optional capability description
 */
public record CapabilityInsertDTO(
        @NotBlank(message = "The capability name cannot be blank.")
        @Size(min = 4, max = 50, message = "The capability name must contain between 4 and 50 characters.")
        String name,

        @Size(max = 255, message = "The capability description must contain maximum 255 characters.")
        String description
) {
}