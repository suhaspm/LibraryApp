package io.mediahub.apigateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * Backend-for-frontend security: the browser holds only an HttpOnly session cookie, while the
 * tokens stay in the server-side session.
 *
 * <p>The gateway authenticates the user but leaves most authorization to the services behind it.
 * It forwards every /api request with the user's token, and catalog-service decides 401/403
 * itself. Checking only at the edge would trust anything that reaches a service directly.
 */
@Configuration
class SecurityConfig {

    @Bean
    SecurityFilterChain bff(HttpSecurity http, ClientRegistrationRepository registrations,
            @Value("${management.server.port:-1}") int managementPort) throws Exception {
        return http
                .authorizeHttpRequests(a -> a
                        .requestMatchers(r -> r.getLocalPort() == managementPort).permitAll()
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/oauth2/**", "/login/**", "/logout").permitAll()
                        .anyRequest().denyAll())
                // Sign in = navigate to /oauth2/authorization/keycloak. No HTML login page of our own.
                .oauth2Login(o -> o
                        .loginPage("/oauth2/authorization/" + OAuthClientConfig.REGISTRATION_ID)
                        .defaultSuccessUrl("/", true))
                // Ends the Keycloak session too (RP-initiated logout), not just ours.
                .logout(l -> l.logoutSuccessHandler(new SpaLogoutSuccessHandler(registrations)))
                // The session cookie is sent automatically by the browser, so state-changing requests
                // must also carry the XSRF-TOKEN cookie's value in an X-XSRF-TOKEN header, which only
                // our own JavaScript (same origin) can read.
                .csrf(c -> c.spa())
                // API callers get a 401 status, never an HTML redirect to the login page.
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .build();
    }
}
