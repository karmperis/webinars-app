package com.karmperis.webinarsapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;

/**
 * JPA entity representing an authentication or verification token.
 * Tokens are used for actions like account activation and password resets.
 */
@Entity
@Table(name = "tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    @Setter(AccessLevel.NONE)
    private String token;

    @Setter(AccessLevel.NONE)
    @Column(nullable = false, length = 50, updatable = false)
    private String type;

    @Column(nullable = false)
    private Boolean used = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter(AccessLevel.NONE)
    private User user;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME2(6)")
    private Instant createdAt;

    @Column(name = "expiry_at", nullable = false, columnDefinition = "DATETIME2(6)")
    private Instant expiryAt;

    /**
     * Check if the token is expired.
     * @return {@code true} if the current time is after the expiry time
     */
    @Transient
    public boolean isExpired() {
        return Instant.now().isAfter(expiryAt);
    }

    /**
     * Equality is based on the unique {@code token} string.
     * This is a natural business key and ensures safe comparisons.
     *
     * @param o other object to compare
     * @return {@code true} when {@code o} is a {@link Token} with the exact same token string
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Token that)) return false;
        return Objects.equals(getToken(), that.getToken());
    }

    /**
     * Compute hash code for this entity based on the unique {@code token} string.
     * @return hash code computed from the token string
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(getToken());
    }
}