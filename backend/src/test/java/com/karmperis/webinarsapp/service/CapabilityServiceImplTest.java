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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CapabilityServiceImplTest {

    @Mock
    private CapabilityRepository capabilityRepository;

    @Mock
    private CapabilityMapper capabilityMapper;

    @InjectMocks
    private CapabilityServiceImpl capabilityService;

    private Capability capability;
    private UUID capabilityUuid;

    @BeforeEach
    void setUp() {
        capabilityUuid = UUID.randomUUID();
        capability = new Capability();
        capability.setUuid(capabilityUuid);
        capability.setName("MANAGE_USERS");
        capability.setDescription("Allows full user management");
    }

    // ==========================================
    // TESTS-SAVE
    // ==========================================

    @Test
    @DisplayName("saveCapability: Should save and return DTO successfully")
    void saveCapability_Success() throws Exception {
        CapabilityInsertDTO dto = new CapabilityInsertDTO("MANAGE_USERS", "Allows full user management");
        CapabilityReadOnlyDTO readOnlyDTO = new CapabilityReadOnlyDTO(capabilityUuid, "MANAGE_USERS", "Allows full user management");

        when(capabilityRepository.findByNameAndDeletedAtIsNull("MANAGE_USERS")).thenReturn(Optional.empty());
        when(capabilityMapper.mapToCapabilityEntity(dto)).thenReturn(capability);
        when(capabilityRepository.save(any(Capability.class))).thenReturn(capability);
        when(capabilityMapper.mapToCapabilityReadOnlyDTO(capability)).thenReturn(readOnlyDTO);

        CapabilityReadOnlyDTO result = capabilityService.saveCapability(dto);

        assertNotNull(result);
        assertEquals("MANAGE_USERS", result.name());
        verify(capabilityRepository, times(1)).save(any(Capability.class));
    }

    @Test
    @DisplayName("saveCapability: Should throw Exception when DTO is null")
    void saveCapability_ThrowsEntityInvalidArgumentException_WhenDtoIsNull() {
        assertThrows(EntityInvalidArgumentException.class, () -> capabilityService.saveCapability(null));
        verify(capabilityRepository, never()).save(any());
    }

    @Test
    @DisplayName("saveCapability: Should throw Exception when name already exists")
    void saveCapability_ThrowsEntityAlreadyExistsException() {
        CapabilityInsertDTO dto = new CapabilityInsertDTO("MANAGE_USERS", "Allows full user management");

        when(capabilityRepository.findByNameAndDeletedAtIsNull("MANAGE_USERS")).thenReturn(Optional.of(capability));

        assertThrows(EntityAlreadyExistsException.class, () -> capabilityService.saveCapability(dto));
        verify(capabilityRepository, never()).save(any());
    }

    // ==========================================
    // TESTS-FIND BY UUID
    // ==========================================

    @Test
    @DisplayName("findCapabilityByUuid: Should return Capability when found")
    void findCapabilityByUuid_Success() throws Exception {
        CapabilityReadOnlyDTO readOnlyDTO = new CapabilityReadOnlyDTO(capabilityUuid, "MANAGE_USERS", "Allows full user management");

        when(capabilityRepository.findByUuidAndDeletedAtIsNull(capabilityUuid)).thenReturn(Optional.of(capability));
        when(capabilityMapper.mapToCapabilityReadOnlyDTO(capability)).thenReturn(readOnlyDTO);

        CapabilityReadOnlyDTO result = capabilityService.findCapabilityByUuid(capabilityUuid);

        assertNotNull(result);
        assertEquals(capabilityUuid, result.uuid());
    }

    @Test
    @DisplayName("findCapabilityByUuid: Should throw Exception when not found")
    void findCapabilityByUuid_ThrowsEntityNotFoundException() {
        when(capabilityRepository.findByUuidAndDeletedAtIsNull(capabilityUuid)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> capabilityService.findCapabilityByUuid(capabilityUuid));
    }

    // ==========================================
    // TESTS-UPDATE/DELETE
    // ==========================================

    @Test
    @DisplayName("updateCapability: Should update Capability successfully")
    void updateCapability_Success() throws Exception {
        CapabilityEditDTO editDTO = new CapabilityEditDTO("MANAGE_ROLES", "Allows role management");
        CapabilityReadOnlyDTO readOnlyDTO = new CapabilityReadOnlyDTO(capabilityUuid, "MANAGE_ROLES", "Allows role management");

        when(capabilityRepository.findByUuidAndDeletedAtIsNull(capabilityUuid)).thenReturn(Optional.of(capability));
        when(capabilityRepository.findByNameAndDeletedAtIsNull("MANAGE_ROLES")).thenReturn(Optional.empty());
        when(capabilityRepository.save(capability)).thenReturn(capability);
        when(capabilityMapper.mapToCapabilityReadOnlyDTO(capability)).thenReturn(readOnlyDTO);

        CapabilityReadOnlyDTO result = capabilityService.updateCapability(capabilityUuid, editDTO);

        assertNotNull(result);
        assertEquals("MANAGE_ROLES", result.name());
    }

    @Test
    @DisplayName("softDeleteCapabilityByUuid: Should perform soft delete successfully")
    void softDeleteCapabilityByUuid_Success() throws Exception {
        when(capabilityRepository.findByUuidAndDeletedAtIsNull(capabilityUuid)).thenReturn(Optional.of(capability));

        capabilityService.softDeleteCapabilityByUuid(capabilityUuid);

        verify(capabilityRepository, times(1)).save(capability);
        assertNotNull(capability.getDeletedAt());
    }
}