package com.karmperis.webinarsapp.dto;

import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO used to create a new capability.
 * Validation is applied in two groups (First, Second) so basic blank checks run before
 * more expensive/length/format validations.
 *
 * @param name capability name (required)
 * @param description optional capability description
 */
@GroupSequence({CapabilityInsertDTO.First.class, CapabilityInsertDTO.Second.class, CapabilityInsertDTO.class})
public record CapabilityInsertDTO(
        @NotBlank(message = "The capability name cannot be blank.", groups = First.class)
        @Size(min = 4, max = 50, message = "The capability name must contain between 4 and 50 characters.", groups = Second.class)
        String name,

        @Size(max = 255, message = "The capability description must contain maximum 255 characters.")
        String description
) {
        public interface First {}
        public interface Second {}
}