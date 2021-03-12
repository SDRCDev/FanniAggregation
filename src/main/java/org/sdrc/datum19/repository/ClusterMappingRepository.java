package org.sdrc.datum19.repository;

import java.util.List;

import org.sdrc.datum19.document.Area;
import org.sdrc.datum19.document.ClusterMapping;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author subham
 *
 */
public interface ClusterMappingRepository extends MongoRepository<ClusterMapping, String> {

	ClusterMapping findByVillage(Area area);
	
	List<ClusterMapping> findAllByVillageIn(List<Area> areaList);

}
