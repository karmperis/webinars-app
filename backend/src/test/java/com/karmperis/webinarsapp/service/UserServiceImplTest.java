package com.karmperis.webinarsapp.service;

import com.karmperis.webinarsapp.core.exceptions.EntityAlreadyExistsException;
import com.karmperis.webinarsapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.webinarsapp.core.exceptions.EntityNotFoundException;
import com.karmperis.webinarsapp.dto.UserEditDTO;
import com.karmperis.webinarsapp.dto.UserInsertDTO;
import com.karmperis.webinarsapp.dto.UserReadOnlyDTO;
import com.karmperis.webinarsapp.mapper.UserMapper;
import com.karmperis.webinarsapp.model.Role;
import com.karmperis.webinarsapp.model.User;
import com.karmperis.webinarsapp.repository.RoleRepository;
import com.karmperis.webinarsapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UUID userUuid;
    private Role role;

    @BeforeEach
    void setUp() {
        userUuid = UUID.randomUUID();

        role = new Role();
        role.setId(1L);
        role.setName("PARTICIPANT");

        user = new User();
        user.setUuid(userUuid);
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setRole(role);
        user.setActive(true);
    }

    // ==========================================
    // TESTS-SAVE
    // ==========================================

    @Test
    @DisplayName("saveUser: Should save and return UserReadOnlyDTO successfully")
    void saveUser_Success() throws Exception {
        UserInsertDTO dto = new UserInsertDTO("testuser", "password123", "John", "Doe", "+306900000000");
        UserReadOnlyDTO readOnlyDTO = new UserReadOnlyDTO(userUuid, "testuser", true, UUID.randomUUID(), "PARTICIPANT", "John", "Doe", "+306900000000");

        User unmappedUser = new User();
        unmappedUser.setUsername("testuser");
        unmappedUser.setPassword("password123");

        when(userRepository.existsByUsernameAndDeletedAtIsNull("testuser")).thenReturn(false);
        when(userMapper.mapToUserEntity(dto)).thenReturn(unmappedUser);
        when(roleRepository.findByNameAndDeletedAtIsNull("PARTICIPANT")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.mapToUserReadOnlyDTO(user)).thenReturn(readOnlyDTO);

        UserReadOnlyDTO result = userService.saveUser(dto);

        assertNotNull(result);
        assertTrue(user.getActive());
        assertEquals("testuser", result.username());
        assertEquals("PARTICIPANT", user.getRole().getName());
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode("password123");
    }

    @Test
    @DisplayName("saveUser: Should throw Exception when DTO is null")
    void saveUser_ThrowsEntityInvalidArgumentException_WhenDtoIsNull() {
        assertThrows(EntityInvalidArgumentException.class, () -> userService.saveUser(null));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("saveUser: Should throw Exception when username is blank")
    void saveUser_ThrowsEntityInvalidArgumentException_WhenUsernameIsBlank() {
        UserInsertDTO dto = new UserInsertDTO("   ", "password123", "John", "Doe", "+306900000000");

        assertThrows(EntityInvalidArgumentException.class, () -> userService.saveUser(dto));

        verifyNoInteractions(userRepository);
        verifyNoInteractions(roleRepository);
        verifyNoInteractions(userMapper);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("saveUser: Should throw Exception when username is too short")
    void saveUser_ThrowsEntityInvalidArgumentException_WhenUsernameIsTooShort() {
        UserInsertDTO dto = new UserInsertDTO("abc", "password123", "John", "Doe", "+306900000000");

        assertThrows(EntityInvalidArgumentException.class, () -> userService.saveUser(dto));

        verifyNoInteractions(userRepository);
        verifyNoInteractions(roleRepository);
        verifyNoInteractions(userMapper);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("saveUser: Should throw Exception when username is too long")
    void saveUser_ThrowsEntityInvalidArgumentException_WhenUsernameIsTooLong() {
        String longUsername = "a".repeat(51);
        UserInsertDTO dto = new UserInsertDTO(longUsername, "password123", "John", "Doe", "+306900000000");

        assertThrows(EntityInvalidArgumentException.class, () -> userService.saveUser(dto));

        verifyNoInteractions(userRepository);
        verifyNoInteractions(roleRepository);
        verifyNoInteractions(userMapper);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("saveUser: Should throw Exception when username already exists")
    void saveUser_ThrowsEntityAlreadyExistsException() {
        UserInsertDTO dto = new UserInsertDTO("testuser", "password123", "John", "Doe", "+306900000000");

        when(userRepository.existsByUsernameAndDeletedAtIsNull("testuser")).thenReturn(true);

        assertThrows(EntityAlreadyExistsException.class, () -> userService.saveUser(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("saveUser: Should throw Exception when Role does not exist")
    void saveUser_ThrowsEntityInvalidArgumentException_WhenRoleNotFound() {
        UserInsertDTO dto = new UserInsertDTO("testuser", "password1234!N", "John", "Doe", "+306900000000");
        User unmappedUser = new User();
        unmappedUser.setUsername("testuser");

        when(userRepository.existsByUsernameAndDeletedAtIsNull("testuser")).thenReturn(false);
        when(userMapper.mapToUserEntity(dto)).thenReturn(unmappedUser);
        when(roleRepository.findByNameAndDeletedAtIsNull("PARTICIPANT")).thenReturn(Optional.empty());

        assertThrows(EntityInvalidArgumentException.class, () -> userService.saveUser(dto));
        verify(userRepository, never()).save(any());
    }

    // ==========================================
    // TESTS-FIND METHODS
    // ==========================================

    @Test
    @DisplayName("findUserByUuid: Should return User when found")
    void findUserByUuid_Success() throws Exception {
        UserReadOnlyDTO readOnlyDTO = new UserReadOnlyDTO(userUuid, "testuser", true, UUID.randomUUID(), "PARTICIPANT", "John", "Doe", "+306900000000");

        when(userRepository.findByUuidAndDeletedAtIsNull(userUuid)).thenReturn(Optional.of(user));
        when(userMapper.mapToUserReadOnlyDTO(user)).thenReturn(readOnlyDTO);

        UserReadOnlyDTO result = userService.findUserByUuid(userUuid);

        assertNotNull(result);
        assertEquals(userUuid, result.uuid());
    }

    @Test
    @DisplayName("findUserByUuid: Should throw Exception when not found")
    void findUserByUuid_ThrowsEntityNotFoundException() {
        when(userRepository.findByUuidAndDeletedAtIsNull(userUuid)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.findUserByUuid(userUuid));
    }

    @Test
    @DisplayName("findUserByUsername: Should return User when found")
    void findUserByUsername_Success() throws Exception {
        UserReadOnlyDTO readOnlyDTO = new UserReadOnlyDTO(
                userUuid,
                "testuser",
                true,
                UUID.randomUUID(),
                "PARTICIPANT",
                "John",
                "Doe",
                "+306900000000"
        );

        when(userRepository.findByUsernameAndDeletedAtIsNull("testuser"))
                .thenReturn(Optional.of(user));

        when(userMapper.mapToUserReadOnlyDTO(user))
                .thenReturn(readOnlyDTO);

        UserReadOnlyDTO result = userService.findUserByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.username());

        verify(userRepository).findByUsernameAndDeletedAtIsNull("testuser");
        verify(userMapper).mapToUserReadOnlyDTO(user);
    }

    @Test
    @DisplayName("findUserByUsername: Should throw Exception when not found")
    void findUserByUsername_ThrowsEntityNotFoundException() {
        when(userRepository.findByUsernameAndDeletedAtIsNull("missinguser"))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> userService.findUserByUsername("missinguser"));

        verify(userRepository).findByUsernameAndDeletedAtIsNull("missinguser");
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("findAllUsersSortedByName: Should return a Page of Users")
    void findAllUsersSortedByName_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(user));
        UserReadOnlyDTO readOnlyDTO = new UserReadOnlyDTO(userUuid, "testuser", true, UUID.randomUUID(), "PARTICIPANT", "John", "Doe", "+306900000000");

        when(userRepository.findByDeletedAtIsNull(pageable)).thenReturn(userPage);
        when(userMapper.mapToUserReadOnlyDTO(user)).thenReturn(readOnlyDTO);

        Page<UserReadOnlyDTO> result = userService.findAllUsersSortedByName(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("testuser", result.getContent().getFirst().username());
    }

    // ==========================================
    // TESTS-UPDATE/DELETE
    // ==========================================

    @Test
    @DisplayName("updateUser: Should throw Exception when DTO is null")
    void updateUser_ThrowsEntityInvalidArgumentException_WhenDtoIsNull() {
        assertThrows(EntityInvalidArgumentException.class,
                () -> userService.updateUser(userUuid, null));

        verifyNoInteractions(userRepository);
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("updateUser: Should throw Exception when user is not found")
    void updateUser_ThrowsEntityNotFoundException_WhenUserIsNotFound() {
        UserEditDTO editDTO = new UserEditDTO("John", "Doe", "+306900000000");

        when(userRepository.findByUuidAndDeletedAtIsNull(userUuid))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> userService.updateUser(userUuid, editDTO));

        verify(userRepository).findByUuidAndDeletedAtIsNull(userUuid);
        verify(userRepository, never()).save(any());
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("updateUser: Should update User successfully")
    void updateUser_Success() throws Exception {
        UserEditDTO editDTO = new UserEditDTO("John", "Doe", "+306900000000");
        UserReadOnlyDTO readOnlyDTO = new UserReadOnlyDTO(userUuid, "testuser", true, UUID.randomUUID(), "PARTICIPANT", "John", "Doe", "+306900000000");

        when(userRepository.findByUuidAndDeletedAtIsNull(userUuid)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.mapToUserReadOnlyDTO(user)).thenReturn(readOnlyDTO);

        UserReadOnlyDTO result = userService.updateUser(userUuid, editDTO);

        assertNotNull(result);
        assertEquals("John", result.firstname());
        assertEquals("Doe", result.lastname());
        assertEquals("+306900000000", result.phoneNumber());

        verify(userRepository).findByUuidAndDeletedAtIsNull(userUuid);
        verify(userMapper).mapToUserEditDTO(user, editDTO);
        verify(userRepository).save(user);
        verify(userMapper).mapToUserReadOnlyDTO(user);
    }

    @Test
    @DisplayName("softDeleteUserByUuid: Should perform soft delete and deactivate user")
    void softDeleteUserByUuid_Success() throws Exception {
        when(userRepository.findByUuidAndDeletedAtIsNull(userUuid)).thenReturn(Optional.of(user));

        userService.softDeleteUserByUuid(userUuid);

        verify(userRepository, times(1)).save(user);
        assertNotNull(user.getDeletedAt());
        assertFalse(user.getActive());

        verify(userRepository).findByUuidAndDeletedAtIsNull(userUuid);
    }

    @Test
    @DisplayName("softDeleteUserByUuid: Should throw Exception when user is not found")
    void softDeleteUserByUuid_ThrowsEntityNotFoundException_WhenUserIsNotFound() {
        when(userRepository.findByUuidAndDeletedAtIsNull(userUuid))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> userService.softDeleteUserByUuid(userUuid));

        verify(userRepository).findByUuidAndDeletedAtIsNull(userUuid);
        verify(userRepository, never()).save(any());
    }

    // ==========================================
    // TESTS-UPDATE (ADMIN)
    // ==========================================

    @Test
    @DisplayName("updateUserAccess: Should throw Exception when DTO is null")
    void updateUserAccess_ThrowsEntityInvalidArgumentException_WhenDtoIsNull() {
        assertThrows(EntityInvalidArgumentException.class,
                () -> userService.updateUserAccess(userUuid, null));

        verifyNoInteractions(userRepository);
        verifyNoInteractions(roleRepository);
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("updateUserAccess: Should throw Exception when user is not found")
    void updateUserAccess_ThrowsEntityNotFoundException_WhenUserIsNotFound() {
        UUID roleUuid = UUID.randomUUID();
        com.karmperis.webinarsapp.dto.UserAdminEditDTO adminEditDTO =
                new com.karmperis.webinarsapp.dto.UserAdminEditDTO(roleUuid, true);

        when(userRepository.findByUuidAndDeletedAtIsNull(userUuid))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> userService.updateUserAccess(userUuid, adminEditDTO));

        verify(userRepository).findByUuidAndDeletedAtIsNull(userUuid);
        verifyNoInteractions(roleRepository);
        verify(userRepository, never()).save(any());
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("updateUserAccess: Should throw Exception when role is not found")
    void updateUserAccess_ThrowsEntityInvalidArgumentException_WhenRoleIsNotFound() {
        UUID roleUuid = UUID.randomUUID();
        com.karmperis.webinarsapp.dto.UserAdminEditDTO adminEditDTO =
                new com.karmperis.webinarsapp.dto.UserAdminEditDTO(roleUuid, true);

        when(userRepository.findByUuidAndDeletedAtIsNull(userUuid))
                .thenReturn(Optional.of(user));

        when(roleRepository.findByUuidAndDeletedAtIsNull(roleUuid))
                .thenReturn(Optional.empty());

        assertThrows(EntityInvalidArgumentException.class,
                () -> userService.updateUserAccess(userUuid, adminEditDTO));

        verify(userRepository).findByUuidAndDeletedAtIsNull(userUuid);
        verify(roleRepository).findByUuidAndDeletedAtIsNull(roleUuid);
        verify(userRepository, never()).save(any());
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("updateUserAccess: Should update user role and status successfully (Admin)")
    void updateUserAccess_Success() throws Exception {
        UUID teacherRoleUuid = UUID.randomUUID();
        com.karmperis.webinarsapp.dto.UserAdminEditDTO adminEditDTO =
                new com.karmperis.webinarsapp.dto.UserAdminEditDTO(teacherRoleUuid, false);

        Role newRole = new Role();
        newRole.setId(2L);
        newRole.setName("TEACHER");

        UserReadOnlyDTO readOnlyDTO = new UserReadOnlyDTO(userUuid, "testuser", false, teacherRoleUuid, "TEACHER", "John", "Doe", "+306900000000");

        when(userRepository.findByUuidAndDeletedAtIsNull(userUuid)).thenReturn(Optional.of(user));
        when(roleRepository.findByUuidAndDeletedAtIsNull(teacherRoleUuid)).thenReturn(Optional.of(newRole));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.mapToUserReadOnlyDTO(user)).thenReturn(readOnlyDTO);

        UserReadOnlyDTO result = userService.updateUserAccess(userUuid, adminEditDTO);

        assertNotNull(result);
        assertEquals("TEACHER", result.roleName());
        assertFalse(result.active());

        verify(userRepository).findByUuidAndDeletedAtIsNull(userUuid);
        verify(roleRepository).findByUuidAndDeletedAtIsNull(teacherRoleUuid);
        verify(userRepository).save(user);
        verify(userMapper).mapToUserReadOnlyDTO(user);
    }
}