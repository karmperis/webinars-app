package com.karmperis.webinarsapp.api;

import com.karmperis.webinarsapp.core.exceptions.EntityAlreadyExistsException;
import com.karmperis.webinarsapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.webinarsapp.core.exceptions.EntityNotFoundException;
import com.karmperis.webinarsapp.core.exceptions.ValidationException;
import com.karmperis.webinarsapp.dto.*;
import com.karmperis.webinarsapp.model.User;
import com.karmperis.webinarsapp.service.IWebinarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

/**
 * REST controller exposing CRUD endpoints for managing webinars.
 * Base path: {@code /api/v1/webinars}.
 */
@RestController
@RequestMapping("/api/v1/webinars")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class WebinarRestController {
    private final IWebinarService webinarService;

    /**
     * Creates a new webinar.
     *
     * @param dto            the request payload used to create a webinar
     * @param bindingResult  bean validation results
     * @param authentication the authenticated principal (used to derive organizer UUID)
     * @return HTTP 201 with the created webinar DTO and a {@code Location} header
     * @throws ValidationException            if request payload validation fails (HTTP 400)
     * @throws EntityAlreadyExistsException   if a webinar with the same title already exists (HTTP 409)
     * @throws EntityInvalidArgumentException if business validation fails (HTTP 400)
     * @throws EntityNotFoundException        if the organizer specified does not exist (HTTP 404)
     */
    @Operation(
            summary = "Create a new webinar",
            description = "Creates a new scheduled webinar in the system."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Webinar created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = WebinarReadOnlyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Organizer not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Webinar already exists",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping
    public ResponseEntity<WebinarReadOnlyDTO> createWebinar(@Valid @RequestBody WebinarInsertDTO dto,
                                                            BindingResult bindingResult, Authentication authentication)
            throws ValidationException, EntityAlreadyExistsException, EntityInvalidArgumentException, EntityNotFoundException {

        if (bindingResult.hasErrors()) {
            throw new ValidationException("Webinar", "Invalid webinar data", bindingResult);
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            throw new EntityNotFoundException("User", "Invalid authentication principal");
        }
        UUID organizerUuid = ((User) principal).getUuid();

        WebinarReadOnlyDTO readOnlyDTO = webinarService.saveWebinar(dto, organizerUuid);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{uuid}")
                .buildAndExpand(readOnlyDTO.uuid())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(readOnlyDTO);
    }

    /**
     * Retrieves a webinar by UUID.
     *
     * @param uuid the webinar UUID
     * @return HTTP 200 with the webinar
     * @throws EntityNotFoundException if no non-deleted webinar exists for the given UUID (HTTP 404)
     */
    @Operation(summary = "Get webinar by UUID", description = "Retrieves a non-deleted webinar by its UUID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Webinar found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = WebinarReadOnlyDTO.class))),
            @ApiResponse(responseCode = "404", description = "Webinar not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<WebinarReadOnlyDTO> getWebinarByUUID(@PathVariable UUID uuid) throws EntityNotFoundException {
        return ResponseEntity.ok(webinarService.findWebinarByUuid(uuid));
    }

    /**
     * Returns a paginated list of active webinars.
     *
     * @param pageable pagination and sorting configuration
     * @return HTTP 200 with a page of webinars
     */
    @Operation(summary = "Get a page of webinars", description = "Returns a paginated and sorted page of active webinars.")
    @GetMapping
    public ResponseEntity<Page<WebinarReadOnlyDTO>> getAllWebinars(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(webinarService.findAllWebinars(pageable));
    }

    /**
     * Returns a paginated list of active webinars organized by a specific user.
     *
     * @param organizerUuid the UUID of the organizer
     * @param pageable      pagination and sorting configuration
     * @return HTTP 200 with a page of webinars
     * @throws EntityNotFoundException if the organizer is not found
     */
    @Operation(summary = "Get webinars by organizer", description = "Returns a paginated list of webinars organized by a specific user.")
    @GetMapping("/organizer/{organizerUuid}")
    public ResponseEntity<Page<WebinarReadOnlyDTO>> getWebinarsByOrganizer(@PathVariable UUID organizerUuid, @ParameterObject Pageable pageable)
            throws EntityNotFoundException {
        return ResponseEntity.ok(webinarService.findAllWebinarsByOrganizer(organizerUuid, pageable));
    }

    /**
     * Returns a paginated list of active webinars where a specific user is enrolled as participant.
     *
     * @param userUuid the UUID of the participant
     * @param pageable pagination and sorting configuration
     * @return HTTP 200 with a page of webinars
     * @throws EntityNotFoundException if the participant is not found
     */
    @Operation(
            summary = "Get webinars by participant",
            description = "Returns a paginated list of webinars where a specific user is enrolled as participant."
    )
    @GetMapping("/participants/{userUuid}")
    public ResponseEntity<Page<WebinarReadOnlyDTO>> getWebinarsByParticipant(
            @PathVariable UUID userUuid,
            @ParameterObject Pageable pageable
    ) throws EntityNotFoundException {
        return ResponseEntity.ok(webinarService.findAllWebinarsByParticipant(userUuid, pageable));
    }

    /**
     * Updates an existing webinar.
     *
     * @param uuid          the webinar UUID
     * @param dto           the request payload containing updated data
     * @param bindingResult bean validation results
     * @return HTTP 200 with the updated webinar DTO
     * @throws ValidationException            if request payload validation fails
     * @throws EntityNotFoundException        if the webinar does not exist
     * @throws EntityAlreadyExistsException   if the new title conflicts
     * @throws EntityInvalidArgumentException if business validation fails
     */
    @Operation(summary = "Update an existing webinar", description = "Updates the details of an existing scheduled webinar.")
    @PutMapping("/{uuid}")
    public ResponseEntity<WebinarReadOnlyDTO> updateWebinar(@PathVariable UUID uuid,
                                                            @Valid @RequestBody WebinarEditDTO dto,
                                                            BindingResult bindingResult)
            throws ValidationException, EntityNotFoundException, EntityAlreadyExistsException, EntityInvalidArgumentException {

        if (bindingResult.hasErrors()) {
            throw new ValidationException("Webinar", "Invalid webinar update data", bindingResult);
        }

        WebinarReadOnlyDTO updatedWebinar = webinarService.updateWebinar(uuid, dto);
        return ResponseEntity.ok(updatedWebinar);
    }

    /**
     * Soft-deletes a webinar.
     *
     * @param uuid the webinar UUID
     * @return HTTP 204 if deleted successfully
     * @throws EntityNotFoundException if the webinar does not exist
     */
    @Operation(summary = "Soft delete a webinar", description = "Marks a webinar as deleted in the system.")
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteWebinar(@PathVariable UUID uuid) throws EntityNotFoundException {
        webinarService.softDeleteWebinarByUuid(uuid);
        return ResponseEntity.noContent().build();
    }

    /**
     * Enrolls a user as a participant in a webinar.
     *
     * @param webinarUuid the UUID of the webinar
     * @param userUuid    the UUID of the user to enroll
     * @return HTTP 204 indicating successful enrollment
     * @throws EntityNotFoundException        if either the webinar or user is not found
     * @throws EntityAlreadyExistsException   if the user is already enrolled in the webinar
     * @throws EntityInvalidArgumentException if the organizer tries to enroll in their own webinar
     */
    @Operation(
            summary = "Enroll user in webinar",
            description = "Registers a user as a participant in a specific webinar using their UUIDs."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User successfully enrolled (No Content)"),
            @ApiResponse(responseCode = "404", description = "Webinar or User not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid enrollment request",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "User is already enrolled",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping("/{webinarUuid}/participants/{userUuid}")
    public ResponseEntity<Void> enrollUser(@PathVariable UUID webinarUuid, @PathVariable UUID userUuid)
            throws EntityNotFoundException, EntityAlreadyExistsException, EntityInvalidArgumentException {
        webinarService.enrollUserInWebinar(webinarUuid, userUuid);
        return ResponseEntity.noContent().build();
    }
}