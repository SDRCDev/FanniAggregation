package org.sdrc.datum19.repository;

import java.util.List;

import org.sdrc.datum19.document.Area;
import org.sdrc.datum19.document.AreaLevel;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AreaRepository extends MongoRepository<Area, String>{

	Area findByAreaNameAndAreaLevel(String areaName, AreaLevel areaLevel);

	List<Area> findByParentAreaIdOrderByAreaName(Integer parentAreaId);

	List<Area> findByAreaIdOrderByAreaName(Integer parentAreaId);

	List<Area> findByAreaLevel(AreaLevel areaLevel);

	@Query(value="{}", fields="{areaId : 1, areaName : 1}")
	List<Area> findAreaIdAndAreaName();

	Area findByAreaNameAndAreaLevelAreaLevelId(String string, int i);

	@Cacheable
	Area findByAreaId(Integer areaId);

	List<Area> findByAreaLevelAreaLevelIdInOrderByAreaIdAsc(List<Integer> asList);
	
	List<Area> findByAreaIdIn(List<Integer> areaId);
	
}
