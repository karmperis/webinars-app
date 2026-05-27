package com.karmperis.webinarsapp.repository;

import com.karmperis.webinarsapp.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link User} entities.
 * Provides methods to find active (non-deleted) users by UUID or username and to check existence.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a page of active (non-deleted) users.
     * Eagerly fetches the associated {@code userDetail} and {@code role} to optimize DTO mapping.
     *
     * @param pageable pagination and sorting information
     * @return a page containing active users (those with {@code deletedAt} == null)
     */
    @EntityGraph(attributePaths = {"userDetail", "role"})
    Page<User> findByDeletedAtIsNull(Pageable pageable);

    /**
     * Find an active (non-deleted) user by UUID.
     * Eagerly fetches the associated {@code userDetail} and {@code role}.
     *
     * @param uuid the user's UUID
     * @return an Optional containing the matching user if present and not soft-deleted
     */
    @EntityGraph(attributePaths = {"userDetail", "role"})
    Optional<User> findByUuidAndDeletedAtIsNull(UUID uuid);

    /**
     * Find an active (non-deleted) user by username.
     * Eagerly fetches the associated {@code userDetail} and {@code role}.
     *
     * @param username the user's username
     * @return an Optional containing the matching user if present and not soft-deleted
     */
    @EntityGraph(attributePaths = {"userDetail", "role"})
    Optional<User> findByUsernameAndDeletedAtIsNull(String username);

    /**
     * Find a user by username for authentication purposes.
     * Eagerly fetches the associated {@code role} and its {@code capabilities} to establish
     * Spring Security granted authorities without triggering lazy initialization exceptions.
     *
     * @param username the user's username
     * @return an Optional containing the matching user if present
     */
    @EntityGraph(attributePaths = {"role", "role.capabilities"})
    Optional<User> findByUsername(String username);

    /**
     * Check whether an active (non-deleted) user with the given username exists.
     *
     * @param username the username to check
     * @return {@code true} if an active user with the username exists, otherwise {@code false}
     */
    boolean existsByUsernameAndDeletedAtIsNull(String username);
}