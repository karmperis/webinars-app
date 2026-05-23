package com.karmperis.webinarsapp.repository;

import com.karmperis.webinarsapp.model.Capability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link Capability} entities.
 */
public interface CapabilityRepository extends JpaRepository<Capability, Long> {

    /**
     * Finds an active (non-deleted) capability by its unique name.
     *
     * @param name the capability name
     * @return the matching capability, if present
     */
    Optional<Capability> findByNameAndDeletedAtIsNull(String name);

    /**
     * Finds an active (non-deleted) capability by UUID.
     *
     * @param uuid the capability UUID
     * @return the matching capability, if present
     */
    Optional<Capability> findByUuidAndDeletedAtIsNull(UUID uuid);

    /**
     * Returns all active (non-deleted) capabilities sorted by name.
     *
     * @return list of active capabilities
     */
    List<Capability> findAllByDeletedAtIsNullOrderByNameAsc();

    /**
     * Returns all soft-deleted capabilities sorted by name.
     *
     * @return list of deleted capabilities
     */
    List<Capability> findAllByDeletedAtIsNotNullOrderByNameAsc();
}