package com.karmperis.webinarsapp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Security-related Spring configuration.
 * Exposes beans used by the security layer.
 */
@Configuration
public class SecurityConfiguration {

    /**
     * Provides a {@link PasswordEncoder} that uses BCrypt for hashing passwords.
     *
     * @return a {@link BCryptPasswordEncoder} instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}