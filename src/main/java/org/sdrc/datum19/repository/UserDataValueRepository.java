package org.sdrc.datum19.repository;

import org.sdrc.datum19.document.UserDatumValue;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserDataValueRepository extends MongoRepository<UserDatumValue, String> {

}
