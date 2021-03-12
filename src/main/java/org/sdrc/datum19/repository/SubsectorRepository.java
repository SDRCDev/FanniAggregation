package org.sdrc.datum19.repository;

import org.sdrc.datum19.document.Subsector;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SubsectorRepository extends MongoRepository<Subsector, String> {

	Subsector findTopByOrderByIdDesc();

}
