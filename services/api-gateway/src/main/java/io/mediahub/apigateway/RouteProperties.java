package io.mediahub.apigateway;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

/** Where each backend service lives, as seen from the gateway. */
@Validated
@ConfigurationProperties("mediahub.routes")
public record RouteProperties(@NotBlank String catalog) {
}
