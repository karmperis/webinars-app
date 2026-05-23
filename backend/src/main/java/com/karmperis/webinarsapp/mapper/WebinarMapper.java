package com.karmperis.webinarsapp.mapper;

import com.karmperis.webinarsapp.dto.WebinarInsertDTO;
import com.karmperis.webinarsapp.model.Webinar;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mapper component responsible for converting between webinar-related DTOs and domain entities.
 */
@Component
@RequiredArgsConstructor
public class WebinarMapper {
    private final UserMapper usermapper;

    /**
     * Map a {@link WebinarInsertDTO} to a new {@link Webinar} entity instance. (Insert)
     *
     * @param dto the data transfer object containing webinar creation data
     * @return a new Webinar entity populated from the DTO
     */
    public Webinar mapToWebinarEntity(WebinarInsertDTO dto){
        if(dto == null) return null;

        Webinar webinar = new Webinar();
        webinar.setTitle(dto.title());
        webinar.setDescription(dto.description());
        webinar.setScheduledDate(dto.scheduledDate());
        webinar.setDuration(dto.duration());

        return webinar;
    }
}