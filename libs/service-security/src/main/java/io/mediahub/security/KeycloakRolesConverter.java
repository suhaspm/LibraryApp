package io.mediahub.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Turns Keycloak realm roles ({@code "realm_access": {"roles": ["LIBRARIAN"]}}) into Spring
 * authorities ({@code ROLE_LIBRARIAN}), so rules read {@code hasRole("LIBRARIAN")}. The principal
 * name is the username rather than Keycloak's opaque user id, which makes logs readable.
 */
public class KeycloakRolesConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String name = jwt.hasClaim("preferred_username") ? jwt.getClaimAsString("preferred_username") : jwt.getSubject();
        return new JwtAuthenticationToken(jwt, authorities(jwt), name);
    }

    static Collection<GrantedAuthority> authorities(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess == null || !(realmAccess.get("roles") instanceof List<?> roles)) {
            return List.of();
        }
        return roles.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }
}
