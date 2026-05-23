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
public class User extends AbstractUuidEntity {

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserDetail userDetail;

    @Column(nullable = false)
    private Boolean active = false;

    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.NONE)
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
    public Set<Webinar> getAllEnrolledWebinars() {
        return Set.copyOf(enrolledWebinars);
    }

    /**
     * Enroll this user in the given webinar by updating the internal participants set.
     * Intended for package-level use by the {@link Webinar} entity to keep the
     * bidirectional relationship consistent.
     *
     * @param webinar webinar to add to the user's enrollments
     */
    void enrollInWebinar(Webinar webinar) {
        this.enrolledWebinars.add(webinar);
    }

    /**
     * Remove this user from the given webinar by updating the internal participants set.
     * Intended for package-level use by the {@link Webinar} entity to keep the
     * bidirectional relationship consistent.
     *
     * @param webinar webinar to remove from the user's enrollments
     */
    void dropWebinar(Webinar webinar) {
        this.enrolledWebinars.remove(webinar);
    }
}