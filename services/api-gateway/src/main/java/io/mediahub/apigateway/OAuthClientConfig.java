package io.mediahub.apigateway;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

/**
 * The gateway is a confidential OAuth client of Keycloak.
 *
 * <p>The registration is written out by hand rather than discovered from Keycloak's
 * /.well-known/openid-configuration, because discovery would use one URL for everything. The
 * browser must be sent to the public URL; the gateway's own calls must use the internal one.
 */
@Configuration
class OAuthClientConfig {

    static final String REGISTRATION_ID = "keycloak";

    @Bean
    ClientRegistrationRepository clientRegistrations(BffProperties bff) {
        String oidc = "/protocol/openid-connect";
        ClientRegistration keycloak = ClientRegistration.withRegistrationId(REGISTRATION_ID)
                .clientId(bff.clientId())
                .clientSecret(bff.clientSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "profile", "email")
                .issuerUri(bff.issuer())                                   // checked against the ID token's "iss"
                .authorizationUri(bff.issuer() + oidc + "/auth")           // browser redirect: public URL
                .tokenUri(bff.internalIssuer() + oidc + "/token")          // server-to-server: internal URL
                .jwkSetUri(bff.internalIssuer() + oidc + "/certs")
                .userInfoUri(bff.internalIssuer() + oidc + "/userinfo")
                .userNameAttributeName("preferred_username")
                .providerConfigurationMetadata(Map.of("end_session_endpoint", bff.issuer() + oidc + "/logout"))
                // PKCE even though we have a client secret: it also stops an intercepted
                // authorization code from being redeemed by anyone else.
                .clientSettings(ClientRegistration.ClientSettings.builder().requireProofKey(true).build())
                .build();
        return new InMemoryClientRegistrationRepository(keycloak);
    }

    /** Hands out the signed-in user's access token, refreshing it with the refresh token when it has expired. */
    @Bean
    OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository registrations,
            OAuth2AuthorizedClientRepository authorizedClients) {
        var manager = new DefaultOAuth2AuthorizedClientManager(registrations, authorizedClients);
        manager.setAuthorizedClientProvider(OAuth2AuthorizedClientProviderBuilder.builder()
                .authorizationCode()
                .refreshToken()
                .build());
        return manager;
    }
}
