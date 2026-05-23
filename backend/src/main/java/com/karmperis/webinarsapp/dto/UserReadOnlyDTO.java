package com.karmperis.webinarsapp.dto;

import java.util.UUID;

/**
 * A read-only DTO representing a user and their profile information.
 * Safe for exposed API responses as it completely excludes security credentials.
 *
 * @param uuid the unique public identifier of the user
 * @param username the user's login name
 * @param active whether the user account is currently enabled
 * @param roleId the technical ID of the user's role
 * @param roleName the display name of the user's role
 * @param firstname the user's first name
 * @param lastname the user's last name
 * @param phoneNumber the user's phone number
 */
public record UserReadOnlyDTO(
        UUID uuid,
        String username,
        Boolean active,
        Long roleId,
        String roleName,
        String firstname,
        String lastname,
        String phoneNumber
) {
}