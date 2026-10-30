package io.mediahub.apigateway;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

/**
 * Logout for a single-page app. The SPA calls POST /logout with fetch (so it can send the CSRF
 * header), and fetch can't hand a cross-step redirect to the user. So instead of redirecting,
 * we answer with Keycloak's end-session URL and the SPA navigates there; Keycloak then ends its
 * own session (SSO logout) and sends the browser back to "/".
 */
class SpaLogoutSuccessHandler extends OidcClientInitiatedLogoutSuccessHandler {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    SpaLogoutSuccessHandler(ClientRegistrationRepository registrations) {
        super(registrations);
        setPostLogoutRedirectUri("{baseUrl}/");
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        String target = determineTargetUrl(request, response, authentication);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        JSON.writeValue(response.getOutputStream(), Map.of("logoutUrl", target));
    }
}
