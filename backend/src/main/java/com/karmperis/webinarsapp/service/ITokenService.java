package com.karmperis.webinarsapp.service;

import com.karmperis.webinarsapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.webinarsapp.core.exceptions.EntityNotFoundException;
import com.karmperis.webinarsapp.model.Token;
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
     */
    Token createToken(User user, String type);

    /**
     * Retrieves a token by its string representation and validates its state.
     * Checks if the token exists, matches the expected type, is not expired, and has not been used.
     *
     * @param userToken  the unique token string provided by the client
     * @param expectedType the expected type of the token
     * @return the validated Token entity
     * @throws EntityNotFoundException if the token does not exist
     * @throws EntityInvalidArgumentException if the token is expired or has already been used
     */
    Token verifyAndGetToken(String userToken, String expectedType)
            throws EntityNotFoundException, EntityInvalidArgumentException;

    /**
     * Marks a specific token as used, preventing its future reuse.
     *
     * @param userToken the unique token string
     * @throws EntityNotFoundException if the token does not exist
     */
    void markTokenAsUsed(String userToken) throws EntityNotFoundException;

    /**
     * Removes or invalidates all tokens belonging to a specific user.
     * Useful for cleanup operations, such as when a user requests a new password reset,
     * and we want to invalidate previous unused tokens, or when an account is deleted.
     *
     * @param user the user whose tokens should be cleared
     */
    void clearToken(User user);
}