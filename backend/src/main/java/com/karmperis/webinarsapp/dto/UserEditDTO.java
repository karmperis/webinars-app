package com.karmperis.webinarsapp.dto;

import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO used to update an existing user, no admin use.
 * Validation is applied in two groups (First, Second) so basic null/blank checks run before
 * more expensive/length/format validations.
 *
 * @param firstname   the user's first name (max 100 chars)
 * @param lastname    the user's last name (max 100 chars)
 * @param phoneNumber optional phone number (digits, optional leading '+', 7-15 digits)
 */
@GroupSequence({UserEditDTO.First.class, UserEditDTO.Second.class, UserEditDTO.class})
public record UserEditDTO(
        @NotBlank(message = "The firstname cannot be blank.", groups = First.class)
        @Size(max = 100, message = "The firstname must not exceed 100 characters.", groups = Second.class)
        String firstname,

        @NotBlank(message = "The lastname cannot be blank.", groups = First.class)
        @Size(max = 100, message = "The lastname must not exceed 100 characters.", groups = Second.class)
        String lastname,

        @Size(max = 20, message = "The phone number must not exceed 20 characters.", groups = Second.class)
        @Pattern(
                regexp = "^\\+?[0-9]{7,15}$",
                message = "The phone number must contain only digits, optionally starting with '+' (for international prefix), and be between 7 and 15 digits long.",
                groups = Second.class
        )
        String phoneNumber

) {
    public interface First {
    }

    public interface Second {
    }
}