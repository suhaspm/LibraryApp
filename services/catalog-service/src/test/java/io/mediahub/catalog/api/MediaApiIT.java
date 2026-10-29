package io.mediahub.catalog.api;

import static org.assertj.core.api.Assertions.assertThat;

import io.mediahub.catalog.TestcontainersConfig;
import io.mediahub.catalog.media.MediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
class MediaApiIT {

    private static final String BOOK = """
            {"type":"BOOK","title":"Der Process","creators":["Franz Kafka"],"identifier":"9783150096765",
             "language":"de","publicationYear":1925,"genres":["classic"],
             "description":"Josef K. is arrested without knowing why",
             "attributes":{"pages":288},"purchasePrice":5.00}
            """;

    private static final String DVD = """
            {"type":"DVD","title":"Lola rennt","creators":["Tom Tykwer"],"identifier":"4011976823783",
             "genres":["thriller","film"],"attributes":{"runtimeMinutes":81},"rentalFee":1.00}
            """;

    @Autowired
    MockMvcTester mvc;

    @Autowired
    MediaRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    @Test
    void createsAndReadsMediaWithTypeSpecificAttributes() {
        var created = mvc.post().uri("/api/v1/media").contentType(MediaType.APPLICATION_JSON).content(BOOK).exchange();
        assertThat(created).hasStatus(HttpStatus.CREATED);
        String location = created.getResponse().getHeader("Location");

        assertThat(mvc.get().uri(location)).hasStatusOk().bodyJson()
                .hasPathSatisfying("$.title", v -> v.assertThat().isEqualTo("Der Process"))
                .hasPathSatisfying("$.attributes.pages", v -> v.assertThat().isEqualTo(288))
                .hasPathSatisfying("$.rentalFee", v -> v.assertThat().isEqualTo(0))
                .hasPathSatisfying("$.createdAt", v -> v.assertThat().isNotNull());
    }

    @Test
    void searchesByFullTextAndFiltersByType() {
        post(BOOK);
        post(DVD);

        assertThat(mvc.get().uri("/api/v1/media?q=kafka")).hasStatusOk().bodyJson()
                .hasPathSatisfying("$.totalItems", v -> v.assertThat().isEqualTo(1))
                .hasPathSatisfying("$.items[0].title", v -> v.assertThat().isEqualTo("Der Process"));

        assertThat(mvc.get().uri("/api/v1/media?type=DVD")).hasStatusOk().bodyJson()
                .hasPathSatisfying("$.items[0].title", v -> v.assertThat().isEqualTo("Lola rennt"));
    }

    @Test
    void rejectsInvalidInputWithProblemDetails() {
        assertThat(mvc.post().uri("/api/v1/media").contentType(MediaType.APPLICATION_JSON).content("""
                {"type":"BOOK","title":"","creators":[]}
                """)).hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void rejectsDuplicateIdentifier() {
        post(BOOK);
        assertThat(mvc.post().uri("/api/v1/media").contentType(MediaType.APPLICATION_JSON).content(BOOK))
                .hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    void returns404ForUnknownMedia() {
        assertThat(mvc.get().uri("/api/v1/media/does-not-exist")).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(mvc.delete().uri("/api/v1/media/does-not-exist")).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void updatesAndDeletes() {
        String location = post(BOOK);
        assertThat(mvc.put().uri(location).contentType(MediaType.APPLICATION_JSON)
                .content(BOOK.replace("Der Process", "Der Prozess")))
                .hasStatusOk().bodyJson()
                .hasPathSatisfying("$.title", v -> v.assertThat().isEqualTo("Der Prozess"));

        assertThat(mvc.delete().uri(location)).hasStatus(HttpStatus.NO_CONTENT);
        assertThat(mvc.get().uri(location)).hasStatus(HttpStatus.NOT_FOUND);
    }

    private String post(String body) {
        var result = mvc.post().uri("/api/v1/media").contentType(MediaType.APPLICATION_JSON).content(body).exchange();
        assertThat(result).hasStatus(HttpStatus.CREATED);
        return result.getResponse().getHeader("Location");
    }
}
