package com.karmperis.webinarsapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Objects;

/**
 * JPA entity that stores additional profile information for a {@link User}.
 * Uses the same identifier as the associated user via {@link MapsId}.
 */
@Entity
@Table(name = "users_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDetail {

    @Id
    private Long userId;

    @Column(nullable = false, length = 100)
    private String firstname;

    @Column(nullable = false, length = 100)
    private String lastname;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME2(6)")
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME2(6)")
    private Instant updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @MapsId
    private User user;

    /**
     * Initializes timestamps when the entity is first persisted.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Updates the modification timestamp before the entity is updated.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    /**
     * Equality based on the primary key ({@code userId}).
     * @param o the object to compare with
     * @return {@code true} if both objects represent the same persisted row
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserDetail userDetail)) return false;
        return Objects.equals(getUserId(), userDetail.getUserId());
    }

    /**
     * Hash code based on the primary key ({@code userId}).
     * @return a hash code for this entity
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(getUserId());
    }
}