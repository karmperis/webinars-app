package com.karmperis.webinarsapp.service;

import com.karmperis.webinarsapp.core.exceptions.EntityAlreadyExistsException;
import com.karmperis.webinarsapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.webinarsapp.core.exceptions.EntityNotFoundException;
import com.karmperis.webinarsapp.dto.UserAdminEditDTO;
import com.karmperis.webinarsapp.dto.UserEditDTO;
import com.karmperis.webinarsapp.dto.UserInsertDTO;
import com.karmperis.webinarsapp.dto.UserReadOnlyDTO;
import com.karmperis.webinarsapp.mapper.UserMapper;
import com.karmperis.webinarsapp.model.Role;
import com.karmperis.webinarsapp.model.User;
import com.karmperis.webinarsapp.repository.RoleRepository;
import com.karmperis.webinarsapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Default {@link IUserService} implementation.
 * Handles user CRUD operations and applies soft-delete semantics (deleted users are excluded
 * from read operations).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements IUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create and persist a new user.
     *
     * @param dto user creation data
     * @return the persisted user as a read-only DTO
     * @throws EntityAlreadyExistsException   if a non-deleted user with the same username already exists
     * @throws EntityInvalidArgumentException if the provided user data is invalid
     */
    @Override
    @Transactional(rollbackFor = {EntityAlreadyExistsException.class, EntityInvalidArgumentException.class})
    public UserReadOnlyDTO saveUser(UserInsertDTO dto) throws EntityAlreadyExistsException, EntityInvalidArgumentException {
        // Defensive programming: Guard clause for unit tests and internal calls that bypass Web-layer validation
        if (dto == null) {
            throw new EntityInvalidArgumentException("User", "User data cannot be null");
        }

        // Defensive programming: structural validation enforced at service level even though checked by DTO bean validation
        validateUserData(dto.username());

        log.info("Attempting to save new user with username: {}", dto.username());

        try {
            if (userRepository.existsByUsernameAndDeletedAtIsNull(dto.username())) {
                throw new EntityAlreadyExistsException("User", "Username '" + dto.username() + "' already exists");
            }

            User user = userMapper.mapToUserEntity(dto);

            Role defaultRole = roleRepository.findByNameAndDeletedAtIsNull("PARTICIPANT")
                    .orElseThrow(() -> new EntityInvalidArgumentException("Role", "Default role 'PARTICIPANT' not found"));

            user.setRole(defaultRole);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setUuid(UUID.randomUUID());
            user.setActive(true);

            User savedUser = userRepository.save(user);
            log.info("User saved successfully with UUID: {}", savedUser.getUuid());
            return userMapper.mapToUserReadOnlyDTO(savedUser);

        } catch (EntityAlreadyExistsException | EntityInvalidArgumentException e) {
            log.warn("Failed to save user: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieve a page of active (non-deleted) users sorted based on paging and sorting information.
     *
     * @param pageable pagination and sorting information
     * @return a page of active users mapped to read-only DTOs
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserReadOnlyDTO> findAllUsersSortedByName(Pageable pageable) {
        log.info("Fetching a page of active users based on pageable configuration");

        Page<User> usersPage = userRepository.findByDeletedAtIsNull(pageable);
        return usersPage.map(userMapper::mapToUserReadOnlyDTO);
    }

    /**
     * Retrieve a non-deleted user by UUID.
     *
     * @param uuid user UUID
     * @return the matching user mapped to a read-only DTO
     * @throws EntityNotFoundException if no non-deleted user with the given UUID exists
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwnProfile(#uuid, authentication)")
    public UserReadOnlyDTO findUserByUuid(UUID uuid) throws EntityNotFoundException {
        log.info("Searching for user with UUID: {}", uuid);

        return userRepository.findByUuidAndDeletedAtIsNull(uuid)
                .map(userMapper::mapToUserReadOnlyDTO)
                .orElseThrow(() -> {
                    log.warn("User with UUID {} not found", uuid);
                    return new EntityNotFoundException("User", "User with UUID " + uuid + " not found");
                });
    }

    /**
     * Retrieve a non-deleted user by username.
     *
     * @param username the username to search for
     * @return the matching user mapped to a read-only DTO
     * @throws EntityNotFoundException if no non-deleted user with the given username exists
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public UserReadOnlyDTO findUserByUsername(String username) throws EntityNotFoundException {
        log.info("Searching for user with username: {}", username);

        return userRepository.findByUsernameAndDeletedAtIsNull(username)
                .map(userMapper::mapToUserReadOnlyDTO)
                .orElseThrow(() -> {
                    log.warn("User with username {} not found", username);
                    return new EntityNotFoundException("User", "User with username " + username + " not found");
                });
    }

    /**
     * Update an existing non-deleted user.
     *
     * @param uuid user UUID
     * @param dto  updated user data
     * @return the updated user mapped to a read-only DTO
     * @throws EntityNotFoundException        if no non-deleted user with the given UUID exists
     * @throws EntityInvalidArgumentException if the provided user data is invalid
     */
    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class, EntityInvalidArgumentException.class})
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwnProfile(#uuid, authentication)")
    public UserReadOnlyDTO updateUser(UUID uuid, UserEditDTO dto) throws EntityNotFoundException, EntityInvalidArgumentException {
        // Defensive programming: Guard clause for unit tests and internal calls that bypass Web-layer validation
        if (dto == null) {
            throw new EntityInvalidArgumentException("User", "User data cannot be null");
        }

        log.info("Updating user with UUID: {}", uuid);

        User user = userRepository.findByUuidAndDeletedAtIsNull(uuid)
                .orElseThrow(() -> new EntityNotFoundException("User", "User not found"));

        userMapper.mapToUserEditDTO(user, dto);

        User updatedUser = userRepository.save(user);

        log.info("User with UUID {} updated successfully", uuid);

        return userMapper.mapToUserReadOnlyDTO(updatedUser);
    }

    /**
     * Update an existing user's access rights (role and status).
     * Intended for Administrator use only.
     *
     * @param uuid user UUID
     * @param dto  updated user access data (roleUuid, active)
     * @return the updated user mapped to a read-only DTO
     * @throws EntityNotFoundException        if no non-deleted user with the given UUID exists
     * @throws EntityInvalidArgumentException if the provided role data is invalid
     */
    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class, EntityInvalidArgumentException.class})
    @PreAuthorize("hasRole('ADMIN')")
    public UserReadOnlyDTO updateUserAccess(UUID uuid, UserAdminEditDTO dto) throws EntityNotFoundException, EntityInvalidArgumentException {
        // Defensive programming: Guard clause for unit tests and internal calls that bypass Web-layer validation
        if (dto == null) {
            throw new EntityInvalidArgumentException("User", "User access data cannot be null");
        }

        log.info("Updating access rights for user with UUID: {}", uuid);

        User user = userRepository.findByUuidAndDeletedAtIsNull(uuid)
                .orElseThrow(() -> new EntityNotFoundException("User", "User not found"));

        Role role = roleRepository.findByUuidAndDeletedAtIsNull(dto.roleUuid())
                .orElseThrow(() -> new EntityInvalidArgumentException("Role", "Role not found"));

        user.setRole(role);
        user.setActive(dto.active());

        User updatedUser = userRepository.save(user);
        log.info("User access for UUID {} updated successfully", uuid);

        return userMapper.mapToUserReadOnlyDTO(updatedUser);
    }

    /**
     * Soft-delete a user by setting its deleted timestamp.
     *
     * @param uuid user UUID
     * @throws EntityNotFoundException if no non-deleted user with the given UUID exists
     */
    @Override
    @Transactional(rollbackFor = EntityNotFoundException.class)
    @PreAuthorize("hasRole('ADMIN')")
    public void softDeleteUserByUuid(UUID uuid) throws EntityNotFoundException {
        log.info("Performing soft delete for user with UUID: {}", uuid);

        User user = userRepository.findByUuidAndDeletedAtIsNull(uuid)
                .orElseThrow(() -> new EntityNotFoundException("User", "User not found"));

        user.softDelete();
        user.setActive(false);

        userRepository.save(user);
        log.info("User with UUID {} soft deleted successfully", uuid);
    }

    /**
     * Helper method for defensive structural validation of user data.
     */
    private void validateUserData(String username) throws EntityInvalidArgumentException {
        if (username == null || username.isBlank()) {
            throw new EntityInvalidArgumentException("User", "Username cannot be blank");
        }

        int usernameLength = username.trim().length();
        if (usernameLength < 4 || usernameLength > 50) {
            throw new EntityInvalidArgumentException("User", "Username must contain between 4 and 50 characters");
        }
    }
}