package com.karmperis.webinarsapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Projection view for aggregated webinar report fields.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface WebinarReportView {
    /**
     * @return the webinar title
     */
    String getWebinarTitle();

    /**
     * @return the organizer username
     */
    String getOrganizerUsername();

    /**
     * @return the organizer first name
     */
    String getOrganizerFirstName();

    /**
     * @return the organizer last name
     */
    String getOrganizerLastName();

    /**
     * @return the total number of participants for the webinar
     */
    Integer getTotalParticipants();

    /**
     * @return the total number of webinars in the report scope
     */
    Integer getTotalWebinars();

    /**
     * @return the total duration of webinars in minutes
     */
    Integer getTotalDuration();

    /**
     * @return the webinar status label
     */
    String getWebinarStatus();

    /**
     * @return the user status label
     */
    String getUserStatus();
}