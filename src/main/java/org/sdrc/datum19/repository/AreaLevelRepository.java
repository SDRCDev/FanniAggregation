package org.sdrc.datum19.repository;

import org.sdrc.datum19.document.AreaLevel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AreaLevelRepository extends MongoRepository<AreaLevel, String>{

	AreaLevel findByAreaLevelId(Integer areaLevelId);

}
