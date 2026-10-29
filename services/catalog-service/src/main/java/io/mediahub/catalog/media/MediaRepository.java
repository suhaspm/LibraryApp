package io.mediahub.catalog.media;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MediaRepository extends MongoRepository<Media, String>, MediaSearchRepository {
}
