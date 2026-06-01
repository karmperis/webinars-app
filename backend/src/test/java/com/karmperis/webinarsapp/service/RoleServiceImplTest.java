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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private CapabilityRepository capabilityRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role role;
    private UUID roleUuid;
    private Capability capability;
    private UUID capabilityUuid;

    @BeforeEach
    void setUp() {
        roleUuid = UUID.randomUUID();
        role = new Role();
        role.setUuid(roleUuid);
        role.setName("ADMIN");

        capabilityUuid = UUID.randomUUID();
        capability = new Capability();
        capability.setUuid(capabilityUuid);
        capability.setName("MANAGE_USERS");
    }

    // ==========================================
    // TESTS-SAVE
    // ==========================================

    @Test
    @DisplayName("saveRole: Should save the role successfully")
    void saveRole_Success() throws Exception {
        RoleInsertDTO dto = new RoleInsertDTO("ADMIN");
        RoleReadOnlyDTO readOnlyDTO = new RoleReadOnlyDTO(roleUuid, "ADMIN");

        when(roleRepository.findByNameAndDeletedAtIsNull("ADMIN")).thenReturn(Optional.empty());
        when(roleMapper.mapToRoleEntity(dto)).thenReturn(role);
        when(roleRepository.save(any(Role.class))).thenReturn(role);
        when(roleMapper.mapToRoleReadOnlyDTO(role)).thenReturn(readOnlyDTO);

        RoleReadOnlyDTO result = roleService.saveRole(dto);

        assertNotNull(result);
        assertEquals("ADMIN", result.name());
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    @DisplayName("saveRole: Should throw exception when DTO is null")
    void saveRole_ThrowsEntityInvalidArgumentException_WhenDtoIsNull() {
        assertThrows(EntityInvalidArgumentException.class, () -> roleService.saveRole(null));
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("saveRole: Should throw exception when name already exists")
    void saveRole_ThrowsEntityAlreadyExistsException() {
        RoleInsertDTO dto = new RoleInsertDTO("ADMIN");

        when(roleRepository.findByNameAndDeletedAtIsNull("ADMIN")).thenReturn(Optional.of(role));

        assertThrows(EntityAlreadyExistsException.class, () -> roleService.saveRole(dto));
        verify(roleRepository, never()).save(any());
    }

    // ==========================================
    // TESTS-FIND BY UUID
    // ==========================================

    @Test
    @DisplayName("findRoleByUuid: Should return the role when found")
    void findRoleByUuid_Success() throws Exception {
        RoleReadOnlyDTO readOnlyDTO = new RoleReadOnlyDTO(roleUuid, "ADMIN");

        when(roleRepository.findByUuidAndDeletedAtIsNull(roleUuid)).thenReturn(Optional.of(role));
        when(roleMapper.mapToRoleReadOnlyDTO(role)).thenReturn(readOnlyDTO);

        RoleReadOnlyDTO result = roleService.findRoleByUuid(roleUuid);

        assertNotNull(result);
        assertEquals(roleUuid, result.uuid());
    }

    @Test
    @DisplayName("findRoleByUuid: Should throw exception when not found")
    void findRoleByUuid_ThrowsEntityNotFoundException() {
        when(roleRepository.findByUuidAndDeletedAtIsNull(roleUuid)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> roleService.findRoleByUuid(roleUuid));
    }

    // ==========================================
    // TESTS-UPDATE/DELETE
    // ==========================================

    @Test
    @DisplayName("updateRole: Should update the role successfully")
    void updateRole_Success() throws Exception {
        RoleEditDTO editDTO = new RoleEditDTO("SUPER_ADMIN");
        RoleReadOnlyDTO readOnlyDTO = new RoleReadOnlyDTO(roleUuid, "SUPER_ADMIN");

        when(roleRepository.findByUuidAndDeletedAtIsNull(roleUuid)).thenReturn(Optional.of(role));
        when(roleRepository.findByNameAndDeletedAtIsNull("SUPER_ADMIN")).thenReturn(Optional.empty());
        when(roleRepository.save(role)).thenReturn(role);
        when(roleMapper.mapToRoleReadOnlyDTO(role)).thenReturn(readOnlyDTO);

        RoleReadOnlyDTO result = roleService.updateRole(roleUuid, editDTO);

        assertNotNull(result);
        assertEquals("SUPER_ADMIN", result.name());
    }

    @Test
    @DisplayName("softDeleteRoleByUuid: Should soft delete the role successfully")
    void softDeleteRoleByUuid_Success() throws Exception {
        when(roleRepository.findByUuidAndDeletedAtIsNull(roleUuid)).thenReturn(Optional.of(role));

        roleService.softDeleteRoleByUuid(roleUuid);

        verify(roleRepository, times(1)).save(role);
        assertNotNull(role.getDeletedAt()); // Ελέγχουμε αν το deletedAt πήρε τιμή
    }

    // ==========================================
    // TESTS-ASSIGN CAPABILITY TO ROLE
    // ==========================================

    @Test
    @DisplayName("assignCapabilityToRole: Should link capability to role successfully")
    void assignCapabilityToRole_Success() {
        when(roleRepository.findByUuidAndDeletedAtIsNull(roleUuid)).thenReturn(Optional.of(role));
        when(capabilityRepository.findByUuidAndDeletedAtIsNull(capabilityUuid)).thenReturn(Optional.of(capability));

        assertDoesNotThrow(() -> roleService.assignCapabilityToRole(roleUuid, capabilityUuid));
        assertFalse(role.getAllCapabilities().isEmpty());
    }

    @Test
    @DisplayName("assignCapabilityToRole: Should throw exception when role not found")
    void assignCapabilityToRole_ThrowsEntityNotFoundException_WhenRoleNotFound() {
        when(roleRepository.findByUuidAndDeletedAtIsNull(roleUuid)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> roleService.assignCapabilityToRole(roleUuid, capabilityUuid));
        verify(capabilityRepository, never()).findByUuidAndDeletedAtIsNull(any());
    }
}