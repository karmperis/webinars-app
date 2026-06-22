package com.karmperis.webinarsapp.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karmperis.webinarsapp.core.exceptions.EntityAlreadyExistsException;
import com.karmperis.webinarsapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.webinarsapp.core.exceptions.EntityNotFoundException;
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
import static org.mockito.Mockito.*;
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
                organizerUuid, "organizer", true, UUID.randomUUID(), "ADMIN", "John", "Doe", "+306900000000"
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

        verify(webinarService).saveWebinar(any(WebinarInsertDTO.class), eq(organizerUuid));
    }

    @Test
    @DisplayName("GET /api/v1/webinars/{uuid} - Should return 200 OK")
    void getWebinarByUuid_ReturnsOk() throws Exception {
        UUID uuid = UUID.randomUUID();
        UserReadOnlyDTO organizer = new UserReadOnlyDTO(
                UUID.randomUUID(), "organizer", true, UUID.randomUUID(), "ADMIN", "John", "Doe", "+306900000000");
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
    @DisplayName("GET /api/v1/webinars - Should return paginated webinars")
    void getAllWebinars_ReturnsPage() throws Exception {
        UserReadOnlyDTO organizer = new UserReadOnlyDTO(
                UUID.randomUUID(), "organizer", true, UUID.randomUUID(), "ADMIN", "John", "Doe", "+306900000000");
        WebinarReadOnlyDTO dto = new WebinarReadOnlyDTO(
                UUID.randomUUID(), "Title", "Desc", Instant.parse("2026-12-01T10:00:00Z"), 60, organizer
        );
        org.springframework.data.domain.Page<WebinarReadOnlyDTO> page = new org.springframework.data.domain.PageImpl<>(java.util.List.of(dto));

        when(webinarService.findAllWebinars(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/webinars")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Title"));
    }

    @Test
    @DisplayName("GET /api/v1/webinars/organizer/{organizerUuid} - Should return paginated webinars by organizer")
    void getWebinarsByOrganizer_ReturnsPage() throws Exception {
        UUID organizerUuid = UUID.randomUUID();
        UserReadOnlyDTO organizer = new UserReadOnlyDTO(
                organizerUuid, "organizer", true, UUID.randomUUID(), "ADMIN", "John", "Doe", "+306900000000");
        WebinarReadOnlyDTO dto = new WebinarReadOnlyDTO(
                UUID.randomUUID(), "Title", "Desc", Instant.parse("2026-12-01T10:00:00Z"), 60, organizer
        );
        org.springframework.data.domain.Page<WebinarReadOnlyDTO> page = new org.springframework.data.domain.PageImpl<>(java.util.List.of(dto));

        when(webinarService.findAllWebinarsByOrganizer(eq(organizerUuid), any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/webinars/organizer/{organizerUuid}", organizerUuid)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].organizer.uuid").value(organizerUuid.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/webinars/participants/{userUuid} - Should return paginated webinars by participant")
    void getWebinarsByParticipant_ReturnsPage() throws Exception {
        UUID userUuid = UUID.randomUUID();
        UserReadOnlyDTO organizer = new UserReadOnlyDTO(
                UUID.randomUUID(), "organizer", true, UUID.randomUUID(), "ADMIN", "John", "Doe", "+306900000000");
        WebinarReadOnlyDTO dto = new WebinarReadOnlyDTO(
                UUID.randomUUID(), "Title", "Desc", Instant.parse("2026-12-01T10:00:00Z"), 60, organizer
        );
        org.springframework.data.domain.Page<WebinarReadOnlyDTO> page = new org.springframework.data.domain.PageImpl<>(java.util.List.of(dto));

        when(webinarService.findAllWebinarsByParticipant(eq(userUuid), any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/webinars/participants/{userUuid}", userUuid)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Title"));
    }

    @Test
    @DisplayName("PUT /api/v1/webinars/{uuid} - Should return 200 OK")
    void updateWebinar_ReturnsOk() throws Exception {
        UUID uuid = UUID.randomUUID();
        com.karmperis.webinarsapp.dto.WebinarEditDTO editDTO = new com.karmperis.webinarsapp.dto.WebinarEditDTO(
                "Updated Title", "Updated Desc", Instant.parse("2026-12-02T10:00:00Z"), 90
        );
        UserReadOnlyDTO organizer = new UserReadOnlyDTO(
                UUID.randomUUID(), "organizer", true, UUID.randomUUID(), "ADMIN", "John", "Doe", "+306900000000");
        WebinarReadOnlyDTO responseDTO = new WebinarReadOnlyDTO(
                uuid, "Updated Title", "Updated Desc", Instant.parse("2026-12-02T10:00:00Z"), 90, organizer
        );

        when(webinarService.updateWebinar(eq(uuid), any(com.karmperis.webinarsapp.dto.WebinarEditDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/v1/webinars/{uuid}", uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));

        verify(webinarService).updateWebinar(eq(uuid), any(com.karmperis.webinarsapp.dto.WebinarEditDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/webinars/{wUuid}/participants/{uUuid} - Should return 204")
    void enrollUser_ReturnsNoContent() throws Exception {
        UUID wUuid = UUID.randomUUID();
        UUID uUuid = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/webinars/{wUuid}/participants/{uUuid}", wUuid, uUuid))
                .andExpect(status().isNoContent());

        verify(webinarService).enrollUserInWebinar(wUuid, uUuid);
    }

    @Test
    @DisplayName("DELETE /api/v1/webinars/{uuid} - Should return 204 No Content")
    void deleteWebinar_ReturnsNoContent() throws Exception {
        UUID uuid = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/webinars/{uuid}", uuid))
                .andExpect(status().isNoContent());

        verify(webinarService).softDeleteWebinarByUuid(uuid);
    }

    @Test
    @DisplayName("POST /api/v1/webinars - Should return 400 Bad Request when request is invalid")
    void createWebinar_WithInvalidRequest_ReturnsBadRequest() throws Exception {
        WebinarInsertDTO insertDTO = new WebinarInsertDTO(
                null,
                "Desc",
                Instant.parse("2026-12-01T10:00:00Z"),
                60
        );

        Role role = new Role();
        role.setName("ADMIN");

        User mockUser = new User();
        mockUser.setUuid(UUID.randomUUID());
        mockUser.setRole(role);

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());

        mockMvc.perform(post("/api/v1/webinars")
                        .with(request -> {
                            request.setUserPrincipal(authenticationToken);
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(insertDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/webinars - Should return 404 when authentication principal is invalid")
    void createWebinar_WithInvalidAuthenticationPrincipal_ReturnsNotFound() throws Exception {
        WebinarInsertDTO insertDTO = new WebinarInsertDTO(
                "Title",
                "Desc",
                Instant.parse("2026-12-01T10:00:00Z"),
                60
        );

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken("not-a-user-principal", null, java.util.List.of());

        mockMvc.perform(post("/api/v1/webinars")
                        .with(request -> {
                            request.setUserPrincipal(authenticationToken);
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(insertDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/webinars/{uuid} - Should return 404 when webinar is not found")
    void getWebinarByUuid_WhenWebinarDoesNotExist_ReturnsNotFound() throws Exception {
        UUID uuid = UUID.randomUUID();

        when(webinarService.findWebinarByUuid(uuid))
                .thenThrow(new EntityNotFoundException("Webinar", "Webinar with uuid " + uuid + " not found"));

        mockMvc.perform(get("/api/v1/webinars/{uuid}", uuid))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/webinars/organizer/{organizerUuid} - Should return 404 when organizer is not found")
    void getWebinarsByOrganizer_WhenOrganizerDoesNotExist_ReturnsNotFound() throws Exception {
        UUID organizerUuid = UUID.randomUUID();

        when(webinarService.findAllWebinarsByOrganizer(eq(organizerUuid), any(org.springframework.data.domain.Pageable.class)))
                .thenThrow(new EntityNotFoundException("User", "Organizer with uuid " + organizerUuid + " not found"));

        mockMvc.perform(get("/api/v1/webinars/organizer/{organizerUuid}", organizerUuid)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/webinars/participants/{userUuid} - Should return 404 when participant is not found")
    void getWebinarsByParticipant_WhenUserDoesNotExist_ReturnsNotFound() throws Exception {
        UUID userUuid = UUID.randomUUID();

        when(webinarService.findAllWebinarsByParticipant(eq(userUuid), any(org.springframework.data.domain.Pageable.class)))
                .thenThrow(new EntityNotFoundException("User", "User with uuid " + userUuid + " not found"));

        mockMvc.perform(get("/api/v1/webinars/participants/{userUuid}", userUuid)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/webinars/{uuid} - Should return 400 Bad Request when request is invalid")
    void updateWebinar_WithInvalidRequest_ReturnsBadRequest() throws Exception {
        UUID uuid = UUID.randomUUID();
        com.karmperis.webinarsapp.dto.WebinarEditDTO editDTO =
                new com.karmperis.webinarsapp.dto.WebinarEditDTO(
                        null,
                        "Updated Desc",
                        Instant.parse("2026-12-02T10:00:00Z"),
                        90
                );

        mockMvc.perform(put("/api/v1/webinars/{uuid}", uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/webinars/{uuid} - Should return 404 when webinar does not exist")
    void updateWebinar_WhenWebinarDoesNotExist_ReturnsNotFound() throws Exception {
        UUID uuid = UUID.randomUUID();
        com.karmperis.webinarsapp.dto.WebinarEditDTO editDTO =
                new com.karmperis.webinarsapp.dto.WebinarEditDTO(
                        "Updated Title",
                        "Updated Desc",
                        Instant.parse("2026-12-02T10:00:00Z"),
                        90
                );

        when(webinarService.updateWebinar(eq(uuid), any(com.karmperis.webinarsapp.dto.WebinarEditDTO.class)))
                .thenThrow(new EntityNotFoundException("Webinar", "Webinar with uuid " + uuid + " not found"));

        mockMvc.perform(put("/api/v1/webinars/{uuid}", uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/webinars/{uuid} - Should return 404 when webinar does not exist")
    void deleteWebinar_WhenWebinarDoesNotExist_ReturnsNotFound() throws Exception {
        UUID uuid = UUID.randomUUID();

        doThrow(new EntityNotFoundException("Webinar", "Webinar with uuid " + uuid + " not found"))
                .when(webinarService).softDeleteWebinarByUuid(uuid);

        mockMvc.perform(delete("/api/v1/webinars/{uuid}", uuid))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/webinars/{wUuid}/participants/{uUuid} - Should return 409 when user is already enrolled")
    void enrollUser_WhenUserAlreadyEnrolled_ReturnsConflict() throws Exception {
        UUID webinarUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();

        doThrow(new EntityAlreadyExistsException("Enrollment", "User already enrolled in webinar"))
                .when(webinarService).enrollUserInWebinar(webinarUuid, userUuid);

        mockMvc.perform(post("/api/v1/webinars/{wUuid}/participants/{uUuid}", webinarUuid, userUuid))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/v1/webinars/{wUuid}/participants/{uUuid} - Should return 400 when enrollment is invalid")
    void enrollUser_WhenEnrollmentIsInvalid_ReturnsBadRequest() throws Exception {
        UUID webinarUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();

        doThrow(new EntityInvalidArgumentException("INVALID_ENROLLMENT", "Invalid enrollment request"))
                .when(webinarService).enrollUserInWebinar(webinarUuid, userUuid);

        mockMvc.perform(post("/api/v1/webinars/{wUuid}/participants/{uUuid}", webinarUuid, userUuid))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/webinars/{wUuid}/participants/{uUuid} - Should return 404 when webinar or user does not exist")
    void enrollUser_WhenWebinarOrUserDoesNotExist_ReturnsNotFound() throws Exception {
        UUID webinarUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();

        doThrow(new EntityNotFoundException("Enrollment", "Webinar or user not found"))
                .when(webinarService).enrollUserInWebinar(webinarUuid, userUuid);

        mockMvc.perform(post("/api/v1/webinars/{wUuid}/participants/{uUuid}", webinarUuid, userUuid))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/webinars - Should return 409 when webinar already exists")
    void createWebinar_WhenWebinarAlreadyExists_ReturnsConflict() throws Exception {
        WebinarInsertDTO insertDTO = new WebinarInsertDTO(
                "Title",
                "Desc",
                Instant.parse("2026-12-01T10:00:00Z"),
                60
        );

        Role role = new Role();
        role.setName("ADMIN");

        User mockUser = new User();
        mockUser.setUuid(UUID.randomUUID());
        mockUser.setRole(role);

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());

        when(webinarService.saveWebinar(any(WebinarInsertDTO.class), eq(mockUser.getUuid())))
                .thenThrow(new EntityAlreadyExistsException("Webinar", "Webinar already exists"));

        mockMvc.perform(post("/api/v1/webinars")
                        .with(request -> {
                            request.setUserPrincipal(authenticationToken);
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(insertDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/v1/webinars/{uuid} - Should return 409 when webinar title already exists")
    void updateWebinar_WhenWebinarAlreadyExists_ReturnsConflict() throws Exception {
        UUID uuid = UUID.randomUUID();
        com.karmperis.webinarsapp.dto.WebinarEditDTO editDTO =
                new com.karmperis.webinarsapp.dto.WebinarEditDTO(
                        "Updated Title",
                        "Updated Desc",
                        Instant.parse("2026-12-02T10:00:00Z"),
                        90
                );

        when(webinarService.updateWebinar(eq(uuid), any(com.karmperis.webinarsapp.dto.WebinarEditDTO.class)))
                .thenThrow(new EntityAlreadyExistsException("Webinar", "Webinar already exists"));

        mockMvc.perform(put("/api/v1/webinars/{uuid}", uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editDTO)))
                .andExpect(status().isConflict());
    }
}