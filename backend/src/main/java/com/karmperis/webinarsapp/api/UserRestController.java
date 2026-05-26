package com.karmperis.webinarsapp.api;

import com.karmperis.webinarsapp.core.exceptions.EntityAlreadyExistsException;
import com.karmperis.webinarsapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.webinarsapp.core.exceptions.EntityNotFoundException;
import com.karmperis.webinarsapp.core.exceptions.ValidationException;
import com.karmperis.webinarsapp.dto.*;
import com.karmperis.webinarsapp.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

/**
 * REST controller exposing CRUD endpoints for managing users.
 * Base path: {@code /api/v1/users}.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class UserRestController {
    private final IUserService userService;

    /**
     * Creates a new user.
     *
     * @param userInsertDTO the request payload used to create a user
     * @param bindingResult bean validation results
     * @return HTTP 201 with the created user DTO and a {@code Location} header
     * @throws ValidationException            if request payload validation fails (HTTP 400)
     * @throws EntityAlreadyExistsException   if a user with the same username already exists (HTTP 409)
     * @throws EntityInvalidArgumentException if business validation fails (HTTP 400)
     */
    @Operation(
            summary = "Create a new user",
            description = "Creates a new user account in the system in an inactive state."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserReadOnlyDTO.class)
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
                    description = "User already exists",
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
    public ResponseEntity<UserReadOnlyDTO> createUser(@Valid @RequestBody UserInsertDTO userInsertDTO,
                                                      BindingResult bindingResult)
            throws ValidationException, EntityAlreadyExistsException, EntityInvalidArgumentException {
        if (bindingResult.hasErrors()) {
            throw new ValidationException("User", "Invalid user data", bindingResult);
        }

        UserReadOnlyDTO userReadOnlyDTO = userService.saveUser(userInsertDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{uuid}")
                .buildAndExpand(userReadOnlyDTO.uuid())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(userReadOnlyDTO);

    }

    /**
     * Retrieves a user by UUID.
     *
     * @param uuid the user UUID
     * @return HTTP 200 with the user
     * @throws EntityNotFoundException if no non-deleted user exists for the given UUID (HTTP 404)
     */
    @Operation(
            summary = "Get user by UUID",
            description = "Retrieves a non-deleted user by their UUID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserReadOnlyDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @GetMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwnProfile(#uuid, authentication)")
    public ResponseEntity<UserReadOnlyDTO> getUserByUUID(@PathVariable UUID uuid)
            throws EntityNotFoundException {

        return ResponseEntity.ok(userService.findUserByUuid(uuid));
    }

    /**
     * Returns a paginated and sorted list of active (non-deleted) users.
     *
     * @param pageable pagination and sorting configuration from request query parameters
     * @return HTTP 200 with a page of users
     */
    @Operation(
            summary = "Get a page of users",
            description = "Returns a paginated and sorted page of active users based on query parameters (page, size, sort)."
    )
    @GetMapping
    public ResponseEntity<Page<UserReadOnlyDTO>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.findAllUsersSortedByName(pageable));
    }

    /**
     * Updates an existing user.
     *
     * @param uuid          the user UUID
     * @param userEditDTO   the request payload containing updated user data
     * @param bindingResult bean validation results
     * @return HTTP 200 with the updated user DTO
     * @throws ValidationException            if request payload validation fails (HTTP 400)
     * @throws EntityNotFoundException        if the user does not exist (HTTP 404)
     * @throws EntityInvalidArgumentException if business validation fails (HTTP 400)
     */
    @Operation(
            summary = "Update user profile",
            description = "Updates the profile details of an existing user."
    )
    @PutMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwnProfile(#uuid, authentication)")
    public ResponseEntity<UserReadOnlyDTO> updateUser(@PathVariable UUID uuid,
                                                      @Valid @RequestBody UserEditDTO userEditDTO,
                                                      BindingResult bindingResult)
            throws ValidationException, EntityNotFoundException, EntityInvalidArgumentException {

        if (bindingResult.hasErrors()) {
            throw new ValidationException("User", "Invalid user update data", bindingResult);
        }

        UserReadOnlyDTO updatedUserDto = userService.updateUser(uuid, userEditDTO);
        return ResponseEntity.ok(updatedUserDto);
    }

    /**
     * Updates an existing user's access rights (Role, Active Status).
     * Strictly restricted to Administrators.
     */
    @Operation(
            summary = "Update user access rights (Admin Only)",
            description = "Updates the assigned role and active status of a user."
    )
    @PatchMapping("/{uuid}/access")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserReadOnlyDTO> updateUserAccess(@PathVariable UUID uuid,
                                                            @Valid @RequestBody UserAdminEditDTO userAdminEditDTO,
                                                            BindingResult bindingResult)
            throws ValidationException, EntityNotFoundException, EntityInvalidArgumentException {

        if (bindingResult.hasErrors()) {
            throw new ValidationException("User", "Invalid user access data", bindingResult);
        }

        UserReadOnlyDTO updatedUserDto = userService.updateUserAccess(uuid, userAdminEditDTO);
        return ResponseEntity.ok(updatedUserDto);
    }

    /**
     * Soft-deletes a user.
     *
     * @param uuid the user UUID
     * @return HTTP 204 if deleted successfully
     * @throws EntityNotFoundException if the user does not exist (HTTP 404)
     */
    @Operation(
            summary = "Soft delete a user",
            description = "Marks a user as deleted and deactivates their account in the system."
    )
    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID uuid)
            throws EntityNotFoundException {

        userService.softDeleteUserByUuid(uuid);
        return ResponseEntity.noContent().build();
    }
}