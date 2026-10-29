package io.mediahub.catalog.media;

/** Optional filters for catalog search; null means "don't filter on this". */
public record MediaQuery(String text, MediaType type, String genre) {
}
