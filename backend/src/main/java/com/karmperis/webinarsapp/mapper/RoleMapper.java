package com.karmperis.webinarsapp.mapper;

import com.karmperis.webinarsapp.dto.RoleEditDTO;
import com.karmperis.webinarsapp.dto.RoleInsertDTO;
import com.karmperis.webinarsapp.dto.RoleReadOnlyDTO;
import com.karmperis.webinarsapp.model.Role;
import org.springframework.stereotype.Component;

/**
 * Mapper component responsible for converting between {@link Role} entities and Role-related DTOs.
 */
@Component
public class RoleMapper {
    /**
     * Maps a {@link RoleInsertDTO} to a new {@link Role} entity. (Insert)
     *
     * @param dto the DTO containing values for creating a role
     * @return a new Role entity populated from the DTO
     */
    public Role mapToRoleEntity(RoleInsertDTO dto) {
        if (dto == null) return null;

        Role role = new Role();
        role.setName(dto.name());

        return role;
    }

    /**
     * Maps a {@link Role} entity to a {@link RoleReadOnlyDTO}. (ReadOnly)
     *
     * @param role the Role entity to map
     * @return a read-only DTO representation of the given role
     */
    public RoleReadOnlyDTO mapToRoleReadOnlyDTO(Role role) {
        if (role == null) return null;
        return new RoleReadOnlyDTO(role.getUuid(), role.getName());
    }

    /**
     * Applies values from a {@link RoleEditDTO} to an existing {@link Role} entity. (Edit)
     *
     * @param role the Role entity to update
     * @param dto  the DTO containing the updated values
     */
    public void mapToRoleEditDTO(Role role, RoleEditDTO dto) {
        if (role == null || dto == null) return;
        role.setName(dto.name());
    }
}