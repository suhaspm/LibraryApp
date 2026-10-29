package io.mediahub.catalog.api;

import java.net.URI;

import io.mediahub.catalog.media.MediaQuery;
import io.mediahub.catalog.media.MediaService;
import io.mediahub.catalog.media.MediaType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/media")
@Validated
public class MediaController {

    private final MediaService service;

    public MediaController(MediaService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<MediaResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) MediaType type,
            @RequestParam(required = false) String genre,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        var query = new MediaQuery(q == null || q.isBlank() ? null : q, type, genre);
        return PageResponse.of(service.search(query, PageRequest.of(page, size)), MediaResponse::from);
    }

    @GetMapping("/{id}")
    public MediaResponse get(@PathVariable String id) {
        return MediaResponse.from(service.get(id));
    }

    @PostMapping
    public ResponseEntity<MediaResponse> create(@Valid @RequestBody MediaRequest request) {
        var created = MediaResponse.from(service.create(request.toMedia()));
        return ResponseEntity.created(URI.create("/api/v1/media/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public MediaResponse update(@PathVariable String id, @Valid @RequestBody MediaRequest request) {
        return MediaResponse.from(service.update(id, request.toMedia()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
