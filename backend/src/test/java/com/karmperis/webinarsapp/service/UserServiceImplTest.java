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
import com.karmperis.webinarsapp.repository.UserDetailRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDetailRepository userDetailRepository;

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
        role.setName("USER");

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
        UserInsertDTO dto = new UserInsertDTO("testuser", "password123", 1L, "John", "Doe", "+306900000000");
        UserReadOnlyDTO readOnlyDTO = new UserReadOnlyDTO(userUuid, "testuser", false, 1L, "USER", "John", "Doe", "+306900000000");

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
        assertEquals("testuser", result.username());
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
    @DisplayName("saveUser: Should throw Exception when username already exists")
    void saveUser_ThrowsEntityAlreadyExistsException() {
        UserInsertDTO dto = new UserInsertDTO("testuser", "password123", 1L, "John", "Doe", "+306900000000");

        when(userRepository.existsByUsernameAndDeletedAtIsNull("testuser")).thenReturn(true);

        assertThrows(EntityAlreadyExistsException.class, () -> userService.saveUser(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("saveUser: Should throw Exception when Role does not exist")
    void saveUser_ThrowsEntityInvalidArgumentException_WhenRoleNotFound() {
        UserInsertDTO dto = new UserInsertDTO("testuser", "password1234!N", 1L, "John", "Doe", "+306900000000");
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
        UserReadOnlyDTO readOnlyDTO = new UserReadOnlyDTO(userUuid, "testuser", true, 1L, "USER", "John", "Doe", "+306900000000");

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
    @DisplayName("findAllUsersSortedByName: Should return a Page of Users")
    void findAllUsersSortedByName_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(user));
        UserReadOnlyDTO readOnlyDTO = new UserReadOnlyDTO(userUuid, "testuser", true, 1L, "USER", "John", "Doe", "+306900000000");

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
    @DisplayName("updateUser: Should update User successfully")
    void updateUser_Success() throws Exception {
        UserEditDTO editDTO = new UserEditDTO("John", "Doe", "+306900000000");
        UserReadOnlyDTO readOnlyDTO = new UserReadOnlyDTO(userUuid, "John", true, 1L, "USER", "John", "Doe", "+306900000000");

        when(userRepository.findByUuidAndDeletedAtIsNull(userUuid)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.mapToUserReadOnlyDTO(user)).thenReturn(readOnlyDTO);

        UserReadOnlyDTO result = userService.updateUser(userUuid, editDTO);

        assertNotNull(result);
        assertEquals("John", result.username());
    }

    @Test
    @DisplayName("softDeleteUserByUuid: Should perform soft delete and deactivate user")
    void softDeleteUserByUuid_Success() throws Exception {
        when(userRepository.findByUuidAndDeletedAtIsNull(userUuid)).thenReturn(Optional.of(user));

        userService.softDeleteUserByUuid(userUuid);

        verify(userRepository, times(1)).save(user);
        assertNotNull(user.getDeletedAt());
        assertFalse(user.getActive());
    }

    // ==========================================
    // TESTS-UPDATE (ADMIN)
    // ==========================================
    @Test
    @DisplayName("updateUserAccess: Should update user role and status successfully (Admin)")
    void updateUserAccess_Success() throws Exception {
        com.karmperis.webinarsapp.dto.UserAdminEditDTO adminEditDTO =
                new com.karmperis.webinarsapp.dto.UserAdminEditDTO(2L, false);

        Role newRole = new Role();
        newRole.setId(2L);
        newRole.setName("TEACHER");

        UserReadOnlyDTO readOnlyDTO = new UserReadOnlyDTO(userUuid, "testuser", false, 2L, "TEACHER", "John", "Doe", "+306900000000");

        when(userRepository.findByUuidAndDeletedAtIsNull(userUuid)).thenReturn(Optional.of(user));
        when(roleRepository.findById(2L)).thenReturn(Optional.of(newRole));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.mapToUserReadOnlyDTO(user)).thenReturn(readOnlyDTO);

        UserReadOnlyDTO result = userService.updateUserAccess(userUuid, adminEditDTO);

        assertNotNull(result);
        assertEquals("TEACHER", result.roleName());
        assertFalse(result.active());
    }
}