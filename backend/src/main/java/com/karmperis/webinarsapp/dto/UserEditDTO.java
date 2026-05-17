package com.karmperis.webinarsapp.dto;

import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO used to update an existing user.
 * Validation is applied in two groups (First, Second) so basic null/blank checks run before
 * more expensive/length/format validations.
 *
 * @param username the user's username (4-50 chars)
 * @param roleId the database id of the assigned role
 * @param active whether the user is active
 * @param firstname the user's first name (max 100 chars)
 * @param lastname the user's last name (max 100 chars)
 * @param phoneNumber optional phone number (digits, optional leading '+', 7-15 digits)
 */
@GroupSequence({UserEditDTO.First.class, UserEditDTO.Second.class, UserEditDTO.class})
public record UserEditDTO(

        @NotNull(message = "The username cannot be null.", groups = First.class)
        @Size(min = 4, max = 50, message = "The username must contain between 4 and 50 characters.",
                groups = Second.class)
        String username,

        @NotNull(message = "The role_id cannot be null.", groups = First.class)
        Long roleId,

        @NotNull(message = "The active status cannot be null.", groups = First.class)
        Boolean active,

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
    public interface First{}
    public interface Second{}
}