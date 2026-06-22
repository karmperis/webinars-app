package com.karmperis.webinarsapp.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karmperis.webinarsapp.core.exceptions.EntityAlreadyExistsException;
import com.karmperis.webinarsapp.core.exceptions.EntityNotFoundException;
import com.karmperis.webinarsapp.dto.UserAdminEditDTO;
import com.karmperis.webinarsapp.dto.UserEditDTO;
import com.karmperis.webinarsapp.dto.UserInsertDTO;
import com.karmperis.webinarsapp.dto.UserReadOnlyDTO;
import com.karmperis.webinarsapp.service.IUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserRestController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserRestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IUserService userService;

    @MockitoBean
    private com.karmperis.webinarsapp.authentication.JwtService jwtService;

    @MockitoBean
    private org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("POST /api/v1/users - Should return 201 Created")
    void createUser_ReturnsCreated() throws Exception {
        UserInsertDTO insertDTO = new UserInsertDTO("testuser", "StrongPass123!", "John", "Doe", "+306900000000");
        UUID uuid = UUID.randomUUID();
        UserReadOnlyDTO responseDTO = new UserReadOnlyDTO(uuid, "testuser", true, UUID.randomUUID(), "ADMIN", "John", "Doe", "+306900000000");

        when(userService.saveUser(any(UserInsertDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(insertDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").value(uuid.toString()))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("GET /api/v1/users/{uuid} - Should return 200 OK")
    void getUserByUserUuid_ReturnsOk() throws Exception {
        UUID uuid = UUID.randomUUID();
        UserReadOnlyDTO responseDTO = new UserReadOnlyDTO(uuid, "testuser", true, UUID.randomUUID(), "ADMIN", "John", "Doe", "+306900000000");

        when(userService.findUserByUuid(uuid)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/users/{uuid}", uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(uuid.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/users - Should return paginated users")
    void getAllUsers_ReturnsPage() throws Exception {
        UserReadOnlyDTO dto = new UserReadOnlyDTO(UUID.randomUUID(), "testuser", true, UUID.randomUUID(), "ADMIN", "John", "Doe", "+306900000000");
        Page<UserReadOnlyDTO> page = new PageImpl<>(List.of(dto));

        when(userService.findAllUsersSortedByName(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("testuser"));
    }

    @Test
    @DisplayName("PUT /api/v1/users/{uuid} - Should return 200 OK")
    void updateUser_ReturnsOk() throws Exception {
        UUID uuid = UUID.randomUUID();
        UserEditDTO editDTO = new UserEditDTO("John", "Doe", "+306900000000");
        UserReadOnlyDTO responseDTO = new UserReadOnlyDTO(uuid, "testuser", true, UUID.randomUUID(), "ADMIN", "John", "Doe", "+306900000000");

        when(userService.updateUser(eq(uuid), any(UserEditDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/v1/users/{uuid}", uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastname").value("Doe"));
    }

    @Test
    @DisplayName("PATCH /api/v1/users/{uuid}/access - Should return 200 OK")
    void updateUserAccess_ReturnsOk() throws Exception {
        UUID uuid = UUID.randomUUID();
        UserAdminEditDTO adminEditDTO = new UserAdminEditDTO(UUID.randomUUID(), true);
        UserReadOnlyDTO responseDTO = new UserReadOnlyDTO(uuid, "testuser", true, UUID.randomUUID(), "ADMIN", "John", "Doe", "+306900000000");

        when(userService.updateUserAccess(eq(uuid), any(UserAdminEditDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/v1/users/{uuid}/access", uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminEditDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{uuid} - Should return 204 No Content")
    void deleteUser_ReturnsNoContent() throws Exception {
        UUID uuid = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/users/{uuid}", uuid))
                .andExpect(status().isNoContent());

        verify(userService).softDeleteUserByUuid(uuid);
    }

    @Test
    @DisplayName("POST /api/v1/users - Should return 400 Bad Request when request is invalid")
    void createUser_WithInvalidRequest_ReturnsBadRequest() throws Exception {
        UserInsertDTO insertDTO = new UserInsertDTO(null, "StrongPass123!", "John", "Doe", "+306900000000");

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(insertDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/users - Should return 409 Conflict when user already exists")
    void createUser_WhenUserAlreadyExists_ReturnsConflict() throws Exception {
        UserInsertDTO insertDTO = new UserInsertDTO("testuser", "StrongPass123!", "John", "Doe", "+306900000000");

        when(userService.saveUser(any(UserInsertDTO.class)))
                .thenThrow(new EntityAlreadyExistsException("User", "User already exists"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(insertDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /api/v1/users/{uuid} - Should return 404 when user is not found")
    void getUserByUserUuid_WhenUserDoesNotExist_ReturnsNotFound() throws Exception {
        UUID uuid = UUID.randomUUID();

        when(userService.findUserByUuid(uuid))
                .thenThrow(new EntityNotFoundException("User", "User with uuid " + uuid + " not found"));

        mockMvc.perform(get("/api/v1/users/{uuid}", uuid))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/users/{uuid} - Should return 400 Bad Request when request is invalid")
    void updateUser_WithInvalidRequest_ReturnsBadRequest() throws Exception {
        UUID uuid = UUID.randomUUID();
        UserEditDTO editDTO = new UserEditDTO(null, "Doe", "+306900000000");

        mockMvc.perform(put("/api/v1/users/{uuid}", uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/users/{uuid} - Should return 404 when user does not exist")
    void updateUser_WhenUserDoesNotExist_ReturnsNotFound() throws Exception {
        UUID uuid = UUID.randomUUID();
        UserEditDTO editDTO = new UserEditDTO("John", "Doe", "+306900000000");

        when(userService.updateUser(eq(uuid), any(UserEditDTO.class)))
                .thenThrow(new EntityNotFoundException("User", "User with uuid " + uuid + " not found"));

        mockMvc.perform(put("/api/v1/users/{uuid}", uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/v1/users/{uuid}/access - Should return 400 Bad Request when request is invalid")
    void updateUserAccess_WithInvalidRequest_ReturnsBadRequest() throws Exception {
        UUID uuid = UUID.randomUUID();
        UserAdminEditDTO adminEditDTO = new UserAdminEditDTO(null, true);

        mockMvc.perform(patch("/api/v1/users/{uuid}/access", uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminEditDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/v1/users/{uuid}/access - Should return 404 when user does not exist")
    void updateUserAccess_WhenUserDoesNotExist_ReturnsNotFound() throws Exception {
        UUID uuid = UUID.randomUUID();
        UserAdminEditDTO adminEditDTO = new UserAdminEditDTO(UUID.randomUUID(), true);

        when(userService.updateUserAccess(eq(uuid), any(UserAdminEditDTO.class)))
                .thenThrow(new EntityNotFoundException("User", "User with uuid " + uuid + " not found"));

        mockMvc.perform(patch("/api/v1/users/{uuid}/access", uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminEditDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{uuid} - Should return 404 when user does not exist")
    void deleteUser_WhenUserDoesNotExist_ReturnsNotFound() throws Exception {
        UUID uuid = UUID.randomUUID();

        doThrow(new EntityNotFoundException("User", "User with uuid " + uuid + " not found"))
                .when(userService).softDeleteUserByUuid(uuid);

        mockMvc.perform(delete("/api/v1/users/{uuid}", uuid))
                .andExpect(status().isNotFound());
    }
}