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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@SecurityRequirement(name = "Bearer Authentication")
public class RoleRestController {
    private final IRoleService roleService;

    /**
     * Creates a new role.
     *
     * @param roleInsertDTO the request payload used to create a role
     * @param bindingResult bean validation results
     * @return HTTP 201 with the created role DTO and a {@code Location} header
     * @throws ValidationException            if request payload validation fails (HTTP 400)
     * @throws EntityAlreadyExistsException   if a role with the same name already exists (HTTP 409)
     * @throws EntityInvalidArgumentException if business validation fails (HTTP 400)
     */
    @Operation(
            summary = "Create a new role",
            description = "Creates a new role in the system."
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
     *
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
     * @param uuid          the role UUID
     * @param roleEditDTO   the request payload containing updated role data
     * @param bindingResult bean validation results
     * @return HTTP 200 with the updated role DTO
     * @throws ValidationException            if request payload validation fails (HTTP 400)
     * @throws EntityNotFoundException        if the role does not exist (HTTP 404)
     * @throws EntityAlreadyExistsException   if the new role name conflicts with an existing role (HTTP 409)
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

    /**
     * Assigns a capability to a role.
     *
     * @param roleUuid       the role UUID
     * @param capabilityUuid the capability UUID
     * @return HTTP 200 OK if successful
     * @throws EntityNotFoundException if role or capability does not exist (HTTP 404)
     */
    @Operation(
            summary = "Assign capability to role",
            description = "Assigns an existing capability to an existing role by their respective UUIDs."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Capability successfully assigned to role"
                    // We don't include @Content here because it returns ResponseEntity<Void> (an empty body).
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Role or Capability not found",
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
    @PostMapping("/{roleUuid}/capabilities/{capabilityUuid}")
    public ResponseEntity<Void> assignCapability(
            @PathVariable UUID roleUuid,
            @PathVariable UUID capabilityUuid) throws EntityNotFoundException, EntityAlreadyExistsException {

        roleService.assignCapabilityToRole(roleUuid, capabilityUuid);
        return ResponseEntity.ok().build();
    }

    /**
     * Retrieve all capabilities assigned to a role.
     *
     * @param roleUuid role UUID
     * @return list of capabilities assigned to the role
     * @throws EntityNotFoundException if the role does not exist or is soft-deleted
     */
    @Operation(
            summary = "Get role capabilities",
            description = "Retrieves all capabilities assigned to a specific role."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Capabilities retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Role not found"
            )
    })
    @GetMapping("/{roleUuid}/capabilities/view")
    public ResponseEntity<List<CapabilityReadOnlyDTO>> getRoleCapabilities(
            @PathVariable UUID roleUuid) throws EntityNotFoundException {

        return ResponseEntity.ok(roleService.findCapabilitiesByRoleUuid(roleUuid));
    }
}