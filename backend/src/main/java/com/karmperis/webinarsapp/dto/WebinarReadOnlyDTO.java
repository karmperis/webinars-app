package com.karmperis.webinarsapp.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO representing a read-only view of a Webinar.
 *
 * @param uuid          the unique public identifier of the webinar
 * @param title         the webinar title
 * @param description   the webinar description
 * @param scheduledDate the scheduled date and time of the event
 * @param duration      the duration of the webinar in minutes
 * @param organizer     the read-only details of the organizing user
 */
public record WebinarReadOnlyDTO(
        UUID uuid,
        String title,
        String description,
        Instant scheduledDate,
        Integer duration,
        UserReadOnlyDTO organizer
) {
}