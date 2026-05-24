package com.karmperis.webinarsapp.service;

import com.karmperis.webinarsapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.webinarsapp.core.exceptions.EntityNotFoundException;
import com.karmperis.webinarsapp.model.Token;
import com.karmperis.webinarsapp.model.User;
import com.karmperis.webinarsapp.repository.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TokenServiceImplTest {
    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private TokenServiceImpl tokenService;

    private User user;
    private Token validToken;
    private Token expiredToken;
    private Token usedToken;
    private String tokenStr;

    @BeforeEach
    void setUp() {
        // Inject the @Value property manually for Unit Testing
        ReflectionTestUtils.setField(tokenService, "expirationHours", 24);

        user = new User();
        user.setUuid(UUID.randomUUID());
        user.setUsername("testuser");

        tokenStr = UUID.randomUUID().toString();

        validToken = new Token(
                1L,
                tokenStr,
                "VERIFICATION",
                false,
                user,
                null,
                Instant.now().plus(24, ChronoUnit.HOURS)
        );

        expiredToken = new Token(
                2L,
                UUID.randomUUID().toString(),
                "VERIFICATION",
                false,
                user,
                null,
                Instant.now().minus(1, ChronoUnit.HOURS) // Expired 1 hour ago
        );

        usedToken = new Token(
                3L,
                UUID.randomUUID().toString(),
                "VERIFICATION",
                true, // Already used
                user,
                null,
                Instant.now().plus(24, ChronoUnit.HOURS)
        );
    }

    // ==========================================
    // TESTS-CREATE TOKEN
    // ==========================================

    @Test
    @DisplayName("createToken: Should create and return a new token successfully")
    void createToken_Success() throws Exception {
        when(tokenRepository.save(any(Token.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Token result = tokenService.createToken(user, "VERIFICATION");

        assertNotNull(result);
        assertEquals("VERIFICATION", result.getType());
        assertEquals(user, result.getUser());
        assertFalse(result.getUsed());
        assertNotNull(result.getExpiryAt());
        verify(tokenRepository, times(1)).save(any(Token.class));
    }

    @Test
    @DisplayName("createToken: Should throw Exception when User is null")
    void createToken_ThrowsEntityInvalidArgumentException_WhenUserIsNull() {
        assertThrows(EntityInvalidArgumentException.class, () -> tokenService.createToken(null, "VERIFICATION"));
        verify(tokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("createToken: Should throw Exception when Type is blank")
    void createToken_ThrowsEntityInvalidArgumentException_WhenTypeIsBlank() {
        assertThrows(EntityInvalidArgumentException.class, () -> tokenService.createToken(user, "   "));
        verify(tokenRepository, never()).save(any());
    }

    // ==========================================
    // TESTS-VERIFY TOKEN
    // ==========================================

    @Test
    @DisplayName("verifyAndGetToken: Should return token when valid")
    void verifyAndGetToken_Success() throws Exception {
        when(tokenRepository.findByTokenAndType(tokenStr, "VERIFICATION")).thenReturn(Optional.of(validToken));

        Token result = tokenService.verifyAndGetToken(tokenStr, "VERIFICATION");

        assertNotNull(result);
        assertEquals(tokenStr, result.getToken());
    }

    @Test
    @DisplayName("verifyAndGetToken: Should throw Exception when token not found")
    void verifyAndGetToken_ThrowsEntityNotFoundException_WhenNotFound() {
        when(tokenRepository.findByTokenAndType("invalid-token", "VERIFICATION")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> tokenService.verifyAndGetToken("invalid-token", "VERIFICATION"));
    }

    @Test
    @DisplayName("verifyAndGetToken: Should throw Exception when token is already used")
    void verifyAndGetToken_ThrowsEntityInvalidArgumentException_WhenUsed() {
        when(tokenRepository.findByTokenAndType(usedToken.getToken(), "VERIFICATION")).thenReturn(Optional.of(usedToken));

        assertThrows(EntityInvalidArgumentException.class, () -> tokenService.verifyAndGetToken(usedToken.getToken(), "VERIFICATION"));
    }

    @Test
    @DisplayName("verifyAndGetToken: Should throw Exception when token is expired")
    void verifyAndGetToken_ThrowsEntityInvalidArgumentException_WhenExpired() {
        when(tokenRepository.findByTokenAndType(expiredToken.getToken(), "VERIFICATION")).thenReturn(Optional.of(expiredToken));

        assertThrows(EntityInvalidArgumentException.class, () -> tokenService.verifyAndGetToken(expiredToken.getToken(), "VERIFICATION"));
    }

    @Test
    @DisplayName("verifyAndGetToken: Should throw Exception when arguments are blank")
    void verifyAndGetToken_ThrowsEntityInvalidArgumentException_WhenArgsBlank() {
        assertThrows(EntityInvalidArgumentException.class, () -> tokenService.verifyAndGetToken("", "VERIFICATION"));
        assertThrows(EntityInvalidArgumentException.class, () -> tokenService.verifyAndGetToken(tokenStr, null));
    }

    // ==========================================
    // TESTS-MARK AS USED
    // ==========================================

    @Test
    @DisplayName("markTokenAsUsed: Should successfully update token status")
    void markTokenAsUsed_Success() throws Exception {
        when(tokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(validToken));

        tokenService.markTokenAsUsed(tokenStr);

        assertTrue(validToken.getUsed());
        verify(tokenRepository, times(1)).save(validToken);
    }

    @Test
    @DisplayName("markTokenAsUsed: Should throw Exception when token string is blank")
    void markTokenAsUsed_ThrowsEntityInvalidArgumentException_WhenTokenIsBlank() {
        assertThrows(EntityInvalidArgumentException.class, () -> tokenService.markTokenAsUsed("  "));
        verify(tokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("markTokenAsUsed: Should throw Exception when token not found")
    void markTokenAsUsed_ThrowsEntityNotFoundException_WhenNotFound() {
        when(tokenRepository.findByToken("not-found-token")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> tokenService.markTokenAsUsed("not-found-token"));
        verify(tokenRepository, never()).save(any());
    }

    // ==========================================
    // TESTS FOR CLEAR TOKEN
    // ==========================================

    @Test
    @DisplayName("clearToken: Should delete tokens when user has existing tokens")
    void clearToken_Success_DeletesTokens() throws Exception {
        List<Token> userTokens = List.of(validToken, usedToken);
        when(tokenRepository.findAllByUser(user)).thenReturn(userTokens);

        tokenService.clearToken(user);

        verify(tokenRepository, times(1)).deleteAll(userTokens);
    }

    @Test
    @DisplayName("clearToken: Should do nothing when user has no tokens")
    void clearToken_Success_NoTokensFound() throws Exception {
        when(tokenRepository.findAllByUser(user)).thenReturn(Collections.emptyList());

        tokenService.clearToken(user);

        verify(tokenRepository, never()).deleteAll(anyList());
    }

    @Test
    @DisplayName("clearToken: Should throw Exception when User is null")
    void clearToken_ThrowsEntityInvalidArgumentException_WhenUserIsNull() {
        assertThrows(EntityInvalidArgumentException.class, () -> tokenService.clearToken(null));
        verify(tokenRepository, never()).findAllByUser(any());
    }
}