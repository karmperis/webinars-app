package com.karmperis.webinarsapp.security;

import com.karmperis.webinarsapp.authentication.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that intercepts incoming HTTP requests to extract and validate the JWT token.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Extract and validate a bearer token, then populate the security context if valid.
     *
     * @param request     the incoming HTTP request
     * @param response    the HTTP response
     * @param filterChain the servlet filter chain
     * @throws ServletException if the filter fails
     * @throws IOException      if request/response handling fails
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");
        final String jwt;
        String username;

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        jwt = authorizationHeader.substring(7);

        try {
            username = jwtService.extractSubject(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (!userDetails.isEnabled()) {
                    throw new DisabledException("User account is disabled or deleted");
                }

                if (!jwtService.isTokenValid(jwt, userDetails)) {
                    throw new BadCredentialsException("Invalid token");
                }

                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        } catch (ExpiredJwtException e) {
            throw new AuthenticationCredentialsNotFoundException("Token has expired");
        } catch (DisabledException | BadCredentialsException e) {
            throw e;
        } catch (JwtException | IllegalArgumentException e) {
            throw new BadCredentialsException("Invalid token");
        } catch (Exception e) {
            log.error("Unexpected error during validation", e);
            throw new AuthenticationCredentialsNotFoundException("Token validation failed");
        }

        filterChain.doFilter(request, response);
    }
}