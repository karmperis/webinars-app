package com.karmperis.webinarsapp.api;

import com.karmperis.webinarsapp.core.exceptions.EntityAlreadyExistsException;
import com.karmperis.webinarsapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.webinarsapp.core.exceptions.ValidationException;
import com.karmperis.webinarsapp.dto.CapabilityInsertDTO;
import com.karmperis.webinarsapp.dto.CapabilityReadOnlyDTO;
import com.karmperis.webinarsapp.dto.ErrorResponseDTO;
import com.karmperis.webinarsapp.dto.ValidationErrorResponseDTO;
import com.karmperis.webinarsapp.service.ICapabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * REST controller exposing endpoints for managing capabilities.
 * Base path: {@code /api/v1/capabilities}.
 */
@RestController
@RequestMapping("api/v1/capabilities")
@RequiredArgsConstructor
public class CapabilityRestController {
    private final ICapabilityService capabilityService;

    /**
     * Creates a new capability.
     *
     * @param capabilityInsertDTO the request payload used to create a capability
     * @param bindingResult bean validation results
     * @return HTTP 201 with the created capability DTO and a {@code Location} header
     * @throws ValidationException if request payload validation fails (HTTP 400)
     * @throws EntityAlreadyExistsException if a capability with the same name already exists (HTTP 409)
     * @throws EntityInvalidArgumentException if business validation fails (HTTP 400)
     */
    @Operation(
            summary = "Create a new capability",
            description = "Creates a new capability account in the system"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Capability created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CapabilityReadOnlyDTO.class)
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
                    responseCode = "409",
                    description = "Capability already exists",
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
    @PostMapping
    public ResponseEntity<CapabilityReadOnlyDTO> createCapability(@Valid @RequestBody CapabilityInsertDTO capabilityInsertDTO,
                                                            BindingResult bindingResult)
            throws ValidationException, EntityAlreadyExistsException, EntityInvalidArgumentException {

        if (bindingResult.hasErrors()) {
            throw new ValidationException("Capability", "Invalid capability data", bindingResult);
        }

        CapabilityReadOnlyDTO capabilityReadOnlyDTO = capabilityService.saveCapability(capabilityInsertDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{uuid}")
                .buildAndExpand(capabilityReadOnlyDTO.uuid())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(capabilityReadOnlyDTO);
    }
}