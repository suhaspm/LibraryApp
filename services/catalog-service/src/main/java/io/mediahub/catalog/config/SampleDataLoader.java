package io.mediahub.catalog.config;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import io.mediahub.catalog.media.Media;
import io.mediahub.catalog.media.MediaRepository;
import io.mediahub.catalog.media.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Seeds a few titles for local runs ({@code --spring.profiles.active=sample-data}); never active in tests. */
@Component
@Profile("sample-data")
class SampleDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SampleDataLoader.class);

    private final MediaRepository repository;

    SampleDataLoader(MediaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) {
            return;
        }
        repository.saveAll(List.of(
                media(MediaType.BOOK, "Buddenbrooks", List.of("Thomas Mann"), "9783596294312", "de", 1901,
                        List.of("classic", "novel"), Map.of("pages", 759), "12.00", "0.00"),
                media(MediaType.BOOK, "Designing Data-Intensive Applications", List.of("Martin Kleppmann"),
                        "9781449373320", "en", 2017, List.of("computing"), Map.of("pages", 616), "45.00", "0.00"),
                media(MediaType.DVD, "Soul Kitchen", List.of("Fatih Akin"), "4006680048590", "de", 2009,
                        List.of("comedy", "film"), Map.of("runtimeMinutes", 99, "region", 2), "9.99", "1.00"),
                media(MediaType.VIDEO_GAME, "The Legend of Zelda: Tears of the Kingdom", List.of("Nintendo"),
                        "0045496478643", "en", 2023, List.of("adventure"), Map.of("platform", "Nintendo Switch", "pegi", 12),
                        "59.99", "2.00"),
                media(MediaType.AUDIOBOOK, "Tschick", List.of("Wolfgang Herrndorf"), "9783869090994", "de", 2010,
                        List.of("youth", "novel"), Map.of("narrator", "Hanno Koffler", "runtimeMinutes", 270), "14.95", "0.00")));
        log.info("Loaded sample catalog data");
    }

    private static Media media(MediaType type, String title, List<String> creators, String identifier, String language,
            int year, List<String> genres, Map<String, Object> attributes, String price, String fee) {
        return new Media(null, type, title, creators, identifier, language, year, genres, null, null, attributes,
                new BigDecimal(price), new BigDecimal(fee), null, null, null);
    }
}
