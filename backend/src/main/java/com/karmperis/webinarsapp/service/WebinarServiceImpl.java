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
     * @throws EntityAlreadyExistsException if a non-deleted webinar with the same title already exists
     * @throws EntityInvalidArgumentException if the provided webinar data is invalid
     * @throws EntityNotFoundException if the organizer specified in the DTO does not exist
     */
    @Override
    @Transactional(rollbackFor = {EntityAlreadyExistsException.class, EntityInvalidArgumentException.class, EntityNotFoundException.class})
    public WebinarReadOnlyDTO saveWebinar(WebinarInsertDTO dto)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException, EntityNotFoundException {
        if (dto == null || dto.title() == null || dto.title().isBlank()) {
            throw new EntityInvalidArgumentException("Webinar", "Webinar title cannot be blank");
        }
        if (dto.organizerUuid() == null) {
            throw new EntityInvalidArgumentException("Webinar", "Organizer UUID cannot be null");
        }

        log.info("Attempting to save new webinar with title: {}", dto.title());

        /// Defensive programming: structural validation enforced at service level even though checked by DTO bean validation
        int titleLength = dto.title().trim().length();
        if (titleLength < 5 || titleLength > 100) {
            throw new EntityInvalidArgumentException("Webinar", "Webinar title must contain between 5 and 100 characters");
        }
        if (dto.duration() == null || dto.duration() < 15 || dto.duration() > 480) {
            throw new EntityInvalidArgumentException("Webinar", "Webinar duration must be between 15 and 480 minutes");
        }

        try {
            if (webinarRepository.findByTitleAndDeletedAtIsNull(dto.title()).isPresent()) {
                throw new EntityAlreadyExistsException("Webinar", "Webinar with title '" + dto.title() + "' already exists");
            }

            User organizer = userRepository.findByUuidAndDeletedAtIsNull(dto.organizerUuid())
                    .orElseThrow(() -> {
                        log.warn("Failed to create webinar: Organizer with UUID {} not found", dto.organizerUuid());
                        return new EntityNotFoundException("User", "Organizer with UUID " + dto.organizerUuid() + " not found");
                    });

            Webinar webinar = webinarMapper.mapToWebinarEntity(dto);
            webinar.setUser(organizer);

            Webinar savedWebinar = webinarRepository.save(webinar);
            log.info("Webinar saved successfully with UUID: {}", savedWebinar.getUuid());

            return webinarMapper.mapToWebinarReadOnlyDTO(savedWebinar);

        } catch (EntityAlreadyExistsException | EntityNotFoundException e) {
            log.warn("Failed to save webinar: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieve a page of non-deleted webinars ordered chronologically.
     *
     * @param pageable paging and sorting information
     * @return a page of active webinars as read-only DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public Page<WebinarReadOnlyDTO> findAllWebinars(Pageable pageable) {
        log.info("Fetching a page of active webinars");
        return webinarRepository.findAllByDeletedAtIsNull(pageable)
                .map(webinarMapper::mapToWebinarReadOnlyDTO);
    }

    @Override
    public Page<WebinarReadOnlyDTO> findAllWebinarsByOrganizer(UUID organizerUuid, Pageable pageable) throws EntityNotFoundException {
        return null;
    }

    @Override
    public WebinarReadOnlyDTO findWebinarByUuid(UUID uuid) throws EntityNotFoundException {
        return null;
    }

    @Override
    public WebinarReadOnlyDTO updateWebinar(UUID uuid, WebinarEditDTO dto) throws EntityNotFoundException, EntityAlreadyExistsException, EntityInvalidArgumentException {
        return null;
    }

    @Override
    public void softDeleteWebinarByUuid(UUID uuid) throws EntityNotFoundException {

    }

    @Override
    public void enrollUserInWebinar(UUID webinarUuid, UUID userUuid) throws EntityNotFoundException {

    }
}
