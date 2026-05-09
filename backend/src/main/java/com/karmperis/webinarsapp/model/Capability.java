package com.karmperis.webinarsapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * JPA entity representing a capability/permission with a unique name and
 * optional description. Extends {@link AbstractUuidEntity} to inherit UUID,
 * auditing timestamps and soft-delete support.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "capabilities")
public class Capability extends AbstractUuidEntity {
    @Column(unique = true, nullable = false)
    private String name;

    @Column
    private String description;

    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.NONE)
    @ManyToMany(mappedBy = "capabilities", fetch = FetchType.LAZY)

    private Set<Role> roles = new HashSet<>();

    /**
     * Return an unmodifiable set of roles that have this capability.
     * @return an immutable copy of the roles set
     */
    public Set<Role> getAllRoles(){
        return Set.copyOf(roles);
    }

    /**
     * Add a role to this capability.
     * @param role role to add
     */
    public void addRole(Role role){
        roles.add(role);
        role.getCapabilities().add(this);
    }

    /**
     * Remove a role from this capability.
     * @param role role to remove
     */
    public void removeRole(Role role){
        roles.remove(role);
        role.getCapabilities().remove(this);
    }

    /**
     * Equality is based on the entity UUID. Two Capability instances are equal
     * when their UUIDs are equal.
     * @param o the object to compare
     * @return {@code true} if the given object is a Capability with the same UUID
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Capability capability)) return false;
        return Objects.equals(getUuid(), capability.getUuid());
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