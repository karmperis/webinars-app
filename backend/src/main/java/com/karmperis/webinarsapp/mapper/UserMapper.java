package com.karmperis.webinarsapp.mapper;

import com.karmperis.webinarsapp.dto.UserInsertDTO;
import com.karmperis.webinarsapp.model.User;
import com.karmperis.webinarsapp.model.UserDetail;

/**
 * Mapper component responsible for converting between user-related DTOs and domain entities.
 */
public class UserMapper {

    /**
     * Map a {@link UserInsertDTO} to a new {@link User} entity instance.
     * The returned entity is not persisted; the caller should handle saving and any
     * additional business logic (for example hashing the password).
     * @param dto the data transfer object containing user creation data
     * @return a new {@link User} entity populated from the DTO, or {@code null} if the dto is {@code null}
     */
    public User mapToUserEntity(UserInsertDTO dto) {
        if (dto == null) return null;

        User user = new User();
        user.setUsername(dto.username());
        user.setPassword(dto.password());
        user.setActive(false);

        UserDetail detail = new UserDetail();
        detail.setFirstname(dto.firstname());
        detail.setLastname(dto.lastname());
        detail.setPhoneNumber(dto.phoneNumber());
        user.setUserDetail(detail);
        detail.setUser(user);

        return user;
    }
}