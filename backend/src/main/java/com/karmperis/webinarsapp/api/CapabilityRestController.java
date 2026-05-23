package com.karmperis.webinarsapp.api;

import com.karmperis.webinarsapp.core.exceptions.EntityAlreadyExistsException;
import com.karmperis.webinarsapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.webinarsapp.core.exceptions.EntityNotFoundException;
import com.karmperis.webinarsapp.core.exceptions.ValidationException;
import com.karmperis.webinarsapp.dto.*;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * REST controller exposing endpoints for managing capabilities.
 * Base path: {@code /api/v1/capabilities}.
 */
@RestController
@RequestMapping("/api/v1/capabilities")
@RequiredArgsConstructor
public class CapabilityRestController {
    private final ICapabilityService capabilityService;

    /**
     * Creates a new capability.
     *
     * @param capabilityInsertDTO the request payload used to create a capability
     * @param bindingResult       bean validation results
     * @return HTTP 201 with the created capability DTO and a {@code Location} header
     * @throws ValidationException            if request payload validation fails (HTTP 400)
     * @throws EntityAlreadyExistsException   if a capability with the same name already exists (HTTP 409)
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

    /**
     * Retrieves a capability by UUID.
     *
     * @param uuid the capability UUID
     * @return HTTP 200 with the capability
     * @throws EntityNotFoundException if no non-deleted capability exists for the given UUID (HTTP 404)
     */
    @Operation(
            summary = "Get capability by UUID",
            description = "Retrieves a non-deleted capability by their UUID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Capability found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CapabilityReadOnlyDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Capability not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<CapabilityReadOnlyDTO> getCapabilityByUUID(@PathVariable UUID uuid)
            throws EntityNotFoundException {

        return ResponseEntity.ok(capabilityService.findCapabilityByUuid(uuid));
    }

    /**
     * Returns all active (non-deleted) capabilities sorted by name.
     *
     * @return HTTP 200 with a list of capabilities
     */

    @Operation(
            summary = "Get all capabilities",
            description = "Returns a list of all active capabilities sorted by name."
    )
    @GetMapping
    public ResponseEntity<List<CapabilityReadOnlyDTO>> getAllCapabilities() {
        return ResponseEntity.ok(capabilityService.findAllCapabilitiesSortedByName());
    }

    /**
     * Updates an existing capability.
     *
     * @param uuid              the capability UUID
     * @param capabilityEditDTO the request payload containing updated capability data
     * @param bindingResult     bean validation results
     * @return HTTP 200 with the updated capability DTO
     * @throws ValidationException            if request payload validation fails (HTTP 400)
     * @throws EntityNotFoundException        if the capability does not exist (HTTP 404)
     * @throws EntityAlreadyExistsException   if the new capability name conflicts with an existing capability (HTTP 409)
     * @throws EntityInvalidArgumentException if business validation fails (HTTP 400)
     */
    @Operation(
            summary = "Update an existing capability",
            description = "Updates the name/description of an existing capability."
    )
    @PutMapping("/{uuid}")
    public ResponseEntity<CapabilityReadOnlyDTO> updateCapability(@PathVariable UUID uuid,
                                                                  @Valid @RequestBody CapabilityEditDTO capabilityEditDTO,
                                                                  BindingResult bindingResult)
            throws ValidationException, EntityNotFoundException, EntityAlreadyExistsException, EntityInvalidArgumentException {

        if (bindingResult.hasErrors()) {
            throw new ValidationException("Capability", "Invalid capability update data", bindingResult);
        }

        CapabilityReadOnlyDTO updatedCapabilityDto = capabilityService.updateCapability(uuid, capabilityEditDTO);
        return ResponseEntity.ok(updatedCapabilityDto);
    }

    /**
     * Soft-deletes a capability.
     *
     * @param uuid the capability UUID
     * @return HTTP 204 if deleted successfully
     * @throws EntityNotFoundException if the capability does not exist (HTTP 404)
     */
    @Operation(
            summary = "Soft delete a capability",
            description = "Marks a capability as deleted in the system."
    )
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteCapability(@PathVariable UUID uuid)
            throws EntityNotFoundException {

        capabilityService.softDeleteCapabilityByUuid(uuid);
        return ResponseEntity.noContent().build();
    }
}