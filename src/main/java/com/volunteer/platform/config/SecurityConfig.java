package com.volunteer.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

/**
 * =====================================================================
 * SecurityConfig (Security Layer)
 * ---------------------------------------------------------------------
 * Adds Spring Security in front of the whole application.
 *
 * What Spring Security does here:
 *  1. CSRF protection - every POST form must carry a secret _csrf token.
 *     Thymeleaf adds this hidden field automatically to every form that
 *     uses th:action, so a fake form on another website cannot submit
 *     actions (sign-up, delete, block user) on behalf of a logged-in user.
 *  2. Session fixation protection - the session id changes at login
 *     (see AuthController.processLogin), so a stolen pre-login id is useless.
 *  3. Security headers - X-Frame-Options, X-Content-Type-Options, etc.
 *
 * Who-can-open-which-page (role checks) and the "blocked user" re-check
 * are still done by LoginInterceptor, which runs after this filter chain.
 * =====================================================================
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Defines the security filter chain applied to every HTTP request.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Page access is decided by LoginInterceptor, so Spring Security lets requests through
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())

            // CSRF is ON. Only the H2 developer console (dev profile) is excluded.
            .csrf(csrf -> csrf.ignoringRequestMatchers(antMatcher("/h2-console/**")))

            // We have our own login page and logout URL (AuthController), so turn off the built-in ones
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .logout(logout -> logout.disable())

            // Create a session only when needed and migrate it (new id) when the user logs in
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .sessionFixation(fixation -> fixation.migrateSession()))

            // Allow the H2 console to load inside a frame from our own site only
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}
