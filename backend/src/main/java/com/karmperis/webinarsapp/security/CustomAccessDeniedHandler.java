package com.karmperis.webinarsapp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karmperis.webinarsapp.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Authorization Filter. Returns a structured JSON 403 Forbidden response.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    /**
     * Handle access denied events by returning a structured JSON 403 response.
     *
     * @param request               the incoming HTTP request
     * @param response              the HTTP response
     * @param accessDeniedException the access denial exception
     * @throws IOException if response writing fails
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        log.warn("Access denied for user to request={} with message={}", request.getRequestURI(), accessDeniedException.getMessage());

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json; charset=UTF-8");

        response.getWriter().write(
                objectMapper.writeValueAsString(
                        new ErrorResponseDTO("ACCESS_DENIED", "User is not allowed to access this route.")
                )
        );
    }
}