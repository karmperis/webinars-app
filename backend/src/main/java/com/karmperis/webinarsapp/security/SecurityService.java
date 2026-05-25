package com.karmperis.webinarsapp.security;

import com.karmperis.webinarsapp.model.User;
import com.karmperis.webinarsapp.repository.WebinarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Security helper service used in method-level authorization expressions.
 */
@Service("securityService")
@RequiredArgsConstructor
@Slf4j
public class SecurityService {
    private final WebinarRepository webinarRepository;

    /**
     * Check whether the authenticated user owns the profile with the given UUID.
     *
     * @param targetUserUuid the target user's UUID
     * @param authentication the current authentication
     * @return {@code true} if the authenticated user matches the target UUID
     */
    public boolean isOwnProfile(UUID targetUserUuid, Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null || targetUserUuid == null) {
            log.warn("Access denied: Authentication or target UUID is null");
            return false;
        }

        User principal = (User) authentication.getPrincipal();
        return principal.getUuid().equals(targetUserUuid);
    }

    /**
     * Check whether the authenticated user is the organizer of the webinar.
     *
     * @param webinarUuid    the webinar UUID
     * @param authentication the current authentication
     * @return {@code true} if the user organizes the webinar
     */
    public boolean isOwnWebinar(UUID webinarUuid, Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null || webinarUuid == null) {
            log.warn("Access denied: Authentication or webinar UUID is null");
            return false;
        }

        User principal = (User) authentication.getPrincipal();
        return webinarRepository.existsByUuidAndUserUuidAndDeletedAtIsNull(webinarUuid, principal.getUuid());
    }
}