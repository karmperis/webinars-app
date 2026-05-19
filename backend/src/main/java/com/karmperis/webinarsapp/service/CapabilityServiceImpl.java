package com.karmperis.webinarsapp.service;

import com.karmperis.webinarsapp.core.exceptions.EntityAlreadyExistsException;
import com.karmperis.webinarsapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.webinarsapp.core.exceptions.EntityNotFoundException;
import com.karmperis.webinarsapp.dto.CapabilityEditDTO;
import com.karmperis.webinarsapp.dto.CapabilityInsertDTO;
import com.karmperis.webinarsapp.dto.CapabilityReadOnlyDTO;
import com.karmperis.webinarsapp.mapper.CapabilityMapper;
import com.karmperis.webinarsapp.model.Capability;
import com.karmperis.webinarsapp.repository.CapabilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Default {@link ICapabilityService} implementation.
 * Handles capability CRUD operations and applies soft-delete semantics (deleted capabilities are excluded
 * from read operations).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CapabilityServiceImpl implements ICapabilityService{
        private final CapabilityRepository capabilityRepository;
        private final CapabilityMapper capabilityMapper;

    /**
     * Create and persist a new capability.
     *
     * @param dto capability creation data
     * @return the persisted capability as a read-only DTO
     * @throws EntityAlreadyExistsException if a non-deleted capability with the same name already exists
     * @throws EntityInvalidArgumentException if the provided capability data is invalid
     */
    @Override
    @Transactional(rollbackFor = { EntityAlreadyExistsException.class, EntityInvalidArgumentException.class })
    public CapabilityReadOnlyDTO saveCapability(CapabilityInsertDTO dto) throws EntityAlreadyExistsException, EntityInvalidArgumentException {
        if (dto == null || dto.name() == null || dto.name().isBlank()) {
            throw new EntityInvalidArgumentException("Capability", "Capability name cannot be blank");
        }

        log.info("Attempting to save new capability with name: {}", dto.name());

        int nameLength = dto.name().trim().length();
        if (nameLength < 4 || nameLength > 50) {
            throw new EntityInvalidArgumentException("Capability", "Capability name must contain between 4 and 50 characters");
        }

        try {
            if (capabilityRepository.findByNameAndDeletedAtIsNull(dto.name()).isPresent()) {
                throw new EntityAlreadyExistsException("Capability", "Capability with name " + dto.name() + " already exists");
            }

            Capability capability = capabilityMapper.mapToCapabilityEntity(dto);
            Capability savedCapability = capabilityRepository.save(capability);

            log.info("Capability saved successfully with UUID: {}", savedCapability.getUuid());
            return capabilityMapper.mapToCapabilityReadOnlyDTO(savedCapability);

        } catch (EntityAlreadyExistsException e) {
            log.warn("Failed to save capability: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieve all non-deleted capabilities sorted by name.
     * @return list of active capabilities mapped to read-only DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public List<CapabilityReadOnlyDTO> findAllCapabilitiesSortedByName() {
        log.info("Fetching all active capabilities sorted by name");
        return capabilityRepository.findAllByDeletedAtIsNullOrderByNameAsc()
                .stream()
                .map(capabilityMapper::mapToCapabilityReadOnlyDTO)
                .toList();
    }

    /**
     * Retrieve a non-deleted capability by UUID.
     *
     * @param uuid capability UUID
     * @return the matching capability mapped to a read-only DTO
     * @throws EntityNotFoundException if no non-deleted capability with the given UUID exists
     */
    @Override
    @Transactional(readOnly = true)
    public CapabilityReadOnlyDTO findCapabilityByUuid(UUID uuid) throws EntityNotFoundException {
        log.info("Searching for capability with UUID {}", uuid);
        return capabilityRepository.findByUuidAndDeletedAtIsNull(uuid)
                .map(capabilityMapper::mapToCapabilityReadOnlyDTO)
                .orElseThrow(() -> {
                   log.warn("Capability with UUID {} not found", uuid);
                   return new EntityNotFoundException("Capability", "Capability with UUID " + uuid + " not found");
                });
    }

    /**
     * Update an existing non-deleted capability.
     *
     * @param uuid capability UUID
     * @param dto updated capability data
     * @return the updated capability mapped to a read-only DTO
     * @throws EntityNotFoundException if no non-deleted capability with the given UUID exists
     * @throws EntityAlreadyExistsException if the new name conflicts with another non-deleted capability
     * @throws EntityInvalidArgumentException if the provided capability data is invalid
     */
    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class, EntityAlreadyExistsException.class, EntityInvalidArgumentException.class})
    public CapabilityReadOnlyDTO updateCapability(UUID uuid, CapabilityEditDTO dto) throws EntityNotFoundException, EntityAlreadyExistsException, EntityInvalidArgumentException {
        log.info("Updating capability with UUID: {}", uuid);

            if (dto == null || dto.name() == null || dto.name().isBlank()) {
                throw new EntityInvalidArgumentException("Capability", "Capability name cannot be blank");
            }

        // Defensive programming: structural validation enforced at service level even though checked by DTO bean validation
            int nameLength = dto.name().trim().length();
            if (nameLength < 4 || nameLength > 50) {
                throw new EntityInvalidArgumentException("Capability", "Capability name must contain between 4 and 50 characters");
            }

            Capability capability = capabilityRepository.findByUuidAndDeletedAtIsNull(uuid)
                    .orElseThrow(() -> new EntityNotFoundException("Capability", "Capability not found"));

            if (!capability.getName().equalsIgnoreCase(dto.name()) &&
                    capabilityRepository.findByNameAndDeletedAtIsNull(dto.name()).isPresent()) {
                throw new EntityAlreadyExistsException("Capability", "Capability with name " + dto.name() + " already exists");
            }

            capabilityMapper.mapToCapabilityEditDTO(capability, dto);
            Capability updatedCapability = capabilityRepository.save(capability);
            log.info("Capability with UUID {} updated successfully", uuid);

            return capabilityMapper.mapToCapabilityReadOnlyDTO(updatedCapability);
    }

    /**
     * Soft-delete a capability by setting its deleted timestamp.
     *
     * @param uuid capability UUID
     * @throws EntityNotFoundException if no non-deleted capability with the given UUID exists
     */
    @Override
    @Transactional(rollbackFor = EntityNotFoundException.class)
    public void softDeleteCapabilityByUuid(UUID uuid) throws EntityNotFoundException {
            log.info("Performing soft delete for capability with UUID: {}", uuid);

            Capability capability = capabilityRepository.findByUuidAndDeletedAtIsNull(uuid)
                    .orElseThrow(() -> new EntityNotFoundException("Capability","Capability not found"));

            capability.softDelete();
            capabilityRepository.save(capability);
            log.info("Capability with UUID {} soft deleted successfully", uuid);
    }
}