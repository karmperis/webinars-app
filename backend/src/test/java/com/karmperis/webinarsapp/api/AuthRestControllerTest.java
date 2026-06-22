package com.karmperis.webinarsapp.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karmperis.webinarsapp.authentication.AuthenticationService;
import com.karmperis.webinarsapp.dto.AuthenticationRequestDTO;
import com.karmperis.webinarsapp.dto.AuthenticationResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AuthRestController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthRestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private com.karmperis.webinarsapp.authentication.JwtService jwtService;

    @MockitoBean
    private org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("POST /authenticate - Should return 200 OK and response DTO")
    void authenticate_ReturnsOkAndResponse() throws Exception {
        AuthenticationRequestDTO requestDTO = new AuthenticationRequestDTO("testuser", "password123");
        AuthenticationResponseDTO responseDTO = new AuthenticationResponseDTO("mocked-jwt-token");

        when(authenticationService.authenticate(any(AuthenticationRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"));

        verify(authenticationService).authenticate(argThat(dto ->
                dto.username().equals("testuser")
                        && dto.password().equals("password123")
        ));
    }

    @Test
    @DisplayName("POST /authenticate - Should return 400 Bad Request when request is invalid")
    void authenticate_WithInvalidRequest_ReturnsBadRequest() throws Exception {
        AuthenticationRequestDTO requestDTO = new AuthenticationRequestDTO("", "");

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /authenticate - Should return 401 Unauthorized when credentials are invalid")
    void authenticate_WithInvalidCredentials_ReturnsUnauthorized() throws Exception {
        AuthenticationRequestDTO requestDTO = new AuthenticationRequestDTO("testuser", "wrong-password");

        when(authenticationService.authenticate(any(AuthenticationRequestDTO.class)))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isUnauthorized());
    }
}