package com.karmperis.webinarsapp.dto;

import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.*;

import java.time.Instant;

/**
 * DTO used to create a new Webinar.
 * Validation is applied in two groups (First, Second) so basic null/blank checks run before
 * more expensive length, format, or temporal validations.
 *
 * @param title         the webinar title (must be non-blank and contain between 5 and 100 characters)
 * @param description   the webinar description (optional, but max 1000 characters if provided)
 * @param scheduledDate the exact date and time of the event (must not be null and must be in the future)
 * @param duration      the duration in minutes (must be positive, between 15 and 480 minutes)
 */
@GroupSequence({WebinarInsertDTO.First.class, WebinarInsertDTO.Second.class, WebinarInsertDTO.class})
public record WebinarInsertDTO(
        @NotBlank(message = "The webinar title cannot be blank.", groups = First.class)
        @Size(min = 5, max = 100, message = "The webinar title must contain between 5 and 100 characters.", groups = Second.class)
        String title,

        @Size(max = 1000, message = "The description cannot exceed 1000 characters.", groups = Second.class)
        String description,

        @NotNull(message = "The scheduled date must be provided.", groups = First.class)
        @Future(message = "The scheduled date must be in the future.", groups = Second.class)
        Instant scheduledDate,

        @NotNull(message = "The duration must be provided.", groups = First.class)
        @Positive(message = "The duration must be a positive number.", groups = Second.class)
        @Min(value = 15, message = "The minimum duration is 15 minutes.", groups = Second.class)
        @Max(value = 480, message = "The maximum duration is 480 minutes.", groups = Second.class)
        Integer duration
) {
    public interface First {
    }

    public interface Second {
    }
}