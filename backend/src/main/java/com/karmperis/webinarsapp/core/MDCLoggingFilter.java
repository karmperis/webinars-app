package com.karmperis.webinarsapp.core;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Mapped Diagnostic Context for contextual info inject into loggers.
 * Temporary request-scoped log variables.
 */
@Component
public class MDCLoggingFilter extends OncePerRequestFilter {

    /**
     * Enriches the logging context (MDC) for the duration of the current request.
     * <p>
     * Populates the MDC with:
     * <ul>
     *   <li>{@code user}: the authenticated principal name or {@code anonymous}</li>
     *   <li>{@code ip}: the client IP (prefers {@code X-Forwarded-For} when present)</li>
     * </ul>
     * The MDC is always cleared in a {@code finally} block to avoid leaking values between requests.
     *
     * @param request     current HTTP request
     * @param response    current HTTP response
     * @param filterChain remaining filter chain
     * @throws ServletException if the downstream filter chain throws a servlet error
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String user = "anonymous";
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                user = auth.getName();
            }

            // --- Client IP ---
            String clientIp = request.getHeader("X-Forwarded-For");
            if (clientIp != null && !clientIp.isEmpty()) {
                clientIp = clientIp.split(",")[0].trim();  // Get original client IP if behind a proxy
            } else {
                clientIp = request.getRemoteAddr();        // Fallback to direct connection
            }
            if ("0:0:0:0:0:0:0:1".equals(clientIp)) {
                clientIp = "127.0.0.1";
            }

            // --- Put values into MDC ---
            MDC.put("user", user);
            MDC.put("ip", clientIp);

            filterChain.doFilter(request, response);
        } finally {
            // Always clear MDC to avoid leaking data between threads
            MDC.clear();
        }
    }
}