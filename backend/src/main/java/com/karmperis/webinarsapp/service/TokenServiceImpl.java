package com.karmperis.webinarsapp.service;

import com.karmperis.webinarsapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.webinarsapp.core.exceptions.EntityNotFoundException;
import com.karmperis.webinarsapp.dto.TokenReadOnlyDTO;
import com.karmperis.webinarsapp.model.Token;
import com.karmperis.webinarsapp.model.User;
import com.karmperis.webinarsapp.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Default {@link ITokenService} implementation.
 * Handles the business logic for secure token creation, validation, and cleanup.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenServiceImpl implements ITokenService {
    private final TokenRepository tokenRepository;

    @Value("${app.token.expiration-hours:24}")
    private int expirationHours;

    /**
     * Create and persist a new token for the given user and type.
     *
     * @param user the user the token belongs to
     * @param type the token type (e.g., verification, reset)
     * @return the persisted token entity
     * @throws EntityInvalidArgumentException if the user or token type is invalid
     */
    @Override
    @Transactional(rollbackFor = EntityInvalidArgumentException.class)
    public TokenReadOnlyDTO createToken(User user, String type) throws EntityInvalidArgumentException {

        // Defensive programming: structural validation enforced at service level even though checked by DTO bean validation
        validateUserNotNull(user);
        validateStringNotBlank(type, "Token type");

        log.info("Attempting to create {} token for user: {}", type, user.getUsername());

        String generatedTokenStr = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plus(expirationHours, ChronoUnit.HOURS);

        Token token = new Token(
                null,
                generatedTokenStr,
                type,
                false,
                user,
                null,
                expiryDate
        );

        Token savedToken = tokenRepository.save(token);

        log.info("Successfully created {} token for user: {}", type, user.getUsername());
        return new TokenReadOnlyDTO(savedToken.getToken(), savedToken.getType(), savedToken.getUsed(), savedToken.getExpiryAt(), user.getUuid());
    }

    /**
     * Verify a token string and type and return the matching token.
     *
     * @param userToken    the token string provided by the user
     * @param expectedType the expected token type
     * @return the verified token entity
     * @throws EntityNotFoundException        if the token is missing or type mismatch occurs
     * @throws EntityInvalidArgumentException if the token is blank, used, or expired
     */
    @Override
    @Transactional(readOnly = true)
    public TokenReadOnlyDTO verifyAndGetToken(String userToken, String expectedType) throws EntityNotFoundException, EntityInvalidArgumentException {

        // Defensive programming: structural validation enforced at service level even though checked by DTO bean validation
        validateStringNotBlank(userToken, "Token string");
        validateStringNotBlank(expectedType, "Expected token type");

        log.info("Verifying token of type: {}", expectedType);

        Token token = tokenRepository.findByTokenAndType(userToken, expectedType)
                .orElseThrow(() -> {
                    log.warn("Token verification failed: Token not found or type mismatch");
                    return new EntityNotFoundException("Token", "Invalid or missing token");
                });

        if (Boolean.TRUE.equals(token.getUsed())) {
            log.warn("Token verification failed: Token already used");
            throw new EntityInvalidArgumentException("Token", "This token has already been used");
        }

        if (token.isExpired()) {
            log.warn("Token verification failed: Token expired");
            throw new EntityInvalidArgumentException("Token", "This token has expired");
        }

        log.info("Token successfully verified");
        return new TokenReadOnlyDTO(token.getToken(), token.getType(), token.getUsed(), token.getExpiryAt(), token.getUser().getUuid());
    }

    /**
     * Mark a token as used.
     *
     * @param userToken the token string to mark as used
     * @throws EntityNotFoundException        if the token does not exist
     * @throws EntityInvalidArgumentException if the token string is blank
     */
    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class, EntityInvalidArgumentException.class})
    public void markTokenAsUsed(String userToken) throws EntityNotFoundException, EntityInvalidArgumentException {
        // Defensive programming: structural validation enforced at service level even though checked by DTO bean validation
        validateStringNotBlank(userToken, "Token string");

        log.info("Attempting to mark token as used");

        try {
            Token token = tokenRepository.findByToken(userToken)
                    .orElseThrow(() -> new EntityNotFoundException("Token", "Token not found"));

            token.setUsed(true);
            tokenRepository.save(token);

            log.info("Token successfully marked as used");

        } catch (EntityNotFoundException e) {
            log.warn("Failed to mark token as used: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Delete all tokens associated with a user.
     *
     * @param user the user whose tokens should be cleared
     * @throws EntityInvalidArgumentException if the user is null
     */
    @Override
    @Transactional(rollbackFor = EntityInvalidArgumentException.class)
    public void clearToken(User user) throws EntityInvalidArgumentException {

        // Defensive programming: structural validation enforced at service level even though checked by DTO bean validation
        validateUserNotNull(user);
        log.info("Attempting to clear all tokens for user: {}", user.getUsername());

        List<Token> tokens = tokenRepository.findAllByUser(user);
        if (!tokens.isEmpty()) {
            tokenRepository.deleteAll(tokens);
            log.info("Successfully cleared {} tokens for user: {}", tokens.size(), user.getUsername());
        } else {
            log.info("No tokens found to clear for user: {}", user.getUsername());
        }
    }

    /**
     * Helper method to validate that a user object is not null.
     */
    private void validateUserNotNull(User user) throws EntityInvalidArgumentException {
        if (user == null) {
            throw new EntityInvalidArgumentException("User", "User cannot be null for token operations");
        }
    }

    /**
     * Helper method to validate that a string field is not null or blank.
     */
    private void validateStringNotBlank(String value, String fieldName) throws EntityInvalidArgumentException {
        if (value == null || value.isBlank()) {
            throw new EntityInvalidArgumentException("Token", fieldName + " cannot be blank");
        }
    }
}