package io.mediahub.catalog.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import io.mediahub.catalog.TestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** Authentication (401) versus authorization (403), demonstrated on the catalog. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
class SecurityIT {

    private static final String BOOK = """
            {"type":"BOOK","title":"Momo","creators":["Michael Ende"]}
            """;

    @Autowired
    MockMvcTester mvc;

    @Test
    void anyoneCanBrowse() {
        assertThat(mvc.get().uri("/api/v1/media")).hasStatusOk();
    }

    @Test
    void writingWithoutATokenIs401() {
        assertThat(mvc.post().uri("/api/v1/media").contentType(MediaType.APPLICATION_JSON).content(BOOK))
                .hasStatus(HttpStatus.UNAUTHORIZED)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .hasHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer realm=\"mediahub\"");
    }

    @Test
    void anInvalidTokenIs401() {
        // A real decode attempt: the token is garbage, so the decoder rejects it before any rule runs.
        assertThat(mvc.post().uri("/api/v1/media").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt")
                .contentType(MediaType.APPLICATION_JSON).content(BOOK))
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void writingAsAMemberIs403() {
        assertThat(mvc.post().uri("/api/v1/media")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_MEMBER")))
                .contentType(MediaType.APPLICATION_JSON).content(BOOK))
                .hasStatus(HttpStatus.FORBIDDEN)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void writingAsALibrarianWorks() {
        assertThat(mvc.post().uri("/api/v1/media")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_LIBRARIAN")))
                .contentType(MediaType.APPLICATION_JSON).content(BOOK))
                .hasStatus(HttpStatus.CREATED);
    }
}
