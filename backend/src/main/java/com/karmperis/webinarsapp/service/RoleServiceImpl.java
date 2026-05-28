package com.karmperis.webinarsapp.service;

import com.karmperis.webinarsapp.core.exceptions.EntityAlreadyExistsException;
import com.karmperis.webinarsapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.webinarsapp.core.exceptions.EntityNotFoundException;
import com.karmperis.webinarsapp.dto.RoleEditDTO;
import com.karmperis.webinarsapp.dto.RoleInsertDTO;
import com.karmperis.webinarsapp.dto.RoleReadOnlyDTO;
import com.karmperis.webinarsapp.mapper.RoleMapper;
import com.karmperis.webinarsapp.model.Capability;
import com.karmperis.webinarsapp.model.Role;
import com.karmperis.webinarsapp.repository.CapabilityRepository;
import com.karmperis.webinarsapp.repository.RoleRepository;
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
@PreAuthorize("hasRole('ADMIN')")
public class RoleServiceImpl implements IRoleService {
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final CapabilityRepository capabilityRepository;

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
    public RoleReadOnlyDTO findRoleByUuid(UUID uuid) throws EntityNotFoundException {
        log.info("Searching for role with UUID: {}", uuid);
        return roleRepository.findByUuidAndDeletedAtIsNull(uuid)
                .map(roleMapper::mapToRoleReadOnlyDTO)
                .orElseThrow(() -> {
                    log.warn("Role with UUID {} not found", uuid);
                    return new EntityNotFoundException("Role", "Role with UUID " + uuid + " not found");
                });
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
     * @param uuid role UUID
     * @throws EntityNotFoundException if no non-deleted role with the given UUID exists
     */
    @Override
    @Transactional(rollbackFor = EntityNotFoundException.class)
    public void softDeleteRoleByUuid(UUID uuid) throws EntityNotFoundException {
        log.info("Performing soft delete for role with UUID: {}", uuid);

        Role role = roleRepository.findByUuidAndDeletedAtIsNull(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Role", "Role not found"));

        role.softDelete();

        roleRepository.save(role);
        log.info("Role with UUID {} soft deleted successfully", uuid);
    }

    /**
     * Assign a capability to a role.
     *
     * @param roleUuid       the role UUID
     * @param capabilityUuid the capability UUID
     * @throws EntityNotFoundException if the role or capability does not exist or is soft-deleted
     */
    @Override
    @Transactional(rollbackFor = EntityNotFoundException.class)
    public void assignCapabilityToRole(UUID roleUuid, UUID capabilityUuid) throws EntityNotFoundException {
        log.info("Assigning capability {} to role {}", capabilityUuid, roleUuid);

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

        role.addCapability(capability);

        log.info("Successfully assigned capability {} to role {}", capabilityUuid, roleUuid);
    }

    /**
     * Helper method for defensive structural validation of role data.
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
}