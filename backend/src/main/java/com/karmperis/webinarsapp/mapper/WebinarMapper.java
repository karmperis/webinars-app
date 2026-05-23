package com.karmperis.webinarsapp.mapper;

import com.karmperis.webinarsapp.dto.UserReadOnlyDTO;
import com.karmperis.webinarsapp.dto.WebinarEditDTO;
import com.karmperis.webinarsapp.dto.WebinarInsertDTO;
import com.karmperis.webinarsapp.dto.WebinarReadOnlyDTO;
import com.karmperis.webinarsapp.model.Webinar;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mapper component responsible for converting between webinar-related DTOs and domain entities.
 */
@Component
@RequiredArgsConstructor
public class WebinarMapper {
    private final UserMapper userMapper;

    /**
     * Map a {@link WebinarInsertDTO} to a new {@link Webinar} entity instance. (Insert)
     *
     * @param dto the data transfer object containing webinar creation data
     * @return a new Webinar entity populated from the DTO
     */
    public Webinar mapToWebinarEntity(WebinarInsertDTO dto) {
        if (dto == null) return null;

        Webinar webinar = new Webinar();
        webinar.setTitle(dto.title());
        webinar.setDescription(dto.description());
        webinar.setScheduledDate(dto.scheduledDate());
        webinar.setDuration(dto.duration());

        return webinar;
    }

    /**
     * Map a {@link Webinar} entity to a {@link WebinarReadOnlyDTO} suitable for API responses. (ReadOnly)
     * The method safely handles a {@code null} input and utilizes {@link UserMapper} to map the nested organizer.
     *
     * @param webinar the webinar entity to map
     * @return a {@link WebinarReadOnlyDTO} populated from the entity, or {@code null} if the input is {@code null}
     */
    public WebinarReadOnlyDTO mapToWebinarReadOnlyDTO(Webinar webinar) {
        if (webinar == null) return null;

        UserReadOnlyDTO organizerDTO = null;
        if (webinar.getUser() != null) {
            organizerDTO = userMapper.mapToUserReadOnlyDTO(webinar.getUser());
        }
        return new WebinarReadOnlyDTO(
                webinar.getUuid(),
                webinar.getTitle(),
                webinar.getDescription(),
                webinar.getScheduledDate(),
                webinar.getDuration(),
                organizerDTO
        );
    }

    /**
     * Applies values from a {@link WebinarEditDTO} to an existing {@link Webinar} entity. (Edit)
     *
     * @param webinar the Webinar entity to update
     * @param dto     the DTO containing the updated values
     */
    public void mapToWebinarEditDTO(Webinar webinar, WebinarEditDTO dto) {
        if (webinar == null || dto == null) return;

        webinar.setTitle(dto.title());
        webinar.setDescription(dto.description());
        webinar.setScheduledDate(dto.scheduledDate());
        webinar.setDuration(dto.duration());
    }
}