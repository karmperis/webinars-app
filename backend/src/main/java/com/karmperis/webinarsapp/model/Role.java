package com.karmperis.webinarsapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * JPA entity representing a role with a unique name and associated capabilities.
 * Extends {@link AbstractUuidEntity} to inherit UUID, auditing timestamps and
 * soft-delete support.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "roles")
public class Role extends AbstractUuidEntity {

    @Column(unique = true, nullable = false)
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
     * @return an immutable copy of the capabilities set
     */
    public Set<Capability> getAllCapabilities() {
        return Set.copyOf(capabilities);
    }

    /**
     * Add a capability to this role.
     * @param capability capability to add
     */
    public void addCapability(Capability capability){
        capabilities.add(capability);
        capability.getRoles().add(this);
    }

    /**
     * Remove a capability from this role.
     * @param capability capability to remove
     */
    public void removeCapability(Capability capability){
        capabilities.remove(capability);
        capability.getRoles().remove(this);
    }

    /**
     * Equality is based on the entity UUID. Two Role instances are considered equal
     * when their UUIDs are equal.
     * @param o the object to compare
     * @return {@code true} if the given object is a Role with the same UUID
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Role role)) return false;
        return Objects.equals(getUuid(), role.getUuid());
    }

    /**
     * Compute hash code using the entity UUID.
     * @return hash code derived from {@code uuid}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(getUuid());
    }
}