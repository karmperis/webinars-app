package com.karmperis.webinarsapp.api;

import com.karmperis.webinarsapp.core.exceptions.EntityAlreadyExistsException;
import com.karmperis.webinarsapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.webinarsapp.core.exceptions.EntityNotFoundException;
import com.karmperis.webinarsapp.core.exceptions.ValidationException;
import com.karmperis.webinarsapp.dto.*;
import com.karmperis.webinarsapp.service.IRoleService;
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
 * REST controller exposing CRUD endpoints for managing roles.
 * Base path: {@code /api/v1/roles}.
 */
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleRestController {
    private final IRoleService roleService;

    /**
     * Creates a new role.
     *
     * @param roleInsertDTO the request payload used to create a role
     * @param bindingResult bean validation results
     * @return HTTP 201 with the created role DTO and a {@code Location} header
     * @throws ValidationException if request payload validation fails (HTTP 400)
     * @throws EntityAlreadyExistsException if a role with the same name already exists (HTTP 409)
     * @throws EntityInvalidArgumentException if business validation fails (HTTP 400)
     */
    @Operation(
            summary = "Create a new role",
            description = "Creates a new role account in the system."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Role created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RoleReadOnlyDTO.class)
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
                    description = "Role already exists",
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
    public ResponseEntity<RoleReadOnlyDTO> createRole(@Valid @RequestBody RoleInsertDTO roleInsertDTO,
                                                      BindingResult bindingResult)
            throws ValidationException, EntityAlreadyExistsException, EntityInvalidArgumentException {

        if (bindingResult.hasErrors()) {
            throw new ValidationException("Role", "Invalid role data", bindingResult);
        }

        RoleReadOnlyDTO roleReadOnlyDTO = roleService.saveRole(roleInsertDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{uuid}")
                .buildAndExpand(roleReadOnlyDTO.uuid())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(roleReadOnlyDTO);
    }

    /**
     * Retrieves a role by UUID.
     *
     * @param uuid the role UUID
     * @return HTTP 200 with the role
     * @throws EntityNotFoundException if no non-deleted role exists for the given UUID (HTTP 404)
     */
    @Operation(
            summary = "Get role by UUID",
            description = "Retrieves a non-deleted role by their UUID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Role found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RoleReadOnlyDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Role not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<RoleReadOnlyDTO> getRoleByUUID(@PathVariable UUID uuid)
            throws EntityNotFoundException {

        return ResponseEntity.ok(roleService.findRoleByUuid(uuid));
    }

    /**
     * Returns all active (non-deleted) roles sorted by name.
     * @return HTTP 200 with a list of roles
     */
    @Operation(
            summary = "Get all roles",
            description = "Returns a list of all active roles sorted by name."
    )
    @GetMapping
    public ResponseEntity<List<RoleReadOnlyDTO>> getAllRoles() {
        return ResponseEntity.ok(roleService.findAllRolesSortedByName());
    }

    /**
     * Updates an existing role.
     *
     * @param uuid the role UUID
     * @param roleEditDTO the request payload containing updated role data
     * @param bindingResult bean validation results
     * @return HTTP 200 with the updated role DTO
     * @throws ValidationException if request payload validation fails (HTTP 400)
     * @throws EntityNotFoundException if the role does not exist (HTTP 404)
     * @throws EntityAlreadyExistsException if the new role name conflicts with an existing role (HTTP 409)
     * @throws EntityInvalidArgumentException if business validation fails (HTTP 400)
     */
    @Operation(
            summary = "Update an existing role",
            description = "Updates the name of an existing role."
    )
    @PutMapping("/{uuid}")
    public ResponseEntity<RoleReadOnlyDTO> updateRole(@PathVariable UUID uuid,
                                           @Valid @RequestBody RoleEditDTO roleEditDTO,
                                           BindingResult bindingResult)
            throws ValidationException, EntityNotFoundException, EntityAlreadyExistsException, EntityInvalidArgumentException {

        if (bindingResult.hasErrors()) {
            throw new ValidationException("Role", "Invalid role update data", bindingResult);
        }

        RoleReadOnlyDTO updatedRoleDto = roleService.updateRole(uuid, roleEditDTO);
        return ResponseEntity.ok(updatedRoleDto);
    }

    /**
     * Soft-deletes a role.
     *
     * @param uuid the role UUID
     * @return HTTP 204 if deleted successfully
     * @throws EntityNotFoundException if the role does not exist (HTTP 404)
     */
    @Operation(
            summary = "Soft delete a role",
            description = "Marks a role as deleted in the system."
    )
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID uuid)
            throws EntityNotFoundException {

        roleService.softDeleteRoleByUuid(uuid);
        return ResponseEntity.noContent().build();
    }
}