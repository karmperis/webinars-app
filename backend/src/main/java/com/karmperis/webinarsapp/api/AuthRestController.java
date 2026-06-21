package com.karmperis.webinarsapp.api;

import com.karmperis.webinarsapp.authentication.AuthenticationService;
import com.karmperis.webinarsapp.dto.AuthenticationRequestDTO;
import com.karmperis.webinarsapp.dto.AuthenticationResponseDTO;
import com.karmperis.webinarsapp.dto.ErrorResponseDTO;
import com.karmperis.webinarsapp.dto.ValidationErrorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(
            summary = "Authenticate user",
            description = "Authenticates a user with username and password and returns a JWT token."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Authentication successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthenticationResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ValidationErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid username or password",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponseDTO> authenticate(@Valid @RequestBody AuthenticationRequestDTO dto) {
        AuthenticationResponseDTO responseDTO = authenticationService.authenticate(dto);
        return ResponseEntity.ok(responseDTO);
    }
}