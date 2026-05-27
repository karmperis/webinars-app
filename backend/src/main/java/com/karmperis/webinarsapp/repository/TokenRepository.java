package com.karmperis.webinarsapp.repository;

import com.karmperis.webinarsapp.model.Token;
import com.karmperis.webinarsapp.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Token} entities.
 */
public interface TokenRepository extends JpaRepository<Token, Long> {
    /**
     * Find a token by its unique string representation.
     * Eagerly fetches the associated {@code user} to optimize the subsequent
     * user state updates (e.g., account activation) and avoid extra database queries.
     *
     * @param token the unique token string
     * @return an Optional containing the token if found
     */
    @EntityGraph(attributePaths = {"user"})
    Optional<Token> findByToken(String token);

    /**
     * Find a token by its unique string representation and type.
     * Eagerly fetches the associated {@code user} to optimize operations like password resets.
     *
     * @param token the unique token string
     * @param type  the type of the token
     * @return an Optional containing the token if found
     */
    @EntityGraph(attributePaths = {"user"})
    Optional<Token> findByTokenAndType(String token, String type);

    /**
     * Return all tokens associated with a specific user.
     * Useful when a user's account is deleted or when invalidating old tokens.
     *
     * @param user the user entity
     * @return list of tokens belonging to the user
     */
    List<Token> findAllByUser(User user);
}