package com.karmperis.webinarsapp.service;

import com.karmperis.webinarsapp.core.exceptions.EntityAlreadyExistsException;
import com.karmperis.webinarsapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.webinarsapp.core.exceptions.EntityNotFoundException;
import com.karmperis.webinarsapp.dto.CapabilityReadOnlyDTO;
import com.karmperis.webinarsapp.dto.RoleEditDTO;
import com.karmperis.webinarsapp.dto.RoleInsertDTO;
import com.karmperis.webinarsapp.dto.RoleReadOnlyDTO;
import com.karmperis.webinarsapp.mapper.CapabilityMapper;
import com.karmperis.webinarsapp.mapper.RoleMapper;
import com.karmperis.webinarsapp.model.Capability;
import com.karmperis.webinarsapp.model.Role;
import com.karmperis.webinarsapp.repository.CapabilityRepository;
import com.karmperis.webinarsapp.repository.RoleRepository;
import com.karmperis.webinarsapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Default {@link IRoleService} implementation.
 * Handles role CRUD operations and applies soft-delete semantics (deleted roles are excluded
 * from read operations).
 */
@Service
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('MANAGE_ROLES')")
public class RoleServiceImpl implements IRoleService {
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final CapabilityRepository capabilityRepository;
    private final CapabilityMapper capabilityMapper;
    private final UserRepository userRepository;

    /**
     * Create and persist a new role.
     *
     * @param dto role creation data
     * @return the persisted role as a read-only DTO
     * @throws EntityAlreadyExistsException   if a non-deleted role with the same name already exists
     * @throws EntityInvalidArgumentException if the provided role data is invalid
     */
    @Override
    @Transactional(rollbackFor = {EntityAlreadyExistsException.class, EntityInvalidArgumentException.class})
    public RoleReadOnlyDTO saveRole(RoleInsertDTO dto) throws EntityAlreadyExistsException, EntityInvalidArgumentException {
        // Defensive programming: Guard clause for unit tests and internal calls that bypass Web-layer validation
        if (dto == null) {
            throw new EntityInvalidArgumentException("Role", "Role data cannot be null");
        }

        // Defensive programming: structural validation enforced at service level even though checked by DTO bean validation
        validateRoleData(dto.name());

        log.info("Attempting to save new role with name: {}", dto.name());

        try {
            if (roleRepository.findByNameAndDeletedAtIsNull(dto.name()).isPresent()) {
                throw new EntityAlreadyExistsException("Role", "Role with name " + dto.name() + " already exists");
            }

            Role role = roleMapper.mapToRoleEntity(dto);
            Role savedRole = roleRepository.save(role);

            log.info("Role saved successfully with UUID: {}", savedRole.getUuid());
            return roleMapper.mapToRoleReadOnlyDTO(savedRole);

        } catch (EntityAlreadyExistsException e) {
            log.warn("Failed to save role: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieve all non-deleted roles sorted by name.
     *
     * @return list of active roles mapped to read-only DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public List<RoleReadOnlyDTO> findAllRolesSortedByName() {
        log.info("Fetching all active roles sorted by name");
        return roleRepository.findAllByDeletedAtIsNullOrderByNameAsc()
                .stream()
                .map(roleMapper::mapToRoleReadOnlyDTO)
                .toList();
    }

    /**
     * Retrieve a non-deleted role by UUID.
     *
     * @param uuid role UUID
     * @return the matching role mapped to a read-only DTO
     * @throws EntityNotFoundException if no non-deleted role with the given UUID exists
     */
    @Override
    @Transactional(readOnly = true)
    public RoleReadOnlyDTO findRoleByUuid(UUID uuid) throws EntityNotFoundException, EntityInvalidArgumentException {
        log.info("Searching for role with UUID: {}", uuid);

        // Defensive programming: Guard clause for internal calls that may pass a null UUID
        validateUuid(uuid, "Role UUID");

        return roleRepository.findByUuidAndDeletedAtIsNull(uuid)
                .map(roleMapper::mapToRoleReadOnlyDTO)
                .orElseThrow(() -> {
                    log.warn("Role with UUID {} not found", uuid);
                    return new EntityNotFoundException("Role", "Role with UUID " + uuid + " not found");
                });

    }

    /**
     * Retrieve all capabilities assigned to a non-deleted role.
     *
     * @param roleUuid role UUID
     * @return list of capabilities assigned to the role
     * @throws EntityNotFoundException if no non-deleted role with the given UUID exists
     */
    @Override
    @Transactional(readOnly = true)
    public List<CapabilityReadOnlyDTO> findCapabilitiesByRoleUuid(UUID roleUuid)
            throws EntityNotFoundException, EntityInvalidArgumentException {

        log.info("Fetching capabilities for role with UUID: {}", roleUuid);

        // Defensive programming: Guard clauses for internal calls that may pass null UUIDs
        validateUuid(roleUuid, "Role UUID");

        Role role = roleRepository.findByUuidAndDeletedAtIsNull(roleUuid)
                .orElseThrow(() -> {
                    log.warn("Failed to fetch capabilities: Role with UUID {} not found", roleUuid);
                    return new EntityNotFoundException(
                            "Role",
                            "Role with UUID " + roleUuid + " not found"
                    );
                });

        return role.getAllCapabilities()
                .stream()
                .filter(capability -> capability.getDeletedAt() == null)
                .map(capabilityMapper::mapToCapabilityReadOnlyDTO)
                .toList();
    }

    /**
     * Update an existing non-deleted role.
     *
     * @param uuid role UUID
     * @param dto  updated role data
     * @return the updated role mapped to a read-only DTO
     * @throws EntityNotFoundException        if no non-deleted role with the given UUID exists
     * @throws EntityAlreadyExistsException   if the new name conflicts with another non-deleted role
     * @throws EntityInvalidArgumentException if the provided role data is invalid
     */
    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class, EntityAlreadyExistsException.class, EntityInvalidArgumentException.class})
    public RoleReadOnlyDTO updateRole(UUID uuid, RoleEditDTO dto)
            throws EntityNotFoundException, EntityAlreadyExistsException, EntityInvalidArgumentException {

        log.info("Updating role with UUID: {}", uuid);
        // Defensive programming: Guard clause for unit tests and internal calls that bypass Web-layer validation
        if (dto == null) {
            throw new EntityInvalidArgumentException("Role", "Role data cannot be null");
        }

        // Defensive programming: structural validation enforced at service level even though checked by DTO bean validation
        validateRoleData(dto.name());

        Role role = roleRepository.findByUuidAndDeletedAtIsNull(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Role", "Role not found"));

        if (!role.getName().equalsIgnoreCase(dto.name()) &&
                roleRepository.findByNameAndDeletedAtIsNull(dto.name()).isPresent()) {
            throw new EntityAlreadyExistsException("Role", "Role with name " + dto.name() + " already exists");
        }

        roleMapper.mapToRoleEditDTO(role, dto);
        Role updatedRole = roleRepository.save(role);

        log.info("Role with UUID {} updated successfully", uuid);

        return roleMapper.mapToRoleReadOnlyDTO(updatedRole);
    }

    /**
     * Soft-delete a role by setting its deleted timestamp.
     *
     * <p>Before performing the soft delete, verifies that the role is not assigned
     * to any enabled (active and non-deleted) users. If the role is currently in use,
     * the operation is rejected.</p>
     *
     * @param uuid role UUID
     * @throws EntityNotFoundException        if no non-deleted role with the given UUID exists
     * @throws EntityInvalidArgumentException if the role is assigned to one or more enabled users
     */
    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class, EntityInvalidArgumentException.class})
    public void softDeleteRoleByUuid(UUID uuid) throws EntityNotFoundException, EntityInvalidArgumentException {
        log.info("Performing soft delete for role with UUID: {}", uuid);

        // Defensive programming: Guard clause for internal calls that may pass a null UUID
        validateUuid(uuid, "Role UUID");

        Role role = roleRepository.findByUuidAndDeletedAtIsNull(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Role", "Role not found"));

        if (userRepository.existsByRole_IdAndActiveTrueAndDeletedAtIsNull(role.getId())) {
            log.warn("Role with UUID {} cannot be deleted because it is assigned to active users", uuid);

            throw new EntityInvalidArgumentException(
                    "Role",
                    "Role cannot be deleted because it is assigned to one or more active users");
        }

        role.softDelete();
        roleRepository.save(role);

        log.info("Role with UUID {} soft deleted successfully", uuid);
    }

    /**
     * Assign a capability to a role.
     *
     * @param roleUuid       the role UUID
     * @param capabilityUuid the capability UUID
     * @throws EntityNotFoundException      if the role or capability does not exist or is soft-deleted
     * @throws EntityAlreadyExistsException if the capability is already assigned to the role
     */
    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class, EntityAlreadyExistsException.class})
    public void assignCapabilityToRole(UUID roleUuid, UUID capabilityUuid) throws EntityNotFoundException, EntityAlreadyExistsException, EntityInvalidArgumentException {
        log.info("Assigning capability {} to role {}", capabilityUuid, roleUuid);

        // Defensive programming: Guard clauses for internal calls that may pass null UUIDs
        validateUuid(roleUuid, "Role UUID");
        validateUuid(capabilityUuid, "Capability UUID");

        Role role = roleRepository.findByUuidAndDeletedAtIsNull(roleUuid)
                .orElseThrow(() -> {
                    log.warn("Role assignment failed: Role with UUID {} not found", roleUuid);
                    return new EntityNotFoundException("Role", "Role with UUID " + roleUuid + " not found");
                });
        Capability capability = capabilityRepository.findByUuidAndDeletedAtIsNull(capabilityUuid)
                .orElseThrow(() -> {
                    log.warn("Role assignment failed: Capability with UUID {} not found", capabilityUuid);
                    return new EntityNotFoundException("Capability", "Capability with UUID " + capabilityUuid + " not found");
                });

        if (role.hasCapability(capability)) {
            log.warn("Capability {} is already assigned to role {}", capabilityUuid, roleUuid);
            throw new EntityAlreadyExistsException(
                    "RoleCapability",
                    "Capability is already assigned to this role"
            );
        }

        role.addCapability(capability);

        log.info("Successfully assigned capability {} to role {}", capabilityUuid, roleUuid);
    }

    /**
     * Remove a capability from a role.
     *
     * @param roleUuid       the role UUID
     * @param capabilityUuid the capability UUID
     * @throws EntityNotFoundException        if the role or capability does not exist or is soft-deleted
     * @throws EntityInvalidArgumentException if the capability is not assigned to the role
     */
    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class, EntityInvalidArgumentException.class})
    public void removeCapabilityFromRole(UUID roleUuid, UUID capabilityUuid)
            throws EntityNotFoundException, EntityInvalidArgumentException {

        log.info("Removing capability {} from role {}", capabilityUuid, roleUuid);

        // Defensive programming: Guard clauses for internal calls that may pass null UUIDs
        validateUuid(roleUuid, "Role UUID");
        validateUuid(capabilityUuid, "Capability UUID");

        Role role = roleRepository.findByUuidAndDeletedAtIsNull(roleUuid)
                .orElseThrow(() -> {
                    log.warn("Capability removal failed: Role with UUID {} not found", roleUuid);
                    return new EntityNotFoundException("Role", "Role with UUID " + roleUuid + " not found");
                });

        Capability capability = capabilityRepository.findByUuidAndDeletedAtIsNull(capabilityUuid)
                .orElseThrow(() -> {
                    log.warn("Capability removal failed: Capability with UUID {} not found", capabilityUuid);
                    return new EntityNotFoundException("Capability", "Capability with UUID " + capabilityUuid + " not found");
                });

        if (!role.hasCapability(capability)) {
            log.warn("Capability {} is not assigned to role {}", capabilityUuid, roleUuid);
            throw new EntityInvalidArgumentException(
                    "RoleCapability",
                    "Capability is not assigned to this role"
            );
        }

        role.removeCapability(capability);
        roleRepository.save(role);

        log.info("Successfully removed capability {} from role {}", capabilityUuid, roleUuid);
    }

    /**
     * Validates the structural integrity of role data.
     * Used as a defensive programming guard for service methods that may be invoked
     * outside the Web layer, bypassing Bean Validation.
     *
     * @param name the role name to validate
     * @throws EntityInvalidArgumentException if the role name is null, blank,
     *                                        or its length is outside the allowed range
     */
    private void validateRoleData(String name) throws EntityInvalidArgumentException {
        if (name == null || name.isBlank()) {
            throw new EntityInvalidArgumentException("Role", "Role name cannot be blank");
        }

        int nameLength = name.trim().length();
        if (nameLength < 4 || nameLength > 50) {
            throw new EntityInvalidArgumentException("Role", "Role name must contain between 4 and 50 characters");
        }
    }

    /**
     * Validates that a UUID parameter is not null.
     * Used as a defensive programming guard for service methods that receive UUID arguments.
     *
     * @param uuid      the UUID to validate
     * @param fieldName the logical name of the UUID field used in the exception message
     * @throws EntityInvalidArgumentException if the UUID is null
     */
    private void validateUuid(UUID uuid, String fieldName) throws EntityInvalidArgumentException {
        if (uuid == null) {
            throw new EntityInvalidArgumentException("Role", fieldName + " cannot be null");
        }
    }
}