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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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
    private CapabilityMapper capabilityMapper;

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
        verifyNoInteractions(roleRepository);
        verifyNoInteractions(roleMapper);
    }

    @Test
    @DisplayName("saveRole: Should throw exception when name already exists")
    void saveRole_ThrowsEntityAlreadyExistsException() {
        RoleInsertDTO dto = new RoleInsertDTO("ADMIN");

        when(roleRepository.findByNameAndDeletedAtIsNull("ADMIN")).thenReturn(Optional.of(role));

        assertThrows(EntityAlreadyExistsException.class, () -> roleService.saveRole(dto));
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("saveRole: Should throw exception when name is blank")
    void saveRole_ThrowsEntityInvalidArgumentException_WhenNameIsBlank() {
        RoleInsertDTO dto = new RoleInsertDTO("   ");

        assertThrows(EntityInvalidArgumentException.class, () -> roleService.saveRole(dto));

        verifyNoInteractions(roleRepository);
        verifyNoInteractions(roleMapper);
    }

    @Test
    @DisplayName("saveRole: Should throw exception when name is too short")
    void saveRole_ThrowsEntityInvalidArgumentException_WhenNameIsTooShort() {
        RoleInsertDTO dto = new RoleInsertDTO("ADM");

        assertThrows(EntityInvalidArgumentException.class, () -> roleService.saveRole(dto));

        verifyNoInteractions(roleRepository);
        verifyNoInteractions(roleMapper);
    }

    @Test
    @DisplayName("saveRole: Should throw exception when name is too long")
    void saveRole_ThrowsEntityInvalidArgumentException_WhenNameIsTooLong() {
        String longName = "A".repeat(51);
        RoleInsertDTO dto = new RoleInsertDTO(longName);

        assertThrows(EntityInvalidArgumentException.class, () -> roleService.saveRole(dto));

        verifyNoInteractions(roleRepository);
        verifyNoInteractions(roleMapper);
    }

    // ==========================================
    // TESTS-FIND ALL
    // ==========================================

    @Test
    @DisplayName("findAllRolesSortedByName: Should return all active roles sorted by name")
    void findAllRolesSortedByName_Success() {
        Role secondRole = new Role();
        secondRole.setUuid(UUID.randomUUID());
        secondRole.setName("ORGANIZER");

        RoleReadOnlyDTO firstDTO = new RoleReadOnlyDTO(roleUuid, "ADMIN");
        RoleReadOnlyDTO secondDTO = new RoleReadOnlyDTO(secondRole.getUuid(), "ORGANIZER");

        when(roleRepository.findAllByDeletedAtIsNullOrderByNameAsc())
                .thenReturn(List.of(role, secondRole));

        when(roleMapper.mapToRoleReadOnlyDTO(role))
                .thenReturn(firstDTO);

        when(roleMapper.mapToRoleReadOnlyDTO(secondRole))
                .thenReturn(secondDTO);

        List<RoleReadOnlyDTO> result = roleService.findAllRolesSortedByName();

        assertEquals(2, result.size());
        assertEquals("ADMIN", result.get(0).name());
        assertEquals("ORGANIZER", result.get(1).name());

        verify(roleRepository).findAllByDeletedAtIsNullOrderByNameAsc();
        verify(roleMapper).mapToRoleReadOnlyDTO(role);
        verify(roleMapper).mapToRoleReadOnlyDTO(secondRole);
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
    // TESTS-FIND CAPABILITIES BY ROLE UUID
    // ==========================================

    @Test
    @DisplayName("findCapabilitiesByRoleUuid: Should return only active capabilities of the role")
    void findCapabilitiesByRoleUuid_Success() throws Exception {
        Capability deletedCapability = new Capability();
        deletedCapability.setUuid(UUID.randomUUID());
        deletedCapability.setName("DELETE_USERS");
        deletedCapability.softDelete();

        role.addCapability(capability);
        role.addCapability(deletedCapability);

        CapabilityReadOnlyDTO capabilityDTO = new CapabilityReadOnlyDTO(
                capabilityUuid,
                "MANAGE_USERS",
                null
        );

        when(roleRepository.findByUuidAndDeletedAtIsNull(roleUuid))
                .thenReturn(Optional.of(role));

        when(capabilityMapper.mapToCapabilityReadOnlyDTO(capability))
                .thenReturn(capabilityDTO);

        var result = roleService.findCapabilitiesByRoleUuid(roleUuid);

        assertEquals(1, result.size());
        assertEquals("MANAGE_USERS", result.getFirst().name());

        verify(roleRepository).findByUuidAndDeletedAtIsNull(roleUuid);
        verify(capabilityMapper).mapToCapabilityReadOnlyDTO(capability);
        verify(capabilityMapper, never()).mapToCapabilityReadOnlyDTO(deletedCapability);
    }

    @Test
    @DisplayName("findCapabilitiesByRoleUuid: Should throw exception when role is not found")
    void findCapabilitiesByRoleUuid_ThrowsEntityNotFoundException_WhenRoleIsNotFound() {
        when(roleRepository.findByUuidAndDeletedAtIsNull(roleUuid))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> roleService.findCapabilitiesByRoleUuid(roleUuid));

        verify(roleRepository).findByUuidAndDeletedAtIsNull(roleUuid);
        verifyNoInteractions(capabilityMapper);
    }

    // ==========================================
    // TESTS-UPDATE/DELETE
    // ==========================================

    @Test
    @DisplayName("updateRole: Should throw exception when DTO is null")
    void updateRole_ThrowsEntityInvalidArgumentException_WhenDtoIsNull() {
        assertThrows(EntityInvalidArgumentException.class,
                () -> roleService.updateRole(roleUuid, null));

        verifyNoInteractions(roleRepository);
        verifyNoInteractions(roleMapper);
    }

    @Test
    @DisplayName("updateRole: Should throw exception when name is blank")
    void updateRole_ThrowsEntityInvalidArgumentException_WhenNameIsBlank() {
        RoleEditDTO editDTO = new RoleEditDTO("   ");

        assertThrows(EntityInvalidArgumentException.class,
                () -> roleService.updateRole(roleUuid, editDTO));

        verifyNoInteractions(roleRepository);
        verifyNoInteractions(roleMapper);
    }

    @Test
    @DisplayName("updateRole: Should throw exception when name is too short")
    void updateRole_ThrowsEntityInvalidArgumentException_WhenNameIsTooShort() {
        RoleEditDTO editDTO = new RoleEditDTO("ADM");

        assertThrows(EntityInvalidArgumentException.class,
                () -> roleService.updateRole(roleUuid, editDTO));

        verifyNoInteractions(roleRepository);
        verifyNoInteractions(roleMapper);
    }

    @Test
    @DisplayName("updateRole: Should throw exception when name is too long")
    void updateRole_ThrowsEntityInvalidArgumentException_WhenNameIsTooLong() {
        String longName = "A".repeat(51);
        RoleEditDTO editDTO = new RoleEditDTO(longName);

        assertThrows(EntityInvalidArgumentException.class,
                () -> roleService.updateRole(roleUuid, editDTO));

        verifyNoInteractions(roleRepository);
        verifyNoInteractions(roleMapper);
    }

    @Test
    @DisplayName("updateRole: Should throw exception when role is not found")
    void updateRole_ThrowsEntityNotFoundException_WhenRoleIsNotFound() {
        RoleEditDTO editDTO = new RoleEditDTO("SUPER_ADMIN");

        when(roleRepository.findByUuidAndDeletedAtIsNull(roleUuid))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> roleService.updateRole(roleUuid, editDTO));

        verify(roleRepository).findByUuidAndDeletedAtIsNull(roleUuid);
        verify(roleRepository, never()).save(any());
        verifyNoInteractions(roleMapper);
    }

    @Test
    @DisplayName("updateRole: Should throw exception when new name already exists")
    void updateRole_ThrowsEntityAlreadyExistsException_WhenNewNameAlreadyExists() {
        RoleEditDTO editDTO = new RoleEditDTO("SUPER_ADMIN");

        Role existingRole = new Role();
        existingRole.setUuid(UUID.randomUUID());
        existingRole.setName("SUPER_ADMIN");

        when(roleRepository.findByUuidAndDeletedAtIsNull(roleUuid))
                .thenReturn(Optional.of(role));

        when(roleRepository.findByNameAndDeletedAtIsNull("SUPER_ADMIN"))
                .thenReturn(Optional.of(existingRole));

        assertThrows(EntityAlreadyExistsException.class,
                () -> roleService.updateRole(roleUuid, editDTO));

        verify(roleRepository).findByUuidAndDeletedAtIsNull(roleUuid);
        verify(roleRepository).findByNameAndDeletedAtIsNull("SUPER_ADMIN");
        verify(roleMapper, never()).mapToRoleEditDTO(any(), any());
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateRole: Should not check duplicate name when name is same ignoring case")
    void updateRole_ShouldNotCheckDuplicateName_WhenNameIsSameIgnoringCase() throws Exception {
        RoleEditDTO editDTO = new RoleEditDTO("admin");
        RoleReadOnlyDTO readOnlyDTO = new RoleReadOnlyDTO(roleUuid, "admin");

        when(roleRepository.findByUuidAndDeletedAtIsNull(roleUuid))
                .thenReturn(Optional.of(role));

        when(roleRepository.save(role))
                .thenReturn(role);

        when(roleMapper.mapToRoleReadOnlyDTO(role))
                .thenReturn(readOnlyDTO);

        RoleReadOnlyDTO result = roleService.updateRole(roleUuid, editDTO);

        assertNotNull(result);
        assertEquals("admin", result.name());

        verify(roleRepository, never()).findByNameAndDeletedAtIsNull(anyString());
        verify(roleMapper).mapToRoleEditDTO(role, editDTO);
        verify(roleRepository).save(role);
    }

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

        verify(roleRepository).findByUuidAndDeletedAtIsNull(roleUuid);
        verify(roleRepository).findByNameAndDeletedAtIsNull("SUPER_ADMIN");
        verify(roleMapper).mapToRoleEditDTO(role, editDTO);
        verify(roleRepository).save(role);
        verify(roleMapper).mapToRoleReadOnlyDTO(role);
    }

    @Test
    @DisplayName("softDeleteRoleByUuid: Should soft delete the role successfully")
    void softDeleteRoleByUuid_Success() throws Exception {
        when(roleRepository.findByUuidAndDeletedAtIsNull(roleUuid)).thenReturn(Optional.of(role));

        roleService.softDeleteRoleByUuid(roleUuid);

        verify(roleRepository, times(1)).save(role);
        assertNotNull(role.getDeletedAt());

        verify(roleRepository).findByUuidAndDeletedAtIsNull(roleUuid);
    }

    @Test
    @DisplayName("softDeleteRoleByUuid: Should throw exception when role is not found")
    void softDeleteRoleByUuid_ThrowsEntityNotFoundException_WhenRoleIsNotFound() {
        when(roleRepository.findByUuidAndDeletedAtIsNull(roleUuid))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> roleService.softDeleteRoleByUuid(roleUuid));

        verify(roleRepository).findByUuidAndDeletedAtIsNull(roleUuid);
        verify(roleRepository, never()).save(any());
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

    @Test
    @DisplayName("assignCapabilityToRole: Should throw exception when capability is not found")
    void assignCapabilityToRole_ThrowsEntityNotFoundException_WhenCapabilityNotFound() {
        when(roleRepository.findByUuidAndDeletedAtIsNull(roleUuid))
                .thenReturn(Optional.of(role));

        when(capabilityRepository.findByUuidAndDeletedAtIsNull(capabilityUuid))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> roleService.assignCapabilityToRole(roleUuid, capabilityUuid));

        verify(roleRepository).findByUuidAndDeletedAtIsNull(roleUuid);
        verify(capabilityRepository).findByUuidAndDeletedAtIsNull(capabilityUuid);
    }

    @Test
    @DisplayName("assignCapabilityToRole: Should throw exception when capability is already assigned")
    void assignCapabilityToRole_ThrowsEntityAlreadyExistsException_WhenCapabilityAlreadyAssigned() {
        role.addCapability(capability);

        when(roleRepository.findByUuidAndDeletedAtIsNull(roleUuid))
                .thenReturn(Optional.of(role));

        when(capabilityRepository.findByUuidAndDeletedAtIsNull(capabilityUuid))
                .thenReturn(Optional.of(capability));

        assertThrows(EntityAlreadyExistsException.class,
                () -> roleService.assignCapabilityToRole(roleUuid, capabilityUuid));

        verify(roleRepository).findByUuidAndDeletedAtIsNull(roleUuid);
        verify(capabilityRepository).findByUuidAndDeletedAtIsNull(capabilityUuid);
    }
}