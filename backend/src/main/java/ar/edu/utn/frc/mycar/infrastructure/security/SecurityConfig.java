package ar.edu.utn.frc.mycar.infrastructure.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for the MyCar application.
 *
 * <p>Configures a stateless, JWT-based security chain. Sessions are never created;
 * every request must carry a valid {@code Authorization: Bearer <token>} header
 * to access protected endpoints.</p>
 *
 * <p>Public endpoints (no token required):
 * <ul>
 *   <li>{@code /api/auth/**} — register and login</li>
 *   <li>{@code /ping} — health check</li>
 *   <li>{@code /swagger-ui/**}, {@code /v3/api-docs/**} — OpenAPI documentation</li>
 * </ul>
 * </p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    /**
     * @param jwtAuthFilter filter that extracts and validates the JWT on every request
     */
    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /**
     * Defines the security filter chain.
     *
     * <ul>
     *   <li>CSRF disabled — stateless REST API, no session cookies are used.</li>
     *   <li>Session policy set to {@code STATELESS} — no {@link jakarta.servlet.http.HttpSession} is created.</li>
     *   <li>{@link JwtAuthFilter} is inserted before {@code UsernamePasswordAuthenticationFilter}.</li>
     * </ul>
     *
     * @param http the {@link HttpSecurity} to configure
     * @return the built {@link SecurityFilterChain}
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/ping",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Provides the {@link PasswordEncoder} bean used throughout the application.
     * Uses BCrypt with the default strength factor (10 rounds).
     *
     * @return a {@link BCryptPasswordEncoder} instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Disables the automatic servlet-container registration of {@link JwtAuthFilter}.
     *
     * <p>Without this bean, Spring Boot would register the filter both in the servlet
     * container (via {@code @Component} detection) <em>and</em> in the security filter
     * chain (via {@code addFilterBefore}), causing it to execute twice per request.</p>
     *
     * @param filter the filter whose auto-registration should be suppressed
     * @return a disabled {@link FilterRegistrationBean}
     */
    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtFilterRegistration(JwtAuthFilter filter) {
        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
