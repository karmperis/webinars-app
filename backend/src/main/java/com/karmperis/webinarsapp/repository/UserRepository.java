package com.karmperis.webinarsapp.repository;

import com.karmperis.webinarsapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link User} entities.
 * Provides methods to find active (non-deleted) users by UUID or username and to check existence.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Find an active (non-deleted) user by UUID.
     * @param uuid the user's UUID
     * @return an Optional containing the matching user if present and not soft-deleted
     */
    Optional<User> findByUuidAndDeletedAtIsNull(UUID uuid);

    /**
     * Find an active (non-deleted) user by username.
     * @param username the user's username
     * @return an Optional containing the matching user if present and not soft-deleted
     */
    Optional<User> findByUsernameAndDeletedAtIsNull(String username);

    /**
     * Check whether an active (non-deleted) user with the given username exists.
     * @param username the username to check
     * @return {@code true} if an active user with the username exists, otherwise {@code false}
     */
    boolean existsByUsernameAndDeletedAtIsNull(String username);
}