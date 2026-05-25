package com.karmperis.webinarsapp.service;

import com.karmperis.webinarsapp.core.exceptions.EntityAlreadyExistsException;
import com.karmperis.webinarsapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.webinarsapp.core.exceptions.EntityNotFoundException;
import com.karmperis.webinarsapp.dto.WebinarEditDTO;
import com.karmperis.webinarsapp.dto.WebinarInsertDTO;
import com.karmperis.webinarsapp.dto.WebinarReadOnlyDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service contract for managing webinars.
 */
public interface IWebinarService {

    /**
     * Create and persist a new webinar. (Create)
     *
     * @param dto           the DTO containing values for creating a webinar
     * @param organizerUuid the UUID of the organizing user
     * @return the persisted webinar as a read-only DTO
     * @throws EntityAlreadyExistsException   if a non-deleted webinar with the same title already exists
     * @throws EntityInvalidArgumentException if the provided webinar data is invalid
     * @throws EntityNotFoundException        if the organizer specified in the DTO does not exist
     */
    WebinarReadOnlyDTO saveWebinar(WebinarInsertDTO dto, UUID organizerUuid)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException, EntityNotFoundException;

    /**
     * Retrieve a page of non-deleted webinars ordered chronologically. (ReadAll)
     *
     * @param pageable paging and sorting information
     * @return a page of active webinars as read-only DTOs
     */
    Page<WebinarReadOnlyDTO> findAllWebinars(Pageable pageable);

    /**
     * Retrieve a page of non-deleted webinars organized by a specific user.
     *
     * @param organizerUuid the UUID of the organizing user
     * @param pageable      paging and sorting information
     * @return a page of active webinars as read-only DTOs
     * @throws EntityNotFoundException if the organizer does not exist
     */
    Page<WebinarReadOnlyDTO> findAllWebinarsByOrganizer(UUID organizerUuid, Pageable pageable)
            throws EntityNotFoundException;

    /**
     * Retrieve a non-deleted webinar by UUID. (ReadOne)
     *
     * @param uuid webinar UUID
     * @return the matching webinar as a read-only DTO
     * @throws EntityNotFoundException if no non-deleted webinar with the given UUID exists
     */
    WebinarReadOnlyDTO findWebinarByUuid(UUID uuid) throws EntityNotFoundException;

    /**
     * Update an existing webinar. (Update)
     *
     * @param uuid the UUID of the webinar to update
     * @param dto  the data to apply
     * @return the updated webinar as a read-only DTO
     * @throws EntityNotFoundException        if no non-deleted webinar with the given UUID exists
     * @throws EntityAlreadyExistsException   if the update would conflict with an existing non-deleted webinar title
     * @throws EntityInvalidArgumentException if the provided data is invalid
     */
    WebinarReadOnlyDTO updateWebinar(UUID uuid, WebinarEditDTO dto)
            throws EntityNotFoundException, EntityAlreadyExistsException, EntityInvalidArgumentException;

    /**
     * Soft-delete a webinar by setting its deleted timestamp. (Delete)
     *
     * @param uuid the webinar UUID
     * @throws EntityNotFoundException if the webinar does not exist or is already deleted
     */
    void softDeleteWebinarByUuid(UUID uuid) throws EntityNotFoundException;

    /**
     * Enroll a user as a participant in a webinar.
     *
     * @param webinarUuid the UUID of the webinar
     * @param userUuid    the UUID of the user to enroll
     * @throws EntityNotFoundException if either the webinar or the user is not found
     */
    void enrollUserInWebinar(UUID webinarUuid, UUID userUuid) throws EntityNotFoundException;
}