package com.karmperis.webinars_app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO used to create a new Role.
 * @param name the role name (must be non-blank and contain between 4 and 50 characters)
 */
public record RoleInsertDTO(
        @NotBlank(message = "The role name cannot be blank.")
        @Size(min = 4, max = 50, message = "The role name must contain between 4 and 50 characters.")
        String name
) {
}
