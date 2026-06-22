package com.karmperis.webinarsapp.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karmperis.webinarsapp.core.exceptions.EntityAlreadyExistsException;
import com.karmperis.webinarsapp.core.exceptions.EntityNotFoundException;
import com.karmperis.webinarsapp.dto.CapabilityEditDTO;
import com.karmperis.webinarsapp.dto.CapabilityInsertDTO;
import com.karmperis.webinarsapp.dto.CapabilityReadOnlyDTO;
import com.karmperis.webinarsapp.service.ICapabilityService;
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

@WebMvcTest(CapabilityRestController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CapabilityRestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ICapabilityService capabilityService;

    @MockitoBean
    private com.karmperis.webinarsapp.authentication.JwtService jwtService;

    @MockitoBean
    private org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("POST /api/v1/capabilities - Should return 201 Created")
    void createCapability_ReturnsCreated() throws Exception {
        CapabilityInsertDTO insertDTO = new CapabilityInsertDTO("TEST_CAP", "Description");
        UUID uuid = UUID.randomUUID();
        CapabilityReadOnlyDTO responseDTO = new CapabilityReadOnlyDTO(uuid, "TEST_CAP", "Description");

        when(capabilityService.saveCapability(any(CapabilityInsertDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/capabilities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(insertDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").value(uuid.toString()))
                .andExpect(jsonPath("$.name").value("TEST_CAP"));
    }

    @Test
    @DisplayName("GET /api/v1/capabilities/{uuid} - Should return 200 OK")
    void getCapabilityByUuid_ReturnsOk() throws Exception {
        UUID uuid = UUID.randomUUID();
        CapabilityReadOnlyDTO responseDTO = new CapabilityReadOnlyDTO(uuid, "TEST_CAP", "Description");

        when(capabilityService.findCapabilityByUuid(uuid)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/capabilities/{uuid}", uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(uuid.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/capabilities - Should return list of capabilities")
    void getAllCapabilities_ReturnsList() throws Exception {
        CapabilityReadOnlyDTO dto = new CapabilityReadOnlyDTO(UUID.randomUUID(), "CAP1", "Desc");
        when(capabilityService.findAllCapabilitiesSortedByName()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/capabilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("CAP1"));
    }

    @Test
    @DisplayName("PUT /api/v1/capabilities/{uuid} - Should return 200 OK")
    void updateCapability_ReturnsOk() throws Exception {
        UUID uuid = UUID.randomUUID();
        CapabilityEditDTO editDTO = new CapabilityEditDTO("NEW_NAME", "New Desc");
        CapabilityReadOnlyDTO responseDTO = new CapabilityReadOnlyDTO(uuid, "NEW_NAME", "New Desc");

        when(capabilityService.updateCapability(eq(uuid), any(CapabilityEditDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/v1/capabilities/{uuid}", uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NEW_NAME"));
    }

    @Test
    @DisplayName("DELETE /api/v1/capabilities/{uuid} - Should return 204 No Content")
    void deleteCapability_ReturnsNoContent() throws Exception {
        UUID uuid = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/capabilities/{uuid}", uuid))
                .andExpect(status().isNoContent());

        verify(capabilityService).softDeleteCapabilityByUuid(uuid);
    }

    @Test
    @DisplayName("POST /api/v1/capabilities - Should return 400 Bad Request when request is invalid")
    void createCapability_WithInvalidRequest_ReturnsBadRequest() throws Exception {
        CapabilityInsertDTO insertDTO = new CapabilityInsertDTO(null, "Description");

        mockMvc.perform(post("/api/v1/capabilities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(insertDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/capabilities/{uuid} - Should return 400 Bad Request when request is invalid")
    void updateCapability_WithInvalidRequest_ReturnsBadRequest() throws Exception {
        UUID uuid = UUID.randomUUID();
        CapabilityEditDTO editDTO = new CapabilityEditDTO(null, "New Desc");

        mockMvc.perform(put("/api/v1/capabilities/{uuid}", uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/capabilities/{uuid} - Should return 404 when capability is not found")
    void getCapabilityByUuid_WhenCapabilityDoesNotExist_ReturnsNotFound() throws Exception {
        UUID uuid = UUID.randomUUID();

        when(capabilityService.findCapabilityByUuid(uuid))
                .thenThrow(new EntityNotFoundException("Capability", "Capability with uuid " + uuid + " not found"));

        mockMvc.perform(get("/api/v1/capabilities/{uuid}", uuid))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/capabilities - Should return 409 Conflict when capability already exists")
    void createCapability_WhenCapabilityAlreadyExists_ReturnsConflict() throws Exception {
        CapabilityInsertDTO insertDTO = new CapabilityInsertDTO("TEST_CAP", "Description");

        when(capabilityService.saveCapability(any(CapabilityInsertDTO.class)))
                .thenThrow(new EntityAlreadyExistsException("Capability", "Capability already exists"));

        mockMvc.perform(post("/api/v1/capabilities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(insertDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/v1/capabilities/{uuid} - Should return 404 when capability does not exist")
    void updateCapability_WhenCapabilityDoesNotExist_ReturnsNotFound() throws Exception {
        UUID uuid = UUID.randomUUID();
        CapabilityEditDTO editDTO = new CapabilityEditDTO("NEW_NAME", "New Desc");

        when(capabilityService.updateCapability(eq(uuid), any(CapabilityEditDTO.class)))
                .thenThrow(new EntityNotFoundException("Capability", "Capability with uuid " + uuid + " not found"));

        mockMvc.perform(put("/api/v1/capabilities/{uuid}", uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/capabilities/{uuid} - Should return 404 when capability does not exist")
    void deleteCapability_WhenCapabilityDoesNotExist_ReturnsNotFound() throws Exception {
        UUID uuid = UUID.randomUUID();

        doThrow(new EntityNotFoundException("Capability", "Capability with uuid " + uuid + " not found"))
                .when(capabilityService).softDeleteCapabilityByUuid(uuid);

        mockMvc.perform(delete("/api/v1/capabilities/{uuid}", uuid))
                .andExpect(status().isNotFound());
    }
}