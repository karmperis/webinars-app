package com.karmperis.webinarsapp.service;

import com.karmperis.webinarsapp.core.exceptions.EntityAlreadyExistsException;
import com.karmperis.webinarsapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.webinarsapp.core.exceptions.EntityNotFoundException;
import com.karmperis.webinarsapp.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service contract for managing users.
 */
public interface IUserService {

    /**
     * Create and persist a new user. (Create)
     * @param dto the DTO containing values for creating a user
     * @return the persisted user as a read-only DTO
     * @throws EntityAlreadyExistsException if a non-deleted user with the same username already exists
     * @throws EntityInvalidArgumentException if the provided user data is invalid
     */
    UserReadOnlyDTO saveUser(UserInsertDTO dto) throws EntityAlreadyExistsException, EntityInvalidArgumentException;

    /**
     * Retrieve a page of non-deleted users sorted by name. (ReadAll)
     * @param pageable paging and sorting information
     * @return a page of active users as read-only DTOs
     */
    Page<UserReadOnlyDTO> findAllUsersSortedByName(Pageable pageable);

    /**
     * Retrieve a non-deleted user by UUID. (ReadOne)
     * @param uuid user UUID
     * @return the matching user as a read-only DTO
     * @throws EntityNotFoundException if no non-deleted user with the given UUID exists
     */
    UserReadOnlyDTO findUserByUuid(UUID uuid) throws EntityNotFoundException;

    /**
     * Retrieve a non-deleted user by username. (ReadOne)
     * @param username the username to search for
     * @return the matching user as a read-only DTO
     * @throws EntityNotFoundException if no non-deleted user with the given username exists
     */
    UserReadOnlyDTO findUserByUsername(String username) throws EntityNotFoundException;

    /**
     * Update an existing user. (Update)
     * @param uuid the UUID of the user to update
     * @param dto the data to apply
     * @return the updated user as a read-only DTO
     * @throws EntityNotFoundException if no non-deleted user with the given UUID exists
     * @throws EntityAlreadyExistsException if the update would conflict with an existing non-deleted username
     * @throws EntityInvalidArgumentException if the provided data is invalid
     */
    UserReadOnlyDTO updateUser(UUID uuid, UserEditDTO dto)
            throws EntityNotFoundException, EntityAlreadyExistsException, EntityInvalidArgumentException;

    /**
     * Soft-delete a user by setting its deleted timestamp. (Delete)
     * @param uuid the user UUID
     * @throws EntityNotFoundException if the user does not exist or is already deleted
     */
    void softDeleteUserByUuid(UUID uuid) throws EntityNotFoundException;
}