package com.karmperis.webinars_app.service;

import com.karmperis.webinars_app.dto.RoleEditDTO;
import com.karmperis.webinars_app.dto.RoleInsertDTO;
import com.karmperis.webinars_app.dto.RoleReadOnlyDTO;

import java.util.List;
import java.util.UUID;

/**
 * Service contract for managing roles.
 */
public interface IRoleService {

    /**
     * Create and persist a new role. (Create)
     * @param dto data used to create the role
     * @return a read-only representation of the persisted role
     */
    RoleReadOnlyDTO saveRole(RoleInsertDTO dto);

    /**
     * Retrieve all non-deleted roles ordered by name. (ReadAll)
     * @return list of roles sorted by name
     */
    List<RoleReadOnlyDTO> findAllRolesSortedByName();

    /**
     * Retrieve a non-deleted role by its UUID. (ReadOne)
     * @param uuid role UUID
     * @return the matching role
     */
    RoleReadOnlyDTO findRoleByUuid(UUID uuid);

    /**
     * Update an existing role. (Update)
     * @param uuid the UUID of the role to update
     * @param dto  the data to apply
     */

    void updateRole(UUID uuid, RoleEditDTO dto);
    /**
     * Soft-delete a role by setting its deleted timestamp. (Delete)
     * @param uuid role UUID
     */
    void softDeleteRoleByUuid(UUID uuid);
}