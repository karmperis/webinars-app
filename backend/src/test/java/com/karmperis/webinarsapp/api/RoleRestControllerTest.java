package com.karmperis.webinarsapp.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karmperis.webinarsapp.core.exceptions.EntityAlreadyExistsException;
import com.karmperis.webinarsapp.core.exceptions.EntityNotFoundException;
import com.karmperis.webinarsapp.dto.RoleEditDTO;
import com.karmperis.webinarsapp.dto.RoleInsertDTO;
import com.karmperis.webinarsapp.dto.RoleReadOnlyDTO;
import com.karmperis.webinarsapp.service.IRoleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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

@WebMvcTest(RoleRestController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RoleRestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IRoleService roleService;

    @MockitoBean
    private com.karmperis.webinarsapp.authentication.JwtService jwtService;

    @MockitoBean
    private org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("POST /api/v1/roles - Should return 201 Created")
    void createRole_ReturnsCreated() throws Exception {
        RoleInsertDTO insertDTO = new RoleInsertDTO("ADMIN");
        UUID uuid = UUID.randomUUID();
        RoleReadOnlyDTO responseDTO = new RoleReadOnlyDTO(uuid, "ADMIN");

        when(roleService.saveRole(any(RoleInsertDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(insertDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").value(uuid.toString()))
                .andExpect(jsonPath("$.name").value("ADMIN"));
    }

    @Test
    @DisplayName("GET /api/v1/roles/{uuid} - Should return 200 OK")
    void getRoleByUuid_ReturnsOk() throws Exception {
        UUID uuid = UUID.randomUUID();
        RoleReadOnlyDTO responseDTO = new RoleReadOnlyDTO(uuid, "ADMIN");

        when(roleService.findRoleByUuid(uuid)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/roles/{uuid}", uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(uuid.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/roles - Should return list of roles")
    void getAllRoles_ReturnsList() throws Exception {
        RoleReadOnlyDTO dto = new RoleReadOnlyDTO(UUID.randomUUID(), "USER");
        when(roleService.findAllRolesSortedByName()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("USER"));
    }

    @Test
    @DisplayName("PUT /api/v1/roles/{uuid} - Should return 200 OK")
    void updateRole_ReturnsOk() throws Exception {
        UUID uuid = UUID.randomUUID();
        RoleEditDTO editDTO = new RoleEditDTO("SUPER_ADMIN");
        RoleReadOnlyDTO responseDTO = new RoleReadOnlyDTO(uuid, "SUPER_ADMIN");

        when(roleService.updateRole(eq(uuid), any(RoleEditDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/v1/roles/{uuid}", uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("SUPER_ADMIN"));

        verify(roleService).updateRole(eq(uuid), any(RoleEditDTO.class));
    }

    @Test
    @DisplayName("DELETE /api/v1/roles/{uuid} - Should return 204 No Content")
    void deleteRole_ReturnsNoContent() throws Exception {
        UUID uuid = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/roles/{uuid}", uuid))
                .andExpect(status().isNoContent());

        verify(roleService).softDeleteRoleByUuid(uuid);
    }

    @Test
    @DisplayName("POST /api/v1/roles/{roleUuid}/capabilities/{capabilityUuid} - Should return 200 OK")
    void assignCapability_ReturnsOk() throws Exception {
        UUID roleUuid = UUID.randomUUID();
        UUID capUuid = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/roles/{roleUuid}/capabilities/{capabilityUuid}", roleUuid, capUuid))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/roles/{roleUuid}/capabilities/view - Should return list of capabilities")
    void getRoleCapabilities_ReturnsList() throws Exception {
        UUID roleUuid = UUID.randomUUID();
        com.karmperis.webinarsapp.dto.CapabilityReadOnlyDTO capDto = new com.karmperis.webinarsapp.dto.CapabilityReadOnlyDTO(UUID.randomUUID(), "CAN_VIEW_REPORTS", "can view reports");
        when(roleService.findCapabilitiesByRoleUuid(roleUuid)).thenReturn(List.of(capDto));

        mockMvc.perform(get("/api/v1/roles/{roleUuid}/capabilities/view", roleUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("CAN_VIEW_REPORTS"));
    }

    @Test
    @DisplayName("POST /api/v1/roles - Should return 400 Bad Request when request is invalid")
    void createRole_WithInvalidRequest_ReturnsBadRequest() throws Exception {
        RoleInsertDTO insertDTO = new RoleInsertDTO(null);

        mockMvc.perform(post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(insertDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/roles/{uuid} - Should return 400 Bad Request when request is invalid")
    void updateRole_WithInvalidRequest_ReturnsBadRequest() throws Exception {
        UUID uuid = UUID.randomUUID();
        RoleEditDTO editDTO = new RoleEditDTO(null);

        mockMvc.perform(put("/api/v1/roles/{uuid}", uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/roles/{uuid} - Should return 404 when role is not found")
    void getRoleByUuid_WhenRoleDoesNotExist_ReturnsNotFound() throws Exception {
        UUID uuid = UUID.randomUUID();

        when(roleService.findRoleByUuid(uuid))
                .thenThrow(new EntityNotFoundException("Role", "Role with uuid " + uuid + " not found"));

        mockMvc.perform(get("/api/v1/roles/{uuid}", uuid))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/roles - Should return 409 Conflict when role already exists")
    void createRole_WhenRoleAlreadyExists_ReturnsConflict() throws Exception {
        RoleInsertDTO insertDTO = new RoleInsertDTO("ADMIN");

        when(roleService.saveRole(any(RoleInsertDTO.class)))
                .thenThrow(new EntityAlreadyExistsException("Role", "Role already exists"));

        mockMvc.perform(post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(insertDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/v1/roles/{uuid} - Should return 404 when role does not exist")
    void updateRole_WhenRoleDoesNotExist_ReturnsNotFound() throws Exception {
        UUID uuid = UUID.randomUUID();
        RoleEditDTO editDTO = new RoleEditDTO("SUPER_ADMIN");

        when(roleService.updateRole(eq(uuid), any(RoleEditDTO.class)))
                .thenThrow(new EntityNotFoundException("Role", "Role with uuid " + uuid + " not found"));

        mockMvc.perform(put("/api/v1/roles/{uuid}", uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/roles/{uuid} - Should return 404 when role does not exist")
    void deleteRole_WhenRoleDoesNotExist_ReturnsNotFound() throws Exception {
        UUID uuid = UUID.randomUUID();

        doThrow(new EntityNotFoundException("Role", "Role with uuid " + uuid + " not found"))
                .when(roleService).softDeleteRoleByUuid(uuid);

        mockMvc.perform(delete("/api/v1/roles/{uuid}", uuid))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/roles/{roleUuid}/capabilities/{capabilityUuid} - Should return 404 when role or capability does not exist")
    void assignCapability_WhenRoleOrCapabilityDoesNotExist_ReturnsNotFound() throws Exception {
        UUID roleUuid = UUID.randomUUID();
        UUID capabilityUuid = UUID.randomUUID();

        doThrow(new EntityNotFoundException("Role/Capability", "Role or capability not found"))
                .when(roleService).assignCapabilityToRole(roleUuid, capabilityUuid);

        mockMvc.perform(post("/api/v1/roles/{roleUuid}/capabilities/{capabilityUuid}", roleUuid, capabilityUuid))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/roles/{roleUuid}/capabilities/{capabilityUuid} - Should return 409 when capability is already assigned")
    void assignCapability_WhenCapabilityAlreadyAssigned_ReturnsConflict() throws Exception {
        UUID roleUuid = UUID.randomUUID();
        UUID capabilityUuid = UUID.randomUUID();

        doThrow(new EntityAlreadyExistsException("RoleCapability", "Capability already assigned to role"))
                .when(roleService).assignCapabilityToRole(roleUuid, capabilityUuid);

        mockMvc.perform(post("/api/v1/roles/{roleUuid}/capabilities/{capabilityUuid}", roleUuid, capabilityUuid))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /api/v1/roles/{roleUuid}/capabilities/view - Should return 404 when role does not exist")
    void getRoleCapabilities_WhenRoleDoesNotExist_ReturnsNotFound() throws Exception {
        UUID roleUuid = UUID.randomUUID();

        when(roleService.findCapabilitiesByRoleUuid(roleUuid))
                .thenThrow(new EntityNotFoundException("Role", "Role with uuid " + roleUuid + " not found"));

        mockMvc.perform(get("/api/v1/roles/{roleUuid}/capabilities/view", roleUuid))
                .andExpect(status().isNotFound());
    }
}