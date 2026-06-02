package com.karmperis.webinarsapp;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class WebinarsAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebinarsAppApplication.class, args);
    }

    /**
     * Configures the Spring Security context strategy to be inheritable by child threads.
     * Changing the strategy to {@code MODE_INHERITABLETHREADLOCAL} ensures that the
     * security context is safely propagated to child threads, allowing asynchronous
     * operations to be securely authorized.
     */
    @PostConstruct
    public void enableInheritableSecurityContext() {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }
}