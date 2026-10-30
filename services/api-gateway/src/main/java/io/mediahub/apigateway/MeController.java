package io.mediahub.apigateway;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tells the UI who is signed in. The UI can't read tokens (they never leave the server), so this
 * is how it learns the user's name and roles. Roles here only shape the UI (hiding buttons);
 * the services enforce them.
 */
@RestController
class MeController {

    record Me(boolean authenticated, String username, String name, String email, List<String> roles) {

        static final Me ANONYMOUS = new Me(false, null, null, null, List.of());
    }

    @GetMapping("/api/me")
    Me me(@AuthenticationPrincipal OidcUser user) {
        if (user == null) {
            return Me.ANONYMOUS;
        }
        return new Me(true, user.getPreferredUsername(), user.getFullName(), user.getEmail(),
                mediahubRoles(user.getIdToken().getClaimAsMap("realm_access")));
    }

    /**
     * Our roles from the ID token's realm_access claim. Keycloak also puts its own technical
     * roles there (offline_access, default-roles-...), which mean nothing to the UI.
     */
    static List<String> mediahubRoles(Map<String, Object> realmAccess) {
        if (realmAccess == null || !(realmAccess.get("roles") instanceof List<?> roles)) {
            return List.of();
        }
        return roles.stream()
                .map(String::valueOf)
                .filter(r -> r.equals("MEMBER") || r.equals("LIBRARIAN"))
                .sorted()
                .toList();
    }
}
