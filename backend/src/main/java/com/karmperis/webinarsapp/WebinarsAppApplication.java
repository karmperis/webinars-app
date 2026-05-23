package com.karmperis.webinarsapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

//TODO: REMOVE SecurityAutoConfiguration.class
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableJpaAuditing
public class WebinarsAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebinarsAppApplication.class, args);
    }

}
