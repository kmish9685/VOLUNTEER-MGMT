package com.volunteer.platform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * =====================================================================
 * WebConfig (Configuration Layer)
 * ---------------------------------------------------------------------
 * Configures Spring MVC behavior, registering the LoginInterceptor to
 * guard protected URLs (/admin/**, /org/**, /volunteer/**, /messages/**)
 * while leaving static assets and public authentication pages freely accessible.
 * =====================================================================
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;

    /**
     * Injects the LoginInterceptor component.
     */
    public WebConfig(LoginInterceptor loginInterceptor) {
        this.loginInterceptor = loginInterceptor;
    }

    /**
     * Registers the LoginInterceptor and specifies URL paths to protect and exclude.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/admin/**", "/org/**", "/volunteer/**", "/messages/**")
                .excludePathPatterns(
                        "/",
                        "/login",
                        "/register",
                        "/logout",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/error",
                        "/h2-console/**"
                );
    }
}
