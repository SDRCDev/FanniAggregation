package org.sdrc.datum19.repository;

import org.sdrc.datum19.document.ClusterForAggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClusterRepositoty extends MongoRepository<ClusterForAggregation, String> {

}
