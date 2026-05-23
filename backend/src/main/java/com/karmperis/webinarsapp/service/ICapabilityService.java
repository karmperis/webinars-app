package com.karmperis.webinarsapp.service;

import com.karmperis.webinarsapp.core.exceptions.EntityAlreadyExistsException;
import com.karmperis.webinarsapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.webinarsapp.core.exceptions.EntityNotFoundException;
import com.karmperis.webinarsapp.dto.CapabilityEditDTO;
import com.karmperis.webinarsapp.dto.CapabilityInsertDTO;
import com.karmperis.webinarsapp.dto.CapabilityReadOnlyDTO;

import java.util.List;
import java.util.UUID;

/**
 * Service contract for managing capabilities.
 */
public interface ICapabilityService {

    /**
     * Create and persist a new capability. (Create)
     *
     * @param dto the DTO containing values for creating a capability
     * @return the persisted capability as a read-only DTO
     * @throws EntityAlreadyExistsException   if a non-deleted capability with the same name already exists
     * @throws EntityInvalidArgumentException if the provided data is invalid
     */
    CapabilityReadOnlyDTO saveCapability(CapabilityInsertDTO dto)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException;

    /**
     * Retrieve all non-deleted capabilities sorted by name. (Read)
     *
     * @return a list of active capabilities
     */
    List<CapabilityReadOnlyDTO> findAllCapabilitiesSortedByName();

    /**
     * Retrieve a non-deleted capability by UUID. (Read)
     *
     * @param uuid the capability UUID
     * @return the matching capability as a read-only DTO
     * @throws EntityNotFoundException if the capability does not exist or is soft-deleted
     */
    CapabilityReadOnlyDTO findCapabilityByUuid(UUID uuid) throws EntityNotFoundException;

    /**
     * Update an existing capability. (Update)
     *
     * @param uuid the capability UUID
     * @param dto  the DTO containing the updated values
     * @return the updated capability as a read-only DTO
     * @throws EntityNotFoundException        if the capability does not exist or is soft-deleted
     * @throws EntityAlreadyExistsException   if the update would conflict with an existing non-deleted capability name
     * @throws EntityInvalidArgumentException if the provided data is invalid
     */
    CapabilityReadOnlyDTO updateCapability(UUID uuid, CapabilityEditDTO dto)
            throws EntityNotFoundException, EntityAlreadyExistsException, EntityInvalidArgumentException;

    /**
     * Soft-delete a capability by setting its deleted timestamp. (Delete)
     *
     * @param uuid the capability UUID
     * @throws EntityNotFoundException if the capability does not exist or is already deleted
     */
    void softDeleteCapabilityByUuid(UUID uuid) throws EntityNotFoundException;
}