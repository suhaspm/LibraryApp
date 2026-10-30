package io.mediahub.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Makes any service that depends on this library an OAuth2 resource server that trusts tokens
 * from the MediaHub Keycloak realm, and only those.
 */
@AutoConfiguration
@EnableConfigurationProperties(ServiceSecurityProperties.class)
public class ServiceSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    JwtDecoder jwtDecoder(ServiceSecurityProperties properties) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(properties.jwkSetUri().toString()).build();
        decoder.setJwtValidator(validator(properties));
        return decoder;
    }

    /**
     * Signature is checked by the decoder itself. On top of that a token must: not be expired,
     * come from our realm (iss), and be meant for MediaHub APIs (aud). Without the audience check,
     * a token Keycloak issued to some other application in the realm would be accepted too.
     */
    static OAuth2TokenValidator<Jwt> validator(ServiceSecurityProperties properties) {
        return new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(properties.issuer()),
                new JwtClaimValidator<java.util.List<String>>("aud",
                        aud -> aud != null && aud.contains(properties.audience())));
    }

    @Bean
    @ConditionalOnMissingBean
    ResourceServerDefaults resourceServerDefaults(JwtDecoder decoder,
            @Value("${management.server.port:-1}") int managementPort) {
        return new ResourceServerDefaults(decoder, managementPort);
    }
}
