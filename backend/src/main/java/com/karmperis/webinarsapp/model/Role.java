package com.karmperis.webinarsapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * JPA entity representing a role with a unique name and associated capabilities.
 * Extends {@link AbstractUuidEntity} to inherit UUID, auditing timestamps and
 * soft-delete support.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role extends AbstractUuidEntity {

    @Column(nullable = false, length = 50)
    private String name;

    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.NONE)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "roles_capabilities",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "capability_id")
    )
    private Set<Capability> capabilities = new HashSet<>();

    /**
     * Return an unmodifiable set of this role's capabilities.
     *
     * @return an immutable copy of the capabilities set
     */
    public Set<Capability> getAllCapabilities() {
        return Set.copyOf(capabilities);
    }

    /**
     * Checks whether this role already contains the given capability.
     *
     * @param capability capability to check
     * @return true if the capability is already assigned to this role
     */
    public boolean hasCapability(Capability capability) {
        return capabilities.contains(capability);
    }

    /**
     * Add a capability to this role.
     *
     * @param capability capability to add
     */
    public void addCapability(Capability capability) {
        capabilities.add(capability);
        capability.getRoles().add(this);
    }

    /**
     * Remove a capability from this role.
     *
     * @param capability capability to remove
     */
    public void removeCapability(Capability capability) {
        capabilities.remove(capability);
        capability.getRoles().remove(this);
    }
}