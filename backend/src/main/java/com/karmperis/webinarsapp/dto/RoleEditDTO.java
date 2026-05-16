package com.karmperis.webinarsapp.dto;

import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO used to edit an existing Role.
 * @param name the existing role name (must be non-blank and contain between 4 and 50 characters)
 */
@GroupSequence({RoleEditDTO.First.class, RoleEditDTO.Second.class, RoleEditDTO.class})
public record RoleEditDTO(
        @NotBlank(message = "The role name cannot be blank.", groups = RoleEditDTO.First.class)
        @Size(min = 4, max = 50, message = "The role name must contain between 4 and 50 characters.", groups = RoleEditDTO.Second.class)
        String name
) {
        public interface First {}
        public interface Second {}
}
