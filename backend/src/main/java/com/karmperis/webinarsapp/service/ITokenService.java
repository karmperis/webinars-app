package com.karmperis.webinarsapp.service;

import com.karmperis.webinarsapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.webinarsapp.core.exceptions.EntityNotFoundException;
import com.karmperis.webinarsapp.dto.TokenReadOnlyDTO;
import com.karmperis.webinarsapp.model.User;

/**
 * Service contract for managing verification and security tokens.
 */
public interface ITokenService {
    /**
     * Creates and persists a new secure token for the specified user.
     *
     * @param user the user associated with the token
     * @param type the type of the token
     * @return the generated and persisted Token entity
     * @throws EntityInvalidArgumentException if the user or token type is invalid
     */
    TokenReadOnlyDTO createToken(User user, String type) throws EntityInvalidArgumentException;

    /**
     * Retrieves a token by its string representation and validates its state.
     * Checks if the token exists, matches the expected type, is not expired, and has not been used.
     *
     * @param userToken    the unique token string provided by the client
     * @param expectedType the expected type of the token
     * @return the validated Token entity
     * @throws EntityNotFoundException        if the token does not exist
     * @throws EntityInvalidArgumentException if the token is expired or has already been used
     */
    TokenReadOnlyDTO verifyAndGetToken(String userToken, String expectedType)
            throws EntityNotFoundException, EntityInvalidArgumentException;

    /**
     * Marks a specific token as used, preventing its future reuse.
     *
     * @param userToken the unique token string
     * @throws EntityNotFoundException        if the token does not exist
     * @throws EntityInvalidArgumentException if the token string is blank
     */
    void markTokenAsUsed(String userToken) throws EntityNotFoundException, EntityInvalidArgumentException;

    /**
     * Removes or invalidates all tokens belonging to a specific user.
     * Useful for cleanup operations, such as when a user requests a new password reset,
     * and we want to invalidate previous unused tokens, or when an account is deleted.
     *
     * @param user the user whose tokens should be cleared
     * @throws EntityInvalidArgumentException if the user is null
     */
    void clearToken(User user) throws EntityInvalidArgumentException;
}