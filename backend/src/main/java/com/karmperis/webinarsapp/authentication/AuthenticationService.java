package com.karmperis.webinarsapp.authentication;

import com.karmperis.webinarsapp.dto.AuthenticationRequestDTO;
import com.karmperis.webinarsapp.dto.AuthenticationResponseDTO;
import com.karmperis.webinarsapp.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Service responsible for authenticating users and issuing JWTs.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Authenticate the user and return a JWT response.
     *
     * @param dto the authentication request payload
     * @return the authentication response containing a token
     */
    public AuthenticationResponseDTO authenticate(AuthenticationRequestDTO dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.username(), dto.password()));
        User user = (User) authentication.getPrincipal();
        String token = jwtService.generateToken(authentication.getName(), user.getRole().getName(), user.getUuid().toString());
        return new AuthenticationResponseDTO(token);
    }
}