package com.karmperis.webinarsapp.api;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.mockito.Mockito.when;
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
    }

    @Test
    @DisplayName("DELETE /api/v1/roles/{uuid} - Should return 204 No Content")
    void deleteRole_ReturnsNoContent() throws Exception {
        UUID uuid = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/roles/{uuid}", uuid))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/v1/roles/{roleUuid}/capabilities/{capabilityUuid} - Should return 200 OK")
    void assignCapability_ReturnsOk() throws Exception {
        UUID roleUuid = UUID.randomUUID();
        UUID capUuid = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/roles/{roleUuid}/capabilities/{capabilityUuid}", roleUuid, capUuid))
                .andExpect(status().isOk());
    }
}