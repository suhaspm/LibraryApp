package io.mediahub.catalog.media;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.data.support.PageableExecutionUtils;

class MediaSearchRepositoryImpl implements MediaSearchRepository {

    private final MongoTemplate mongo;

    MediaSearchRepositoryImpl(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    @Override
    public Page<Media> search(MediaQuery filter, Pageable pageable) {
        Query query = filter.text() == null
                ? new Query()
                : TextQuery.queryText(TextCriteria.forDefaultLanguage().matching(filter.text())).sortByScore();
        if (filter.type() != null) {
            query.addCriteria(Criteria.where("type").is(filter.type()));
        }
        if (filter.genre() != null) {
            query.addCriteria(Criteria.where("genres").is(filter.genre()));
        }

        long total = mongo.count(Query.of(query).limit(-1).skip(-1), Media.class);
        List<Media> content = mongo.find(query.with(pageable), Media.class);
        return PageableExecutionUtils.getPage(content, pageable, () -> total);
    }
}
