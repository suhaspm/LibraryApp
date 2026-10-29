package io.mediahub.catalog.media;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * A title in the catalog (not a physical copy; copies belong to inventory-service).
 *
 * <p>Fields shared by every media type are typed; type-specific details such as page count
 * for books or platform for games live in {@code attributes}. That variable shape is the
 * reason the catalog uses a document store (see docs/adr/0003).
 */
@Document("media")
public record Media(
        @Id String id,
        @Indexed MediaType type,
        @TextIndexed(weight = 3) String title,
        @TextIndexed(weight = 2) List<String> creators,
        @Indexed(unique = true, sparse = true) String identifier,
        String language,
        Integer publicationYear,
        @Indexed List<String> genres,
        @TextIndexed String description,
        String coverUrl,
        Map<String, Object> attributes,
        BigDecimal purchasePrice,
        BigDecimal rentalFee,
        @CreatedDate Instant createdAt,
        @LastModifiedDate Instant updatedAt,
        @Version Long version) {

    public Media withDetails(Media details) {
        return new Media(id, details.type, details.title, details.creators, details.identifier,
                details.language, details.publicationYear, details.genres, details.description,
                details.coverUrl, details.attributes, details.purchasePrice, details.rentalFee,
                createdAt, updatedAt, version);
    }
}
