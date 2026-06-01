package com.karmperis.webinarsapp.repository;

import com.karmperis.webinarsapp.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link Role} entities.
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Find a non-deleted role by its name.
     *
     * @param name role name
     * @return an Optional containing the role if found and not soft-deleted
     */
    Optional<Role> findByNameAndDeletedAtIsNull(String name);

    /**
     * Find a non-deleted role by its UUID.
     *
     * @param uuid role UUID
     * @return an Optional containing the role if found and not soft-deleted
     */
    Optional<Role> findByUuidAndDeletedAtIsNull(UUID uuid);

    /**
     * Return all roles ordered by name ascending and not soft-deleted.
     *
     * @return list of roles sorted by name
     */
    List<Role> findAllByDeletedAtIsNullOrderByNameAsc();
}