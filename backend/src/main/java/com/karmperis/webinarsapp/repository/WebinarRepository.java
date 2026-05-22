package com.karmperis.webinarsapp.repository;

import com.karmperis.webinarsapp.model.User;
import com.karmperis.webinarsapp.model.Webinar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link Webinar} entities.
 */
public interface WebinarRepository extends JpaRepository<Webinar, Long> {
    /**
     * Find a non-deleted webinar by its title.
     *
     * @param title webinar title
     * @return an Optional containing the webinar if found and not soft-deleted
     */
    Optional<Webinar> findByTitleAndDeletedAtIsNull(String title);

    /**
     * Find a non-deleted webinar by its UUID.
     *
     * @param uuid webinar UUID
     * @return an Optional containing the webinar if found and not soft-deleted
     */
    Optional<Webinar> findByUuidAndDeletedAtIsNull(UUID uuid);

    /**
     * Return a paginated list of all non-deleted webinars.
     *
     * @param pageable pagination and sorting instructions
     * @return a page of active webinars
     */
    Page<Webinar> findAllByDeletedAtIsNull(Pageable pageable);

    /**
     * Return a paginated list of all soft-deleted webinars.
     *
     * @param pageable pagination and sorting instructions
     * @return a page of deleted webinars
     */
    Page<Webinar> findAllByDeletedAtIsNotNull(Pageable pageable);

    /**
     * Return a paginated list of non-deleted webinars organized by a specific user.
     *
     * @param user the organizing user
     * @param pageable pagination and sorting instructions
     * @return a page of webinars belonging to the user
     */
    Page<Webinar> findAllByUserAndDeletedAtIsNull(User user, Pageable pageable);
}