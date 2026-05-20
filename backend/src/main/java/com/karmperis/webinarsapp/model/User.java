package com.karmperis.webinarsapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

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

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserDetail userDetail;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = false;

    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.NONE)
    @Builder.Default
    @ManyToMany(mappedBy = "participants", fetch = FetchType.LAZY)
    private Set<Webinar> enrolledWebinars = new HashSet<>();

    /**
     * Associate a {@link UserDetail} with this user and ensure the bidirectional
     * relationship is kept in sync by setting the {@code user} reference on the details.
     *
     * @param details profile details to attach to this user; if {@code null} the current
     *                details reference will be cleared
     */
    public void addUserDetail(UserDetail details) {
        this.userDetail = details;
        if (details != null) {
            details.setUser(this);
        }
    }

    /**
     * Return an immutable snapshot of the webinars this user is enrolled in.
     * The returned set is an unmodifiable copy to protect internal state from modification.
     *
     * @return an immutable set with the user's enrolled webinars
     */
    public Set<Webinar> getAllEnrolledWebinars(){
        return Set.copyOf(enrolledWebinars);
    }
}