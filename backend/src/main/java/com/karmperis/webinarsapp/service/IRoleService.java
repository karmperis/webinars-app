package com.karmperis.webinarsapp.service;

import com.karmperis.webinarsapp.core.exceptions.EntityAlreadyExistsException;
import com.karmperis.webinarsapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.webinarsapp.core.exceptions.EntityNotFoundException;
import com.karmperis.webinarsapp.dto.CapabilityReadOnlyDTO;
import com.karmperis.webinarsapp.dto.RoleEditDTO;
import com.karmperis.webinarsapp.dto.RoleInsertDTO;
import com.karmperis.webinarsapp.dto.RoleReadOnlyDTO;

import java.util.List;
import java.util.UUID;

/**
 * Service contract for managing roles.
 */
public interface IRoleService {

    /**
     * Create and persist a new role. (Create)
     *
     * @param dto data used to create the role
     * @return a read-only representation of the persisted role
     * @throws EntityAlreadyExistsException   if a non-deleted role with the same name already exists
     * @throws EntityInvalidArgumentException if the provided role data is invalid
     */
    RoleReadOnlyDTO saveRole(RoleInsertDTO dto) throws EntityAlreadyExistsException, EntityInvalidArgumentException;

    /**
     * Retrieve all non-deleted roles ordered by name. (ReadAll)
     *
     * @return list of roles sorted by name
     */
    List<RoleReadOnlyDTO> findAllRolesSortedByName();

    /**
     * Retrieve a non-deleted role by its UUID. (ReadOne)
     *
     * @param uuid role UUID
     * @return the matching role
     * @throws EntityNotFoundException if no non-deleted role with the given UUID exists
     */
    RoleReadOnlyDTO findRoleByUuid(UUID uuid) throws EntityNotFoundException, EntityInvalidArgumentException;

    /**
     * Retrieve all capabilities assigned to a non-deleted role.
     *
     * @param roleUuid role UUID
     * @return list of capabilities assigned to the role
     * @throws EntityNotFoundException if no non-deleted role with the given UUID exists
     */
    List<CapabilityReadOnlyDTO> findCapabilitiesByRoleUuid(UUID roleUuid) throws EntityNotFoundException, EntityInvalidArgumentException;

    /**
     * Update an existing role. (Update)
     *
     * @param uuid the UUID of the role to update
     * @param dto  the data to apply
     * @return a read-only representation of the updated role
     * @throws EntityNotFoundException        if no non-deleted role with the given UUID exists
     * @throws EntityAlreadyExistsException   if the new name is already taken by another non-deleted role
     * @throws EntityInvalidArgumentException if the provided role data is invalid
     */
    RoleReadOnlyDTO updateRole(UUID uuid, RoleEditDTO dto)
            throws EntityNotFoundException, EntityAlreadyExistsException, EntityInvalidArgumentException;

    /**
     * Soft-delete a role by setting its deleted timestamp. (Delete)
     *
     * @param uuid role UUID
     * @throws EntityNotFoundException if no non-deleted role with the given UUID exists
     */
    void softDeleteRoleByUuid(UUID uuid) throws EntityNotFoundException, EntityInvalidArgumentException;

    /**
     * Assign a capability to a role.
     *
     * @param roleUuid       the role UUID
     * @param capabilityUuid the capability UUID
     * @throws EntityNotFoundException      if the role or capability does not exist or is soft-deleted
     * @throws EntityAlreadyExistsException if the capability is already assigned to the role
     */
    void assignCapabilityToRole(UUID roleUuid, UUID capabilityUuid) throws EntityNotFoundException, EntityAlreadyExistsException, EntityInvalidArgumentException;

    /**
     * Remove a capability from a role.
     *
     * @param roleUuid       the role UUID
     * @param capabilityUuid the capability UUID
     * @throws EntityNotFoundException        if the role or capability does not exist or is soft-deleted
     * @throws EntityInvalidArgumentException if the capability is not assigned to the role
     */
    void removeCapabilityFromRole(UUID roleUuid, UUID capabilityUuid)
            throws EntityNotFoundException, EntityInvalidArgumentException;
}