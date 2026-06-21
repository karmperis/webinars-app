package com.karmperis.webinarsapp.service;

import com.karmperis.webinarsapp.core.exceptions.EntityAlreadyExistsException;
import com.karmperis.webinarsapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.webinarsapp.core.exceptions.EntityNotFoundException;
import com.karmperis.webinarsapp.dto.WebinarEditDTO;
import com.karmperis.webinarsapp.dto.WebinarInsertDTO;
import com.karmperis.webinarsapp.dto.WebinarReadOnlyDTO;
import com.karmperis.webinarsapp.mapper.WebinarMapper;
import com.karmperis.webinarsapp.model.User;
import com.karmperis.webinarsapp.model.Webinar;
import com.karmperis.webinarsapp.repository.UserRepository;
import com.karmperis.webinarsapp.repository.WebinarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Default {@link IWebinarService} implementation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebinarServiceImpl implements IWebinarService {
    private final WebinarRepository webinarRepository;
    private final UserRepository userRepository;
    private final WebinarMapper webinarMapper;

    /**
     * Create and persist a new webinar.
     *
     * @param dto the DTO containing values for creating a webinar
     * @return the persisted webinar as a read-only DTO
     * @throws EntityAlreadyExistsException   if a non-deleted webinar with the same title already exists
     * @throws EntityInvalidArgumentException if the provided webinar data is invalid
     * @throws EntityNotFoundException        if the organizer specified in the DTO does not exist
     */
    @Override
    @Transactional(rollbackFor = {EntityAlreadyExistsException.class, EntityInvalidArgumentException.class, EntityNotFoundException.class})
    @PreAuthorize("hasAuthority('CREATE_WEBINAR') and (#organizerUuid == authentication.principal.uuid or hasRole('ADMIN'))")
    public WebinarReadOnlyDTO saveWebinar(WebinarInsertDTO dto, UUID organizerUuid)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException, EntityNotFoundException {

        // Defensive programming: Guard clause for unit tests and internal calls that bypass Web-layer validation
        if (dto == null) {
            throw new EntityInvalidArgumentException("Webinar", "Webinar data cannot be null");
        }

        // Defensive programming: structural validation enforced at service level even though checked by DTO bean validation
        validateWebinarData(dto.title(), dto.duration());

        log.info("Attempting to save new webinar with title: {}", dto.title());

        try {
            if (webinarRepository.findByTitleAndDeletedAtIsNull(dto.title()).isPresent()) {
                throw new EntityAlreadyExistsException("Webinar", "Webinar with title '" + dto.title() + "' already exists");
            }

            User organizer = userRepository.findByUuidAndDeletedAtIsNull(organizerUuid)
                    .orElseThrow(() -> {
                        log.warn("Failed to create webinar: Organizer with UUID {} not found", organizerUuid);
                        return new EntityNotFoundException("User", "Organizer with UUID " + organizerUuid + " not found");
                    });

            Webinar webinar = webinarMapper.mapToWebinarEntity(dto);
            webinar.setUser(organizer);
            webinar.setUuid(UUID.randomUUID());

            Webinar savedWebinar = webinarRepository.save(webinar);
            log.info("Webinar saved successfully with UUID: {}", savedWebinar.getUuid());

            return webinarMapper.mapToWebinarReadOnlyDTO(savedWebinar);

        } catch (EntityAlreadyExistsException | EntityNotFoundException e) {
            log.warn("Failed to save webinar: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieve a page of non-deleted webinars using the provided paging and sorting information.
     *
     * @param pageable paging and sorting information
     * @return a page of non-deleted webinars as read-only DTOs
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('VIEW_WEBINARS')")
    public Page<WebinarReadOnlyDTO> findAllWebinars(Pageable pageable) {
        log.info("Fetching a page of active webinars");
        return webinarRepository.findAllByDeletedAtIsNull(pageable)
                .map(webinarMapper::mapToWebinarReadOnlyDTO);
    }

    /**
     * Retrieve a page of non-deleted webinars organized by a specific user.
     *
     * @param organizerUuid the UUID of the organizing user
     * @param pageable      paging and sorting information
     * @return a page of non-deleted webinars as read-only DTOs
     * @throws EntityNotFoundException if the organizer does not exist
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('VIEW_WEBINARS') and (#organizerUuid == authentication.principal.uuid or hasRole('ADMIN'))")
    public Page<WebinarReadOnlyDTO> findAllWebinarsByOrganizer(UUID organizerUuid, Pageable pageable) throws EntityNotFoundException {
        log.info("Fetching active webinars for organizer with UUID: {}", organizerUuid);

        User organizer = userRepository.findByUuidAndDeletedAtIsNull(organizerUuid)
                .orElseThrow(() -> {
                    log.warn("Organizer with UUID {} not found", organizerUuid);
                    return new EntityNotFoundException("User", "Organizer with UUID " + organizerUuid + " not found");
                });

        return webinarRepository.findAllByUserAndDeletedAtIsNull(organizer, pageable)
                .map(webinarMapper::mapToWebinarReadOnlyDTO);
    }

    /**
     * Retrieve a non-deleted webinar by UUID.
     *
     * @param uuid webinar UUID
     * @return the matching webinar as a read-only DTO
     * @throws EntityNotFoundException if no non-deleted webinar with the given UUID exists
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('VIEW_WEBINARS')")
    public WebinarReadOnlyDTO findWebinarByUuid(UUID uuid) throws EntityNotFoundException {
        log.info("Searching for webinar with UUID: {}", uuid);
        return webinarRepository.findByUuidAndDeletedAtIsNull(uuid)
                .map(webinarMapper::mapToWebinarReadOnlyDTO)
                .orElseThrow(() -> {
                    log.warn("Webinar with UUID {} not found", uuid);
                    return new EntityNotFoundException("Webinar", "Webinar with UUID " + uuid + " not found");
                });
    }

    /**
     * Update an existing webinar.
     *
     * @param uuid the UUID of the webinar to update
     * @param dto  the data to apply
     * @return the updated webinar as a read-only DTO
     * @throws EntityNotFoundException        if no non-deleted webinar with the given UUID exists
     * @throws EntityAlreadyExistsException   if the update would conflict with an existing non-deleted webinar title
     * @throws EntityInvalidArgumentException if the provided data is invalid
     */
    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class, EntityAlreadyExistsException.class, EntityInvalidArgumentException.class})
    @PreAuthorize("hasAuthority('EDIT_WEBINAR') and (hasRole('ADMIN') or @securityService.isOwnWebinar(#uuid, authentication))")
    public WebinarReadOnlyDTO updateWebinar(UUID uuid, WebinarEditDTO dto) throws EntityNotFoundException, EntityAlreadyExistsException, EntityInvalidArgumentException {
        log.info("Updating webinar with UUID: {}", uuid);

        // Defensive programming: Guard clause for unit tests and internal calls that bypass Web-layer validation
        if (dto == null) {
            throw new EntityInvalidArgumentException("Webinar", "Webinar update data cannot be null");
        }

        // Defensive programming: structural validation enforced at service level even though checked by DTO bean validation
        validateWebinarData(dto.title(), dto.duration());

        Webinar webinar = webinarRepository.findByUuidAndDeletedAtIsNull(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Webinar", "Webinar not found"));

        if (!webinar.getTitle().equalsIgnoreCase(dto.title()) &&
                webinarRepository.findByTitleAndDeletedAtIsNull(dto.title()).isPresent()) {
            throw new EntityAlreadyExistsException("Webinar", "Webinar with title '" + dto.title() + "' already exists");
        }

        webinarMapper.mapToWebinarEditDTO(webinar, dto);
        Webinar updatedWebinar = webinarRepository.save(webinar);
        log.info("Webinar with UUID {} updated successfully", uuid);

        return webinarMapper.mapToWebinarReadOnlyDTO(updatedWebinar);
    }

    /**
     * Soft-delete a webinar by setting its deleted timestamp.
     *
     * @param uuid the webinar UUID
     * @throws EntityNotFoundException if the webinar does not exist or is already deleted
     */
    @Override
    @Transactional(rollbackFor = EntityNotFoundException.class)
    @PreAuthorize("hasAuthority('DELETE_WEBINAR') and (hasRole('ADMIN') or @securityService.isOwnWebinar(#uuid, authentication))")
    public void softDeleteWebinarByUuid(UUID uuid) throws EntityNotFoundException {
        log.info("Performing soft delete for webinar with UUID: {}", uuid);

        Webinar webinar = webinarRepository.findByUuidAndDeletedAtIsNull(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Webinar", "Webinar not found"));

        webinar.softDelete();

        webinarRepository.save(webinar);
        log.info("Webinar with UUID {} soft deleted successfully", uuid);
    }

    /**
     * Enroll a user as a participant in a webinar.
     *
     * @param webinarUuid the UUID of the webinar
     * @param userUuid    the UUID of the user to enroll
     * @throws EntityNotFoundException        if either the webinar or the user is not found
     * @throws EntityAlreadyExistsException   if the user is already enrolled in the webinar
     * @throws EntityInvalidArgumentException if the organizer tries to enroll in their own webinar
     */
    @Override
    @Transactional(rollbackFor = { EntityNotFoundException.class, EntityAlreadyExistsException.class, EntityInvalidArgumentException.class })
    @PreAuthorize("hasAuthority('ENROLL_IN_WEBINAR') and (hasRole('ADMIN') or @securityService.isOwnProfile(#userUuid, authentication))")
    public void enrollUserInWebinar(UUID webinarUuid, UUID userUuid) throws EntityNotFoundException, EntityAlreadyExistsException, EntityInvalidArgumentException {
        log.info("Enrolling user {} in webinar {}", userUuid, webinarUuid);

        Webinar webinar = webinarRepository.findByUuidAndDeletedAtIsNull(webinarUuid)
                .orElseThrow(() -> {
                    log.warn("Enrollment failed: Webinar with UUID {} not found", webinarUuid);
                    return new EntityNotFoundException("Webinar", "Webinar with UUID " + webinarUuid + " not found");
                });

        User user = userRepository.findByUuidAndDeletedAtIsNull(userUuid)
                .orElseThrow(() -> {
                    log.warn("Enrollment failed: User with UUID {} not found", userUuid);
                    return new EntityNotFoundException("User", "User with UUID " + userUuid + " not found");
                });

        if (webinar.getUser().getUuid().equals(user.getUuid())) {
            throw new EntityInvalidArgumentException(
                    "Enrollment",
                    "Organizer cannot enroll in their own webinar"
            );
        }

        if (webinar.hasParticipant(user)) {
            throw new EntityAlreadyExistsException(
                    "Enrollment",
                    "User is already enrolled in this webinar"
            );
        }

        webinar.addParticipant(user);
        webinarRepository.save(webinar);

        log.info("Successfully enrolled user {} in webinar {}", userUuid, webinarUuid);
    }

    /**
     * Helper method for defensive structural validation of webinar data.
     */
    private void validateWebinarData(String title, Integer duration) throws EntityInvalidArgumentException {
        if (title == null || title.isBlank()) {
            throw new EntityInvalidArgumentException("Webinar", "Webinar title cannot be blank");
        }

        int titleLength = title.trim().length();
        if (titleLength < 5 || titleLength > 100) {
            throw new EntityInvalidArgumentException("Webinar", "Webinar title must contain between 5 and 100 characters");
        }

        if (duration == null || duration < 15 || duration > 480) {
            throw new EntityInvalidArgumentException("Webinar", "Webinar duration must be between 15 and 480 minutes");
        }
    }

    /**
     * Retrieve a page of non-deleted webinars where a specific user is enrolled as participant.
     *
     * @param userUuid the UUID of the participant
     * @param pageable paging and sorting information
     * @return a page of non-deleted webinars as read-only DTOs
     * @throws EntityNotFoundException if the participant does not exist
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('VIEW_WEBINARS') and (#userUuid == authentication.principal.uuid or hasRole('ADMIN'))")
    public Page<WebinarReadOnlyDTO> findAllWebinarsByParticipant(UUID userUuid, Pageable pageable)
            throws EntityNotFoundException {
        log.info("Fetching active webinars for participant with UUID: {}", userUuid);

        User participant = userRepository.findByUuidAndDeletedAtIsNull(userUuid)
                .orElseThrow(() -> {
                    log.warn("Participant with UUID {} not found", userUuid);
                    return new EntityNotFoundException("User", "Participant with UUID " + userUuid + " not found");
                });

        return webinarRepository.findAllByParticipantsContainingAndDeletedAtIsNull(participant, pageable)
                .map(webinarMapper::mapToWebinarReadOnlyDTO);
    }
}