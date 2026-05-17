package com.karmperis.webinarsapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

/**
 * JPA entity representing an application user.
 * Associates a {@link Role} and an optional {@link UserDetail} profile.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends AbstractUuidEntity {
    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserDetail userDetail;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = false;

    /**
     * Equality based on the UUID identifier.
     * @param o the object to compare with
     * @return {@code true} if both objects represent the same persisted user
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User user)) return false;
        return Objects.equals(getUuid(), user.getUuid());
    }

    /**
     * Hash code based on the UUID identifier.
     * @return a hash code for this user
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(getUuid());
    }
}