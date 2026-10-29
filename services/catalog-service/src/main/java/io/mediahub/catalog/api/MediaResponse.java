package io.mediahub.catalog.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import io.mediahub.catalog.media.Media;
import io.mediahub.catalog.media.MediaType;

/** API view of a title; kept separate from the document so storage changes don't leak into the contract. */
public record MediaResponse(
        String id,
        MediaType type,
        String title,
        List<String> creators,
        String identifier,
        String language,
        Integer publicationYear,
        List<String> genres,
        String description,
        String coverUrl,
        Map<String, Object> attributes,
        BigDecimal purchasePrice,
        BigDecimal rentalFee,
        Instant createdAt,
        Instant updatedAt) {

    static MediaResponse from(Media m) {
        return new MediaResponse(m.id(), m.type(), m.title(), m.creators(), m.identifier(), m.language(),
                m.publicationYear(), m.genres(), m.description(), m.coverUrl(), m.attributes(),
                m.purchasePrice(), m.rentalFee(), m.createdAt(), m.updatedAt());
    }
}
