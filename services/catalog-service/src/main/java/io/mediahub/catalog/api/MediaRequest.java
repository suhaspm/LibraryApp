package io.mediahub.catalog.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import io.mediahub.catalog.media.Media;
import io.mediahub.catalog.media.MediaType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MediaRequest(
        @NotNull MediaType type,
        @NotBlank @Size(max = 300) String title,
        @NotEmpty List<@NotBlank String> creators,
        @Size(max = 32) String identifier,
        @Size(min = 2, max = 2) String language,
        Integer publicationYear,
        List<String> genres,
        @Size(max = 5000) String description,
        String coverUrl,
        Map<String, Object> attributes,
        @DecimalMin("0.00") BigDecimal purchasePrice,
        @DecimalMin("0.00") BigDecimal rentalFee) {

    Media toMedia() {
        return new Media(null, type, title, List.copyOf(creators), identifier, language, publicationYear,
                genres == null ? List.of() : List.copyOf(genres), description, coverUrl,
                attributes == null ? Map.of() : Map.copyOf(attributes), purchasePrice,
                rentalFee == null ? BigDecimal.ZERO : rentalFee, null, null, null);
    }
}
