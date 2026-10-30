package io.mediahub.security;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;

/**
 * The settings every MediaHub API shares. A service applies them, then adds only its own
 * authorization rules:
 *
 * <pre>{@code
 * defaults.apply(http);
 * http.authorizeHttpRequests(a -> a.anyRequest().hasRole("LIBRARIAN"));
 * }</pre>
 */
public class ResourceServerDefaults {

    private final JwtDecoder decoder;
    private final int managementPort;

    ResourceServerDefaults(JwtDecoder decoder, int managementPort) {
        this.decoder = decoder;
        this.managementPort = managementPort;
    }

    public HttpSecurity apply(HttpSecurity http) throws Exception {
        return http
                // Bearer tokens, no cookies: no session to hijack and no CSRF risk, so both are off.
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(c -> c.disable())
                .oauth2ResourceServer(o -> o
                        .jwt(jwt -> jwt.decoder(decoder).jwtAuthenticationConverter(new KeycloakRolesConverter()))
                        .authenticationEntryPoint(ProblemResponses.unauthorized())
                        .accessDeniedHandler(ProblemResponses.forbidden()))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(ProblemResponses.unauthorized())
                        .accessDeniedHandler(ProblemResponses.forbidden()))
                // Kubernetes probes and Prometheus hit the management port without a token.
                // It is never routed through the gateway, so it is not reachable from outside.
                .authorizeHttpRequests(a -> a.requestMatchers(r -> r.getLocalPort() == managementPort).permitAll())
                .httpBasic(h -> h.disable())
                .formLogin(f -> f.disable())
                .anonymous(Customizer.withDefaults());
    }
}
