package io.mediahub.apigateway;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

/**
 * The gateway's OAuth client settings.
 *
 * @param issuer         Keycloak as the browser sees it; the browser is redirected here to log in,
 *                       and tokens carry it as "iss"
 * @param internalIssuer the same realm as reached from inside the cluster; used for the
 *                       server-to-server calls (code-for-token exchange, signing keys, user info)
 */
@Validated
@ConfigurationProperties("mediahub.bff")
public record BffProperties(
        @NotBlank String clientId,
        @NotBlank String clientSecret,
        @NotBlank String issuer,
        @NotBlank String internalIssuer) {
}
