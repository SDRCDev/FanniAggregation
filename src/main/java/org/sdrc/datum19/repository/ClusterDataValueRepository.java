package org.sdrc.datum19.repository;

import org.sdrc.datum19.document.ClusterDataValue;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClusterDataValueRepository extends MongoRepository<ClusterDataValue, String>{

}
