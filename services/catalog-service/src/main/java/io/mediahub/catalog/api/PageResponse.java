package io.mediahub.catalog.api;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

/** Stable paging envelope; Spring's Page serialisation is not a contract we want clients to depend on. */
public record PageResponse<T>(List<T> items, int page, int size, long totalItems, int totalPages) {

    static <S, T> PageResponse<T> of(Page<S> page, Function<S, T> mapper) {
        return new PageResponse<>(page.map(mapper).getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
