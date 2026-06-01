package com.karmperis.webinarsapp.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karmperis.webinarsapp.dto.UserReadOnlyDTO;
import com.karmperis.webinarsapp.dto.WebinarInsertDTO;
import com.karmperis.webinarsapp.dto.WebinarReadOnlyDTO;
import com.karmperis.webinarsapp.model.Role;
import com.karmperis.webinarsapp.model.User;
import com.karmperis.webinarsapp.service.IWebinarService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebinarRestController.class)
@AutoConfigureMockMvc(addFilters = false)
public class WebinarRestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IWebinarService webinarService;

    @MockitoBean
    private com.karmperis.webinarsapp.authentication.JwtService jwtService;

    @MockitoBean
    private org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("POST /api/v1/webinars - Should return 201 Created")
    void createWebinar_ReturnsCreated() throws Exception {
        WebinarInsertDTO insertDTO = new WebinarInsertDTO(
                "Title",
                "Desc",
                Instant.parse("2026-12-01T10:00:00Z"),
                60
        );

        UUID organizerUuid = UUID.randomUUID();
        UUID webinarUuid = UUID.randomUUID();
        UserReadOnlyDTO organizer = new UserReadOnlyDTO(
                organizerUuid, "organizer", true, 1L, "ADMIN", "John", "Doe", "+306900000000"
        );

        WebinarReadOnlyDTO responseDTO = new WebinarReadOnlyDTO(
                webinarUuid, "Title",
                "Desc",
                Instant.parse("2026-12-01T10:00:00Z"),
                60,
                organizer
        );

        Role role = new Role();
        role.setName("ADMIN");

        User mockUser = new User();
        mockUser.setUuid(organizerUuid);
        mockUser.setRole(role);

        when(webinarService.saveWebinar(any(WebinarInsertDTO.class), eq(organizerUuid))).thenReturn(responseDTO);

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());

        mockMvc.perform(post("/api/v1/webinars")
                        .with(request -> {
                            request.setUserPrincipal(authenticationToken);
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(insertDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").value(webinarUuid.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/webinars/{uuid} - Should return 200 OK")
    void getWebinarByUuid_ReturnsOk() throws Exception {
        UUID uuid = UUID.randomUUID();
        UserReadOnlyDTO organizer = new UserReadOnlyDTO(
                UUID.randomUUID(), "organizer", true, 1L, "ADMIN", "John", "Doe", "+306900000000");
        WebinarReadOnlyDTO responseDTO = new WebinarReadOnlyDTO(
                uuid, "Title",
                "Desc",
                Instant.parse("2026-12-01T10:00:00Z"),
                60,
                organizer
        );

        when(webinarService.findWebinarByUuid(uuid)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/webinars/{uuid}", uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(uuid.toString()));
    }

    @Test
    @DisplayName("POST /api/v1/webinars/{wUuid}/participants/{uUuid} - Should return 204")
    void enrollUser_ReturnsNoContent() throws Exception {
        UUID wUuid = UUID.randomUUID();
        UUID uUuid = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/webinars/{wUuid}/participants/{uUuid}", wUuid, uUuid))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/webinars/{uuid} - Should return 204 No Content")
    void deleteWebinar_ReturnsNoContent() throws Exception {
        UUID uuid = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/webinars/{uuid}", uuid))
                .andExpect(status().isNoContent());
    }
}