package com.karmperis.webinarsapp.repository;

import com.karmperis.webinarsapp.dto.WebinarReportView;
import com.karmperis.webinarsapp.model.User;
import com.karmperis.webinarsapp.model.Webinar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link Webinar} entities.
 */
public interface WebinarRepository extends JpaRepository<Webinar, Long> {
    /**
     * Find a non-deleted webinar by its title.
     * Eagerly fetches the organizing {@code user}, including their {@code userDetail} and {@code role}.
     *
     * @param title webinar title
     * @return an Optional containing the webinar if found and not soft-deleted
     */
    @EntityGraph(attributePaths = {"user", "user.userDetail", "user.role"})
    Optional<Webinar> findByTitleAndDeletedAtIsNull(String title);

    /**
     * Find a non-deleted webinar by its UUID.
     * Eagerly fetches the organizing {@code user}, including their {@code userDetail} and {@code role}.
     *
     * @param uuid webinar UUID
     * @return an Optional containing the webinar if found and not soft-deleted
     */
    @EntityGraph(attributePaths = {"user", "user.userDetail", "user.role"})
    Optional<Webinar> findByUuidAndDeletedAtIsNull(UUID uuid);

    /**
     * Return a paginated list of all non-deleted webinars.
     * Eagerly fetches the organizing {@code user}, including their {@code userDetail} and {@code role}.
     *
     * @param pageable pagination and sorting instructions
     * @return a page of active webinars
     */
    @EntityGraph(attributePaths = {"user", "user.userDetail", "user.role"})
    Page<Webinar> findAllByDeletedAtIsNull(Pageable pageable);

    /**
     * Return a paginated list of non-deleted webinars organized by a specific user.
     * Eagerly fetches the organizing {@code user}, including their {@code userDetail} and {@code role}.
     *
     * @param user     the organizing user
     * @param pageable pagination and sorting instructions
     * @return a page of webinars belonging to the user
     */
    @EntityGraph(attributePaths = {"user", "user.userDetail", "user.role"})
    Page<Webinar> findAllByUserAndDeletedAtIsNull(User user, Pageable pageable);

    /**
     * Check whether a non-deleted webinar exists for the given webinar UUID and organizer user UUID.
     *
     * @param webinarUuid the webinar UUID
     * @param userUuid    the organizer user UUID
     * @return {@code true} if a matching non-deleted webinar exists
     */
    boolean existsByUuidAndUserUuidAndDeletedAtIsNull(UUID webinarUuid, UUID userUuid);

    @Query(value = """
            SELECT
                w.title AS webinarTitle,
                u.username AS organizerUsername,
                ud.firstname AS organizerFirstName,
                ud.lastname AS organizerLastName,
                COUNT(uw.user_id) AS totalParticipants
            FROM webinars w
            INNER JOIN users u ON w.user_id = u.id
            INNER JOIN users_details ud ON u.id = ud.user_id
            LEFT JOIN users_webinars uw ON w.id = uw.webinar_id
            WHERE w.deleted_at IS NULL
            GROUP BY w.id, w.title, u.username, ud.firstname, ud.lastname
            ORDER BY totalParticipants DESC
            """, nativeQuery = true)
    List<WebinarReportView> findWebinarsPopularityReport();

    @Query(value = """
            SELECT
                u.username AS organizerUsername,
                ud.firstname AS organizerFirstName,
                ud.lastname AS organizerLastName,
                COUNT(w.id) AS totalWebinars,
                SUM(w.duration) AS totalDuration
            FROM users u
            INNER JOIN users_details ud ON u.id = ud.user_id
            INNER JOIN webinars w ON u.id = w.user_id
            WHERE w.deleted_at IS NULL
            GROUP BY u.id, u.username, ud.firstname, ud.lastname
            HAVING COUNT(w.id) >= 4
            ORDER BY totalDuration DESC
            """, nativeQuery = true)
    List<WebinarReportView> findProductiveOrganizersReport();

    @Query(value = """
              SELECT
                  w.title AS webinarTitle,
                  CASE
                      WHEN w.deleted_at IS NOT NULL THEN N'ΔΙΕΓΡΑΜΜΕΝΟ'
                      ELSE N'ΕΝΕΡΓΟ'
                  END AS webinarStatus,
                  u.username AS organizerUsername,
                  ud.firstname AS organizerFirstName,
                  ud.lastname AS organizerLastName,
                  CASE
                      WHEN u.deleted_at IS NOT NULL THEN N'ΔΙΕΓΡΑΜΜΕΝΟΣ'
                      WHEN u.active = 0 THEN N'ΑΝΕΝΕΡΓΟΣ'
                      ELSE N'ΕΝΕΡΓΟΣ'
                  END AS userStatus
              FROM webinars w
              INNER JOIN users u ON w.user_id = u.id
              INNER JOIN users_details ud ON ud.user_id = u.id
              WHERE w.deleted_at IS NOT NULL
                  OR u.active = 0
                  OR u.deleted_at IS NOT NULL
              ORDER BY w.deleted_at DESC, u.username
            """, nativeQuery = true)
    List<WebinarReportView> findInactiveRecordsReport();
}