package io.mediahub.catalog.media;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MediaService {

    private final MediaRepository repository;

    public MediaService(MediaRepository repository) {
        this.repository = repository;
    }

    public Page<Media> search(MediaQuery query, Pageable pageable) {
        return repository.search(query, pageable);
    }

    public Media get(String id) {
        return repository.findById(id).orElseThrow(() -> new MediaNotFoundException(id));
    }

    public Media create(Media media) {
        return repository.save(media);
    }

    public Media update(String id, Media details) {
        return repository.save(get(id).withDetails(details));
    }

    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new MediaNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
