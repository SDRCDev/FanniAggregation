package org.sdrc.datum19.repository;

import org.sdrc.datum19.document.TypeDetail;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TypeDetailsRepository extends MongoRepository<TypeDetail, String> {

}
