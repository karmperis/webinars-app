package com.karmperis.webinarsapp.api;

import com.karmperis.webinarsapp.authentication.AuthenticationService;
import com.karmperis.webinarsapp.dto.AuthenticationRequestDTO;
import com.karmperis.webinarsapp.dto.AuthenticationResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing authentication endpoints.
 * Base path: {@code /api/v1/auth}.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthRestController {
    private final AuthenticationService authenticationService;

    /**
     * Authenticate a user and return a JWT response.
     *
     * @param dto the authentication request payload
     * @return HTTP 200 with the authentication response
     */
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponseDTO> authenticate(@Valid @RequestBody AuthenticationRequestDTO dto) {
        AuthenticationResponseDTO responseDTO = authenticationService.authenticate(dto);
        return ResponseEntity.ok(responseDTO);
    }
}