package io.mediahub.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.jwt.BadJwtException;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpServer;

/**
 * Runs the real decoder against a real JWKS endpoint, the way a service talks to Keycloak.
 * Each test changes one thing about an otherwise valid token.
 */
class TokenValidationTest {

    private static final String ISSUER = "http://localhost/auth/realms/mediahub";

    private static RSAKey realmKey;
    private static HttpServer jwks;
    private static JwtDecoder decoder;

    @BeforeAll
    static void startFakeKeycloak() throws Exception {
        realmKey = new RSAKeyGenerator(2048).keyID("realm-key-1").generate();
        byte[] body = new JWKSet(realmKey.toPublicJWK()).toString().getBytes(StandardCharsets.UTF_8);
        jwks = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        jwks.createContext("/certs", exchange -> {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        jwks.start();
        var properties = new ServiceSecurityProperties(ISSUER,
                URI.create("http://127.0.0.1:" + jwks.getAddress().getPort() + "/certs"), "mediahub-api");
        decoder = new ServiceSecurityAutoConfiguration().jwtDecoder(properties);
    }

    @AfterAll
    static void stop() {
        jwks.stop(0);
    }

    @Test
    void acceptsAValidTokenAndMapsRealmRoles() throws Exception {
        var jwt = decoder.decode(token(valid().build(), realmKey));

        assertThat(jwt.getClaimAsString("preferred_username")).isEqualTo("librarian1");
        var auth = new KeycloakRolesConverter().convert(jwt);
        assertThat(auth.getName()).isEqualTo("librarian1");
        assertThat(auth.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_LIBRARIAN", "ROLE_MEMBER");
    }

    @Test
    void rejectsTokenFromAnotherIssuer() throws Exception {
        String foreign = token(valid().issuer("http://evil.example/realms/mediahub").build(), realmKey);
        assertThatThrownBy(() -> decoder.decode(foreign)).isInstanceOf(JwtValidationException.class)
                .hasMessageContaining("iss");
    }

    @Test
    void rejectsTokenMeantForAnotherAudience() throws Exception {
        String other = token(valid().audience("some-other-app").build(), realmKey);
        assertThatThrownBy(() -> decoder.decode(other)).isInstanceOf(JwtValidationException.class)
                .hasMessageContaining("aud");
    }

    @Test
    void rejectsExpiredToken() throws Exception {
        String expired = token(valid().issueTime(Date.from(Instant.now().minusSeconds(900)))
                .expirationTime(Date.from(Instant.now().minusSeconds(600))).build(), realmKey);
        assertThatThrownBy(() -> decoder.decode(expired)).isInstanceOf(JwtValidationException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void rejectsTokenSignedWithAnotherKey() throws Exception {
        RSAKey attackerKey = new RSAKeyGenerator(2048).keyID("realm-key-1").generate(); // same kid, different key
        String forged = token(valid().build(), attackerKey);
        assertThatThrownBy(() -> decoder.decode(forged)).isInstanceOf(BadJwtException.class)
                .hasMessageContaining("signature");
    }

    @Test
    void tokenWithoutRolesHasNoAuthorities() throws Exception {
        var jwt = decoder.decode(token(valid().claim("realm_access", null).build(), realmKey));
        assertThat(new KeycloakRolesConverter().convert(jwt).getAuthorities()).isEmpty();
    }

    private static JWTClaimsSet.Builder valid() {
        return new JWTClaimsSet.Builder()
                .issuer(ISSUER)
                .subject("8f2c1e4a-user-id")
                .audience(List.of("mediahub-api", "account"))
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plusSeconds(300)))
                .claim("preferred_username", "librarian1")
                .claim("realm_access", Map.of("roles", List.of("LIBRARIAN", "MEMBER")));
    }

    private static String token(JWTClaimsSet claims, RSAKey key) throws Exception {
        var jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.getKeyID()).build(), claims);
        jwt.sign(new RSASSASigner(key));
        return jwt.serialize();
    }
}
