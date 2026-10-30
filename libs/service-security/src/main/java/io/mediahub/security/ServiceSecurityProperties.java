package io.mediahub.security;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * How a service validates access tokens.
 *
 * <p>The two URLs differ on purpose. {@code issuer} is what Keycloak writes into every token: the
 * address the <em>browser</em> uses (http://localhost/auth/realms/mediahub). {@code jwkSetUri} is
 * where this service downloads Keycloak's public keys from <em>inside</em> the cluster. Using the
 * issuer URL for both is the classic "works in the browser, 401 in the pod" mistake.
 */
@Validated
@ConfigurationProperties("mediahub.security")
public record ServiceSecurityProperties(
        @NotBlank String issuer,
        @NotNull URI jwkSetUri,
        @NotBlank String audience) {
}
