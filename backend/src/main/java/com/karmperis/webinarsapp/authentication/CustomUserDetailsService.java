package com.karmperis.webinarsapp.authentication;

import com.karmperis.webinarsapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom implementation of Spring Security's UserDetailsService.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    /**
     * Load a user by username , excluding soft-deleted accounts.
     *
     * @param username the username to look up
     * @return the resolved {@link UserDetails}
     * @throws UsernameNotFoundException if no active user is found with the given username
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findAuthUserByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}