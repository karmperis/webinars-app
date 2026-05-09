package com.karmperis.webinars_app.mapper;

import com.karmperis.webinars_app.dto.RoleEditDTO;
import com.karmperis.webinars_app.dto.RoleInsertDTO;
import com.karmperis.webinars_app.dto.RoleReadOnlyDTO;
import com.karmperis.webinars_app.model.Role;
import org.springframework.stereotype.Component;

/**
 * Mapper component responsible for converting between {@link Role} entities and Role-related DTOs.
 */
@Component
public class RoleMapper {
    /**
     * Maps a {@link RoleInsertDTO} to a new {@link Role} entity. (Insert)
     * @param dto the DTO containing values for creating a role
     * @return a new Role entity populated from the DTO
     */
    public Role mapToRoleEntity(RoleInsertDTO dto){
        Role role = new Role();
        role.setName(dto.name());
        return role;
    }

    /**
     * Maps a {@link Role} entity to a {@link RoleReadOnlyDTO}. (ReadOnly)
     * @param role the Role entity to map
     * @return a read-only DTO representation of the given role
     */
    public RoleReadOnlyDTO mapToRoleReadOnlyDTO(Role role){
        return new RoleReadOnlyDTO(role.getUuid(),role.getName());
    }

    /**
     * Applies values from a {@link RoleEditDTO} to an existing {@link Role} entity. (Edit)
     * @param role the Role entity to update
     * @param dto  the DTO containing the updated values
     */
    public void mapToRoleEditDTO(Role role, RoleEditDTO dto){
        role.setName(dto.name());
    }
}