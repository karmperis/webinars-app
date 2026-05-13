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

    @Override
    public List<CapabilityReadOnlyDTO> findAllCapabilitiesSortedByName() {
        return List.of();
    }

    @Override
    public CapabilityReadOnlyDTO findCapabilityByUuid(UUID uuid) throws EntityNotFoundException {
        return null;
    }

    @Override
    public CapabilityReadOnlyDTO editCapability(UUID uuid, CapabilityEditDTO dto) throws EntityNotFoundException, EntityAlreadyExistsException, EntityInvalidArgumentException {
        return null;
    }

    @Override
    public void softDeleteCapabilityByUuid(UUID uuid) throws EntityNotFoundException {

    }
}
