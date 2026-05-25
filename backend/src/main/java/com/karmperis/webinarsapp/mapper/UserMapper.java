package com.karmperis.webinarsapp.mapper;

import com.karmperis.webinarsapp.dto.UserEditDTO;
import com.karmperis.webinarsapp.dto.UserInsertDTO;
import com.karmperis.webinarsapp.dto.UserReadOnlyDTO;
import com.karmperis.webinarsapp.model.User;
import com.karmperis.webinarsapp.model.UserDetail;
import org.springframework.stereotype.Component;

/**
 * Mapper component responsible for converting between user-related DTOs and domain entities.
 */
@Component
public class UserMapper {

    /**
     * Map a {@link UserInsertDTO} to a new {@link User} entity instance. (Insert)
     * The returned entity is not persisted; the caller should handle saving and any
     * additional business logic (for example hashing the password).
     *
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

    /**
     * Map a {@link User} entity to a {@link UserReadOnlyDTO} suitable for API responses. (ReadOnly)
     * The method safely handles a {@code null} input and missing related objects (role, userDetail).
     *
     * @param user the user entity to map
     * @return a {@link UserReadOnlyDTO} populated from the entity, or {@code null} if the input is {@code null}
     */
    public UserReadOnlyDTO mapToUserReadOnlyDTO(User user) {
        if (user == null) return null;

        String firstname = null;
        String lastname = null;
        String phoneNumber = null;

        if (user.getUserDetail() != null) {
            firstname = user.getUserDetail().getFirstname();
            lastname = user.getUserDetail().getLastname();
            phoneNumber = user.getUserDetail().getPhoneNumber();
        }

        Long roleId = (user.getRole() != null) ? user.getRole().getId() : null;
        String roleName = (user.getRole() != null) ? user.getRole().getName() : null;

        return new UserReadOnlyDTO(
                user.getUuid(),
                user.getUsername(),
                user.getActive(),
                roleId,
                roleName,
                firstname,
                lastname,
                phoneNumber
        );
    }

    /**
     * Applies values from a {@link UserEditDTO} to an existing {@link User} entity and its {@link UserDetail}. (Edit)
     *
     * @param user the User entity to update
     * @param dto  the DTO containing the updated values
     */
    public void mapToUserEditDTO(User user, UserEditDTO dto) {
        if (user == null || dto == null) return;

        if (user.getUserDetail() == null) {
            UserDetail detail = new UserDetail();
            detail.setUser(user);
            user.setUserDetail(detail);
        }

        UserDetail detail = user.getUserDetail();
        detail.setFirstname(dto.firstname());
        detail.setLastname(dto.lastname());
        detail.setPhoneNumber(dto.phoneNumber());
    }
}