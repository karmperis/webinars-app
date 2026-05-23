package com.karmperis.webinarsapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
@EntityListeners(AuditingEntityListener.class)
public class UserDetail {

    @Id
    private Long userId;

    @Column(nullable = false, length = 100)
    private String firstname;

    @Column(nullable = false, length = 100)
    private String lastname;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME2(6)")
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME2(6)")
    private Instant updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @MapsId
    private User user;

    /**
     * Equality for UserDetail is based on the linked user's identifier.
     * This method compares only the {@code userId} (primary key) and is
     * safe to use for detached entities as well.
     *
     * @param o other object to compare
     * @return {@code true} when {@code o} is a {@link UserDetail} with the same {@code userId}
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserDetail userDetail)) return false;
        return Objects.equals(getUserId(), userDetail.getUserId());
    }

    /**
     * Compute hash code for this entity based on the {@code userId}.
     *
     * @return hash code computed from {@code userId}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(getUserId());
    }
}