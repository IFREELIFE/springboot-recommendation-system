package com.recommendation.homestay.repository;

import com.recommendation.homestay.entity.PropertyDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyDocumentRepository extends ElasticsearchRepository<PropertyDocument, Long> {
}
