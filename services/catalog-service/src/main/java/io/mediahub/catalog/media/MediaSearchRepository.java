package io.mediahub.catalog.media;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MediaSearchRepository {

    Page<Media> search(MediaQuery query, Pageable pageable);
}
