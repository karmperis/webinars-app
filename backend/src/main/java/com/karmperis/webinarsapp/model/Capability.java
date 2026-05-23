package com.karmperis.webinarsapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * JPA entity representing a capability/permission with a unique name and
 * optional description. Extends {@link AbstractUuidEntity} to inherit UUID,
 * auditing timestamps and soft-delete support.
 */
@Entity
@Table(name = "capabilities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Capability extends AbstractUuidEntity {
    @Column(nullable = false, length = 50)
    private String name;

    @Column
    private String description;

    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.NONE)
    @ManyToMany(mappedBy = "capabilities", fetch = FetchType.LAZY)
    private Set<Role> roles = new HashSet<>();

    /**
     * Return an unmodifiable set of roles that have this capability.
     *
     * @return an immutable copy of the roles set
     */
    public Set<Role> getAllRoles() {
        return Set.copyOf(roles);
    }

    /**
     * Add a role to this capability.
     *
     * @param role role to add
     */
    public void addRole(Role role) {
        roles.add(role);
        role.getCapabilities().add(this);
    }

    /**
     * Remove a role from this capability.
     *
     * @param role role to remove
     */
    public void removeRole(Role role) {
        roles.remove(role);
        role.getCapabilities().remove(this);
    }
}