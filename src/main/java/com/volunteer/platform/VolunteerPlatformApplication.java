package com.volunteer.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * =====================================================================
 * VolunteerPlatformApplication (Main Entry Point)
 * ---------------------------------------------------------------------
 * Launches the Spring Boot application for the Online Volunteer Management
 * Platform. Configures component scanning and registers the BCrypt password
 * encoder bean for secure password hashing.
 * =====================================================================
 */
@SpringBootApplication
public class VolunteerPlatformApplication {

    /**
     * Main method that bootstraps and starts the Spring Boot embedded server.
     */
    public static void main(String[] args) {
        SpringApplication.run(VolunteerPlatformApplication.class, args);
    }

    /**
     * Provides a BCryptPasswordEncoder bean used throughout the application
     * to hash and verify passwords securely without needing full Spring Security.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
