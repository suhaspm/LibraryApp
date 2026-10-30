package io.mediahub.apigateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;

@SpringBootTest(properties = {
        "BFF_CLIENT_SECRET=test-secret",
        "OIDC_ISSUER=http://localhost/auth/realms/mediahub",
        "OIDC_INTERNAL=http://keycloak:8080/auth/realms/mediahub"
})
@AutoConfigureMockMvc
@Testcontainers
class GatewayIT {

    /** Sessions live in Valkey (Redis protocol), as in production. */
    @Container
    @ServiceConnection(name = "redis")
    static final GenericContainer<?> valkey = new GenericContainer<>("valkey/valkey:9.1-alpine").withExposedPorts(6379);

    /** Stands in for catalog-service and remembers the last request the gateway forwarded. */
    static final HttpServer catalog = fakeCatalog();
    static final AtomicReference<Headers> lastForwarded = new AtomicReference<>();

    @DynamicPropertySource
    static void routes(DynamicPropertyRegistry registry) {
        registry.add("CATALOG_URL", () -> "http://127.0.0.1:" + catalog.getAddress().getPort());
    }

    @Autowired
    MockMvcTester mvc;

    @Autowired
    org.springframework.security.oauth2.client.registration.ClientRegistrationRepository registrations;

    @BeforeEach
    void reset() {
        lastForwarded.set(null);
    }

    @AfterAll
    static void stop() {
        catalog.stop(0);
    }

    @Test
    void anonymousMeSaysNotSignedIn() {
        assertThat(mvc.get().uri("/api/me")).hasStatusOk().bodyJson()
                .hasPathSatisfying("$.authenticated", v -> v.assertThat().isEqualTo(false));
    }

    @Test
    void meShowsTheSignedInUserAndRoles() {
        assertThat(mvc.get().uri("/api/me").with(librarian())).hasStatusOk().bodyJson()
                .hasPathSatisfying("$.username", v -> v.assertThat().isEqualTo("librarian1"))
                .hasPathSatisfying("$.name", v -> v.assertThat().isEqualTo("Lars Librarian"))
                .hasPathSatisfying("$.roles", v -> v.assertThat().asInstanceOf(
                        org.assertj.core.api.InstanceOfAssertFactories.LIST).containsExactly("LIBRARIAN", "MEMBER"));
    }

    @Test
    void anonymousBrowsingIsForwardedWithoutAToken() {
        assertThat(mvc.get().uri("/api/v1/media?q=mann")).hasStatusOk();
        assertThat(lastForwarded.get().getFirst(HttpHeaders.AUTHORIZATION)).isNull();
    }

    @Test
    void signedInRequestsCarryTheAccessTokenButNotTheSessionCookie() {
        assertThat(mvc.post().uri("/api/v1/media").with(librarian()).with(csrf())
                .header(HttpHeaders.COOKIE, "MEDIAHUB_SESSION=abc")
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .hasStatusOk();
        assertThat(lastForwarded.get().getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer access-token");
        assertThat(lastForwarded.get().getFirst(HttpHeaders.COOKIE)).isNull();
    }

    @Test
    void postWithoutCsrfTokenIsRejectedBeforeReachingTheService() {
        assertThat(mvc.post().uri("/api/v1/media").with(librarian())
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .hasStatus(HttpStatus.FORBIDDEN);
        assertThat(lastForwarded.get()).isNull();
    }

    @Test
    void signInRedirectsToKeycloakWithPkce() {
        var result = mvc.get().uri("/oauth2/authorization/keycloak").exchange();
        assertThat(result).hasStatus(HttpStatus.FOUND);
        String location = result.getResponse().getHeader(HttpHeaders.LOCATION);
        assertThat(location)
                .startsWith("http://localhost/auth/realms/mediahub/protocol/openid-connect/auth?")
                .contains("client_id=mediahub-bff", "code_challenge=", "code_challenge_method=S256", "scope=openid");
    }

    @Test
    void logoutEndsTheSessionAndReturnsKeycloaksLogoutUrl() {
        var result = mvc.post().uri("/logout").with(librarian()).with(csrf()).exchange();
        assertThat(result).hasStatusOk().bodyJson()
                .hasPathSatisfying("$.logoutUrl", v -> v.assertThat().asString()
                        .startsWith("http://localhost/auth/realms/mediahub/protocol/openid-connect/logout?")
                        .contains("id_token_hint=", "post_logout_redirect_uri=http://localhost/"));
    }

    @Test
    void unknownPathsAreNotServed() {
        assertThat(mvc.get().uri("/admin")).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor librarian() {
        return oidcLogin()
                .idToken(t -> t.claim("preferred_username", "librarian1").claim("name", "Lars Librarian")
                        // As Keycloak sends it, including its own technical roles that /api/me must filter out.
                        .claim("realm_access", Map.of("roles", List.of("LIBRARIAN", "offline_access", "default-roles-mediahub", "MEMBER"))))
                // The real Keycloak registration, so token relay looks up the same client as in production.
                .clientRegistration(registrations.findByRegistrationId("keycloak"));
    }

    private static HttpServer fakeCatalog() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/", exchange -> {
                lastForwarded.set(exchange.getRequestHeaders());
                byte[] body = "{\"items\":[]}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
                exchange.close();
            });
            server.start();
            return server;
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
