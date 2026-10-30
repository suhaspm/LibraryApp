package io.mediahub.catalog.config;

import io.mediahub.security.ResourceServerDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Who may do what in the catalog. Authentication (is the token genuine?) is handled by the
 * shared library; this class only decides authorization.
 */
@Configuration
class SecurityConfig {

    @Bean
    SecurityFilterChain api(HttpSecurity http, ResourceServerDefaults defaults) throws Exception {
        defaults.apply(http);
        http.authorizeHttpRequests(a -> a
                // Anyone may browse the catalog, signed in or not.
                .requestMatchers(HttpMethod.GET, "/api/v1/media", "/api/v1/media/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                // Changing it is a librarian's job.
                .requestMatchers("/api/v1/media/**", "/api/v1/media").hasRole("LIBRARIAN")
                .anyRequest().denyAll());
        return http.build();
    }
}
