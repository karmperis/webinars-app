package com.karmperis.webinarsapp.repository;

import com.karmperis.webinarsapp.model.User;
import com.karmperis.webinarsapp.model.Webinar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

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
     * Return a paginated list of all soft-deleted webinars.
     * Eagerly fetches the organizing {@code user}, including their {@code userDetail} and {@code role}.
     *
     * @param pageable pagination and sorting instructions
     * @return a page of deleted webinars
     */
    @EntityGraph(attributePaths = {"user", "user.userDetail", "user.role"})
    Page<Webinar> findAllByDeletedAtIsNotNull(Pageable pageable);

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
}