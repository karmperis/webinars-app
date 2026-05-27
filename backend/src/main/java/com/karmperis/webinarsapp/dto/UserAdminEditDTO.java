package com.karmperis.webinarsapp.dto;

import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotNull;

/**
 * DTO used strictly by Administrators to update user access levels.
 *
 * @param roleId the database id of the role to assign to the user
 * @param active whether the user account should be active (enabled)
 */
@GroupSequence({UserAdminEditDTO.First.class, UserAdminEditDTO.Second.class, UserAdminEditDTO.class})
public record UserAdminEditDTO(
        @NotNull(message = "The role_id cannot be null.", groups = First.class)
        Long roleId,

        @NotNull(message = "The active status cannot be null.", groups = First.class)
        Boolean active
) {
    public interface First {
    }

    public interface Second {
    }
}